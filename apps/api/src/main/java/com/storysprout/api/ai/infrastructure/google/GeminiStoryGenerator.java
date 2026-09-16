package com.storysprout.api.ai.infrastructure.google;

import com.storysprout.api.story.StoryGenerationException;
import com.storysprout.api.story.StoryGenerationRequest;
import com.storysprout.api.story.StoryGenerationResult;
import com.storysprout.api.story.StoryGenerator;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
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
public class GeminiStoryGenerator implements StoryGenerator {
    private static final Logger log = LoggerFactory.getLogger(GeminiStoryGenerator.class);

    private final ChatClient chatClient;
    private final GeminiStoryPromptBuilder promptBuilder;
    private final GeminiAiCallExecutor callExecutor;
    private final GeminiAiProperties properties;
    private final String model;

    public GeminiStoryGenerator(
        ChatClient chatClient,
        GeminiStoryPromptBuilder promptBuilder,
        GeminiAiCallExecutor callExecutor,
        GeminiAiProperties properties,
        @Value("${spring.ai.google.genai.chat.model:gemini-2.5-flash}") String model) {
        this.chatClient = chatClient;
        this.promptBuilder = promptBuilder;
        this.callExecutor = callExecutor;
        this.properties = properties;
        this.model = model;
    }

    @Override
    public StoryGenerationResult generate(StoryGenerationRequest request) {
        String prompt = promptBuilder.build(request);
        long started = System.nanoTime();
        int attempts = 0;

        while (attempts < properties.getMaxAttempts()) {
            attempts++;
            try {
                GeminiStoryResponse response = callExecutor.execute(
                    () -> chatClient.prompt()
                        .user(prompt)
                        .call()
                        .entity(GeminiStoryResponse.class, spec -> spec.useProviderStructuredOutput()),
                    properties.getTimeout().toMillis(),
                    TimeUnit.MILLISECONDS);

                StoryGenerationResult result = validate(response);
                log.info("AI story generation succeeded provider=google-genai model={} durationMs={} attempts={}",
                    model, elapsedMillis(started), attempts);
                return result;
            } catch (TimeoutException ex) {
                log.warn("AI story generation timed out provider=google-genai model={} durationMs={} attempts={}",
                    model, elapsedMillis(started), attempts);
                throw new StoryGenerationException("Story generation timed out");
            } catch (StoryGenerationException ex) {
                log.warn("AI story generation failed provider=google-genai model={} durationMs={} attempts={} category=validation",
                    model, elapsedMillis(started), attempts);
                throw ex;
            } catch (TransientAiException ex) {
                if (attempts < properties.getMaxAttempts()) {
                    log.warn("AI story generation transient failure provider=google-genai model={} attempts={} category=transient; retrying",
                        model, attempts);
                    continue;
                }
                log.warn("AI story generation failed provider=google-genai model={} durationMs={} attempts={} category=transient",
                    model, elapsedMillis(started), attempts);
                throw new StoryGenerationException("Story generation temporarily unavailable");
            } catch (NonTransientAiException ex) {
                log.warn("AI story generation failed provider=google-genai model={} durationMs={} attempts={} category=permanent",
                    model, elapsedMillis(started), attempts);
                throw new StoryGenerationException("Story generation is unavailable");
            } catch (RuntimeException ex) {
                log.warn("AI story generation failed provider=google-genai model={} durationMs={} attempts={} category=provider",
                    model, elapsedMillis(started), attempts);
                throw new StoryGenerationException("Story generation is unavailable");
            }
        }

        throw new StoryGenerationException("Story generation is unavailable");
    }

    private StoryGenerationResult validate(GeminiStoryResponse response) {
        if (response == null || response.title() == null || response.title().isBlank()) {
            throw new StoryGenerationException("AI response did not contain a valid title");
        }
        if (response.draft() == null || response.draft().isBlank()) {
            throw new StoryGenerationException("AI response did not contain a valid story draft");
        }
        return new StoryGenerationResult(response.title().trim(), response.draft().trim());
    }

    private long elapsedMillis(long started) {
        return Duration.ofNanos(System.nanoTime() - started).toMillis();
    }
}
