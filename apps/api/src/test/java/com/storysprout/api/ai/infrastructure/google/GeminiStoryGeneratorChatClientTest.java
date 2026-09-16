package com.storysprout.api.ai.infrastructure.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.storysprout.api.story.StoryGenerationRequest;
import com.storysprout.api.story.StoryGenerationResult;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.ai.chat.client.ChatClient;

class GeminiStoryGeneratorChatClientTest {
    @Test
    void mapsTypedChatClientResponseAndSendsCompletePrompt() throws Exception {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        GeminiAiCallExecutor executor = mock(GeminiAiCallExecutor.class);
        GeminiAiProperties properties = new GeminiAiProperties();
        GeminiStoryPromptBuilder promptBuilder = new GeminiStoryPromptBuilder();
        StoryGenerationRequest request = new StoryGenerationRequest("A rabbit learns to share.", "6_8", 3, "2D", "ENGLISH");

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(eq(GeminiStoryResponse.class), any())).thenReturn(
            new GeminiStoryResponse("Sharing Rabbit", "A rabbit learns to share."));
        when(executor.execute(any(), any(Long.class), any(TimeUnit.class))).thenAnswer(invocation -> {
            Callable<?> operation = invocation.getArgument(0);
            return operation.call();
        });

        GeminiStoryGenerator generator = new GeminiStoryGenerator(
            chatClient, promptBuilder, executor, properties, "gemini-2.5-flash");

        StoryGenerationResult result = generator.generate(request);

        assertThat(result).isEqualTo(new StoryGenerationResult("Sharing Rabbit", "A rabbit learns to share."));
        ArgumentMatcher<String> completePrompt = prompt -> prompt != null
            && prompt.contains("A rabbit learns to share.")
            && prompt.contains("6_8")
            && prompt.contains("3")
            && prompt.contains("2D")
            && prompt.contains("ENGLISH");
        verify(requestSpec).user(org.mockito.ArgumentMatchers.argThat(completePrompt));
        verify(responseSpec).entity(eq(GeminiStoryResponse.class), any());
    }
}
