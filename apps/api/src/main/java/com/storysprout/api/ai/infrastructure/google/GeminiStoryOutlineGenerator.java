package com.storysprout.api.ai.infrastructure.google;

import com.storysprout.api.outline.GeneratedOutlineScene;
import com.storysprout.api.outline.OutlineGenerationException;
import com.storysprout.api.outline.StoryOutlineGenerationRequest;
import com.storysprout.api.outline.StoryOutlineGenerationResult;
import com.storysprout.api.outline.StoryOutlineGenerator;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "storysprout.ai.provider", havingValue = "gemini")
public class GeminiStoryOutlineGenerator implements StoryOutlineGenerator {
    private static final Logger log = LoggerFactory.getLogger(GeminiStoryOutlineGenerator.class);
    private final ChatClient chatClient;
    private final GeminiStoryOutlinePromptBuilder promptBuilder;
    private final GeminiAiCallExecutor callExecutor;
    private final GeminiAiProperties properties;
    private final String model;

    public GeminiStoryOutlineGenerator(ChatClient chatClient, GeminiStoryOutlinePromptBuilder promptBuilder, GeminiAiCallExecutor callExecutor, GeminiAiProperties properties, @Value("${spring.ai.google.genai.chat.model:gemini-2.5-flash}") String model) {
        this.chatClient = chatClient;
        this.promptBuilder = promptBuilder;
        this.callExecutor = callExecutor;
        this.properties = properties;
        this.model = model;
    }

    @Override
    public StoryOutlineGenerationResult generate(StoryOutlineGenerationRequest request) {
        String prompt = promptBuilder.build(request);
        long started = System.nanoTime();
        for (int attempt = 1; attempt <= properties.getMaxAttempts(); attempt++) {
            try {
                GeminiStoryOutlineResponse response = callExecutor.execute(
                    () -> chatClient.prompt().user(prompt).call().entity(GeminiStoryOutlineResponse.class, spec -> spec.useProviderStructuredOutput()),
                    properties.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
                List<GeneratedOutlineScene> scenes = response == null || response.scenes() == null ? List.of() : response.scenes();
                validate(scenes);
                log.info("AI story outline generation succeeded provider=google-genai model={} durationMs={} attempts={}", model, elapsedMillis(started), attempt);
                return new StoryOutlineGenerationResult(scenes);
            } catch (TimeoutException ex) {
                log.warn("AI story outline generation timed out provider=google-genai model={} durationMs={} attempts={}", model, elapsedMillis(started), attempt);
                throw new OutlineGenerationException("Outline generation timed out");
            } catch (OutlineGenerationException ex) {
                throw ex;
            } catch (TransientAiException ex) {
                if (attempt < properties.getMaxAttempts()) continue;
                throw new OutlineGenerationException("Outline generation temporarily unavailable");
            } catch (NonTransientAiException ex) {
                throw new OutlineGenerationException("Outline generation is unavailable");
            } catch (RuntimeException ex) {
                throw new OutlineGenerationException("Outline generation is unavailable");
            }
        }
        throw new OutlineGenerationException("Outline generation is unavailable");
    }

    private void validate(List<GeneratedOutlineScene> scenes) {
        if (scenes.isEmpty()) throw new OutlineGenerationException("AI returned no scenes");
        for (GeneratedOutlineScene scene : scenes) {
            if (scene == null || scene.title() == null || scene.title().isBlank() || scene.title().trim().length() > 200) throw new OutlineGenerationException("AI returned an invalid scene title");
            if (scene.summary() == null || scene.summary().isBlank() || scene.summary().trim().length() > 2000) throw new OutlineGenerationException("AI returned an invalid scene summary");
            if (scene.durationSeconds() == null || scene.durationSeconds() < 1) throw new OutlineGenerationException("AI returned an invalid scene duration");
        }
    }

    private long elapsedMillis(long started) { return Duration.ofNanos(System.nanoTime() - started).toMillis(); }
}
