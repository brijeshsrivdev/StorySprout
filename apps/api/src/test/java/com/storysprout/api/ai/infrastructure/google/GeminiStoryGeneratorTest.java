package com.storysprout.api.ai.infrastructure.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.storysprout.api.story.StoryGenerationException;
import com.storysprout.api.story.StoryGenerationRequest;
import com.storysprout.api.story.StoryGenerationResult;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;

class GeminiStoryGeneratorTest {
    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec responseSpec;
    private GeminiStoryPromptBuilder promptBuilder;
    private GeminiAiCallExecutor callExecutor;
    private GeminiAiProperties properties;
    private GeminiStoryGenerator generator;
    private StoryGenerationRequest request;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        responseSpec = mock(ChatClient.CallResponseSpec.class);
        promptBuilder = new GeminiStoryPromptBuilder();
        callExecutor = mock(GeminiAiCallExecutor.class);
        properties = new GeminiAiProperties();
        properties.setTimeout(Duration.ofSeconds(1));
        properties.setMaxAttempts(2);
        generator = new GeminiStoryGenerator(chatClient, promptBuilder, callExecutor, properties, "gemini-2.5-flash");
        request = new StoryGenerationRequest("A rabbit learns to share.", "6_8", 3, "2D", "ENGLISH");

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
    }

    @Test
    void successfulResponseMapsToStoryGenerationResult() throws Exception {
        GeminiStoryResponse response = new GeminiStoryResponse("Sharing Rabbit", "A rabbit learns to share.");
        when(callExecutor.execute(any(), eq(1000L), eq(TimeUnit.MILLISECONDS))).thenReturn(response);

        StoryGenerationResult result = generator.generate(request);

        assertThat(result).isEqualTo(new StoryGenerationResult("Sharing Rabbit", "A rabbit learns to share."));
    }

    @Test
    void promptContainsAllStorySetupFields() {
        String prompt = promptBuilder.build(request);

        assertThat(prompt).contains("A rabbit learns to share.", "6_8", "3", "2D", "ENGLISH");
    }

    @Test
    void arbitraryTargetAgeIsRejected() {
        StoryGenerationRequest invalid = new StoryGenerationRequest("A rabbit", "13_15", 3, "2D", "ENGLISH");

        assertThatThrownBy(() -> generator.generate(invalid))
            .isInstanceOf(StoryGenerationException.class)
            .hasMessage("Unsupported target age for AI generation");
        verify(chatClient, never()).prompt();
    }

    @Test
    void blankTitleIsRejected() throws Exception {
        when(callExecutor.execute(any(), eq(1000L), eq(TimeUnit.MILLISECONDS)))
            .thenReturn(new GeminiStoryResponse("  ", "A valid draft."));

        assertThatThrownBy(() -> generator.generate(request))
            .isInstanceOf(StoryGenerationException.class)
            .hasMessage("AI response did not contain a valid title");
    }

    @Test
    void blankDraftIsRejected() throws Exception {
        when(callExecutor.execute(any(), eq(1000L), eq(TimeUnit.MILLISECONDS)))
            .thenReturn(new GeminiStoryResponse("Valid title", "  "));

        assertThatThrownBy(() -> generator.generate(request))
            .isInstanceOf(StoryGenerationException.class)
            .hasMessage("AI response did not contain a valid story draft");
    }

    @Test
    void nullStructuredResponseIsRejected() throws Exception {
        when(callExecutor.execute(any(), eq(1000L), eq(TimeUnit.MILLISECONDS))).thenReturn(null);

        assertThatThrownBy(() -> generator.generate(request))
            .isInstanceOf(StoryGenerationException.class)
            .hasMessage("AI response did not contain a valid title");
    }

    @Test
    void timeoutBecomesControlledFailure() throws Exception {
        when(callExecutor.execute(any(), eq(1000L), eq(TimeUnit.MILLISECONDS))).thenThrow(new TimeoutException("provider timeout"));

        assertThatThrownBy(() -> generator.generate(request))
            .isInstanceOf(StoryGenerationException.class)
            .hasMessage("Story generation timed out");
    }

    @Test
    void transientFailureRetriesOnceWithinBound() throws Exception {
        when(callExecutor.execute(any(), eq(1000L), eq(TimeUnit.MILLISECONDS)))
            .thenThrow(new TransientAiException("temporary"))
            .thenReturn(new GeminiStoryResponse("Sharing Rabbit", "A rabbit learns to share."));

        StoryGenerationResult result = generator.generate(request);

        assertThat(result.generatedTitle()).isEqualTo("Sharing Rabbit");
        verify(callExecutor, org.mockito.Mockito.times(2)).execute(any(), eq(1000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void permanentFailureIsNotRetried() throws Exception {
        when(callExecutor.execute(any(), eq(1000L), eq(TimeUnit.MILLISECONDS)))
            .thenThrow(new NonTransientAiException("invalid credentials"));

        assertThatThrownBy(() -> generator.generate(request))
            .isInstanceOf(StoryGenerationException.class)
            .hasMessage("Story generation is unavailable");
        verify(callExecutor).execute(any(), eq(1000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void transientFailureStopsAtConfiguredAttemptLimit() throws Exception {
        when(callExecutor.execute(any(), eq(1000L), eq(TimeUnit.MILLISECONDS)))
            .thenThrow(new TransientAiException("temporary"));

        assertThatThrownBy(() -> generator.generate(request))
            .isInstanceOf(StoryGenerationException.class)
            .hasMessage("Story generation temporarily unavailable");
        verify(callExecutor, org.mockito.Mockito.times(2)).execute(any(), eq(1000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void providerFailureDoesNotLeakProviderMessage() throws Exception {
        when(callExecutor.execute(any(), eq(1000L), eq(TimeUnit.MILLISECONDS)))
            .thenThrow(new RuntimeException("Authorization: secret-api-key-123"));

        assertThatThrownBy(() -> generator.generate(request))
            .isInstanceOf(StoryGenerationException.class)
            .hasMessage("Story generation is unavailable")
            .hasMessageNotContaining("secret-api-key-123");
    }
}
