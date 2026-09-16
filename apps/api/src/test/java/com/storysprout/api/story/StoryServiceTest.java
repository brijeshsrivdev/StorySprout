package com.storysprout.api.story;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StoryServiceTest {
    private ProjectRepository projects;
    private StoryRepository stories;
    private StoryGenerator generator;
    private StoryService service;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        projects = mock(ProjectRepository.class);
        stories = mock(StoryRepository.class);
        generator = mock(StoryGenerator.class);
        service = new StoryService(projects, stories, generator);
        projectId = UUID.randomUUID();
        when(projects.findById(projectId)).thenReturn(Optional.of(new Project(projectId, "Untitled Story", ProjectStatus.DRAFT, null, null)));
    }

    @Test
    void blankStoryPersistsWithoutCallingGenerator() {
        StoryCreateRequest request = request(StoryCreationMode.BLANK);
        Story saved = story(StoryGenerationStatus.NOT_REQUESTED, null, null);
        when(stories.insert(any())).thenReturn(saved);

        StoryCreationResult result = service.createStory(projectId, request);

        assertThat(result.story().generationStatus()).isEqualTo(StoryGenerationStatus.NOT_REQUESTED);
        verify(generator, never()).generate(any());
        verify(stories).insert(any());
    }

    @Test
    void aiStoryUsesExactSetupAndPersistsGeneratedContent() {
        StoryCreateRequest request = request(StoryCreationMode.AI);
        Story generating = story(StoryGenerationStatus.GENERATING, null, null);
        Story completed = story(StoryGenerationStatus.COMPLETED, "A Sharing Rabbit", "A rabbit learns to share.");
        when(stories.insert(any())).thenReturn(generating);
        when(stories.updateGeneration(any(), eq(StoryGenerationStatus.COMPLETED), eq("A Sharing Rabbit"), eq("A rabbit learns to share."))).thenReturn(completed);
        when(generator.generate(any())).thenReturn(new StoryGenerationResult("A Sharing Rabbit", "A rabbit learns to share."));

        StoryCreationResult result = service.createStory(projectId, request);

        assertThat(result.story()).isEqualTo(completed);
        verify(generator).generate(new StoryGenerationRequest("A rabbit learns to share.", "6_8", 3, "2D", "ENGLISH"));
    }

    @Test
    void aiFailurePreservesSetupAndMarksStoryFailed() {
        StoryCreateRequest request = request(StoryCreationMode.AI);
        Story generating = story(StoryGenerationStatus.GENERATING, null, null);
        Story failed = story(StoryGenerationStatus.FAILED, null, null);
        when(stories.insert(any())).thenReturn(generating);
        when(generator.generate(any())).thenThrow(new StoryGenerationException("generation unavailable"));
        when(stories.updateGeneration(any(), eq(StoryGenerationStatus.FAILED), isNull(), isNull())).thenReturn(failed);

        StoryCreationResult result = service.createStory(projectId, request);

        assertThat(result.story()).isEqualTo(failed);
        assertThat(result.generationFailure()).isEqualTo("generation unavailable");
    }

    @Test
    void missingProjectIsRejected() {
        when(projects.findById(projectId)).thenReturn(Optional.empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.createStory(projectId, request(StoryCreationMode.BLANK)))
            .isInstanceOf(ProjectNotFoundException.class);
        verify(stories, never()).insert(any());
    }

    private StoryCreateRequest request(StoryCreationMode mode) {
        return new StoryCreateRequest(mode, "A rabbit learns to share.", "6_8", 3, "2D", "ENGLISH");
    }

    private Story story(StoryGenerationStatus status, String title, String draft) {
        return new Story(UUID.randomUUID(), projectId, title == null ? "Untitled Story" : title, "A rabbit learns to share.", "6_8", 3, "2D", "ENGLISH", StoryCreationMode.AI, status, draft, null, null);
    }
}
