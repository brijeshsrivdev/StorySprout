package com.storysprout.api.ai.infrastructure.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.storysprout.api.story.Project;
import com.storysprout.api.story.ProjectRepository;
import com.storysprout.api.story.ProjectStatus;
import com.storysprout.api.story.Story;
import com.storysprout.api.story.StoryCreateRequest;
import com.storysprout.api.story.StoryCreationMode;
import com.storysprout.api.story.StoryCreationResult;
import com.storysprout.api.story.StoryGenerationStatus;
import com.storysprout.api.story.StoryRepository;
import com.storysprout.api.story.StoryService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

class GeminiStoryServiceTest {
    @Test
    void storyServiceUsesGeminiAdapterWithoutProviderSpecificContract() throws Exception {
        ProjectRepository projects = mock(ProjectRepository.class);
        StoryRepository stories = mock(StoryRepository.class);
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        GeminiAiCallExecutor executor = mock(GeminiAiCallExecutor.class);
        GeminiAiProperties properties = new GeminiAiProperties();
        GeminiStoryPromptBuilder promptBuilder = new GeminiStoryPromptBuilder();
        GeminiStoryGenerator generator = new GeminiStoryGenerator(chatClient, promptBuilder, executor, properties, "gemini-2.5-flash");
        StoryService service = new StoryService(projects, stories, generator);
        UUID projectId = UUID.randomUUID();
        UUID storyId = UUID.randomUUID();

        when(projects.findById(projectId)).thenReturn(Optional.of(new Project(projectId, "Project", ProjectStatus.DRAFT, null, null)));
        when(stories.insert(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Story completed = new Story(storyId, projectId, "Sharing Rabbit", "A rabbit learns to share.", "6_8", 3, "2D", "ENGLISH", StoryCreationMode.AI, StoryGenerationStatus.COMPLETED, "A rabbit learns to share.", null, null);
        when(stories.updateGeneration(any(UUID.class), eq(StoryGenerationStatus.COMPLETED), eq("Sharing Rabbit"), eq("A rabbit learns to share."))).thenReturn(completed);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(eq(GeminiStoryResponse.class), any())).thenReturn(new GeminiStoryResponse("Sharing Rabbit", "A rabbit learns to share."));
        when(executor.execute(any(), any(Long.class), any())).thenAnswer(invocation -> {
            java.util.concurrent.Callable<?> operation = invocation.getArgument(0);
            return operation.call();
        });

        StoryCreationResult result = service.createStory(projectId,
            new StoryCreateRequest(StoryCreationMode.AI, "A rabbit learns to share.", "6_8", 3, "2D", "ENGLISH"));

        assertThat(result.story()).isEqualTo(completed);
        assertThat(result.generationFailure()).isNull();
    }
}
