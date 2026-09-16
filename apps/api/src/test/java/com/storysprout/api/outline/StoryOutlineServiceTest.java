package com.storysprout.api.outline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.storysprout.api.story.Story;
import com.storysprout.api.story.StoryCreationMode;
import com.storysprout.api.story.StoryGenerationStatus;
import com.storysprout.api.story.StoryRepository;
import com.storysprout.api.story.StoryValidationException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class StoryOutlineServiceTest {
    @Mock StoryRepository stories;
    @Mock OutlineSceneRepository scenes;
    @Mock StoryOutlineGenerator generator;
    StoryOutlineService service;
    UUID storyId;
    Story story;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new StoryOutlineService(stories, scenes, generator);
        storyId = UUID.randomUUID();
        Instant now = Instant.now();
        story = new Story(storyId, UUID.randomUUID(), "Rabbit", "A rabbit shares.", "6_8", 3, "2D", "ENGLISH", StoryCreationMode.BLANK, StoryGenerationStatus.NOT_REQUESTED, null, now, now);
        when(stories.findById(storyId)).thenReturn(java.util.Optional.of(story));
    }

    @Test void varianceIsCalculatedWithoutRequiringTargetEquality() {
        OutlineScene a = scene(1, 40);
        OutlineScene b = scene(2, 55);
        OutlineScene c = scene(3, 50);
        OutlineScene d = scene(4, 45);
        when(scenes.findByStoryId(storyId)).thenReturn(List.of(a, b, c, d));

        OutlineSnapshot snapshot = service.getOutline(storyId);

        assertThat(snapshot.targetDurationSeconds()).isEqualTo(180);
        assertThat(snapshot.plannedDurationSeconds()).isEqualTo(190);
        assertThat(snapshot.varianceSeconds()).isEqualTo(10);
    }

    @Test void addDoesNotModifyExistingDurations() {
        when(scenes.findByStoryId(storyId)).thenReturn(List.of(scene(1, 60), scene(2, 60)));
        OutlineScene created = scene(3, 10);
        when(scenes.insert(any())).thenReturn(created);

        service.create(storyId, new CreateOutlineSceneRequest("Third", "Third scene", 10));

        verify(scenes).insert(any(OutlineScene.class));
        verify(scenes, never()).update(any(), any(), any(), any(), any());
    }

    @Test void deleteDoesNotModifyRemainingDurations() {
        UUID sceneId = UUID.randomUUID();
        when(scenes.findByIdAndStoryId(sceneId, storyId)).thenReturn(java.util.Optional.of(scene(2, 70)));
        service.delete(storyId, sceneId);
        verify(scenes).deleteAndCompact(sceneId, storyId);
    }

    @Test void reorderOnlyPassesOrderIds() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        when(scenes.findByStoryId(storyId)).thenReturn(List.of(scene(first, 1, 40), scene(second, 2, 80)));
        service.reorder(storyId, new ReorderOutlineScenesRequest(List.of(second, first)));
        verify(scenes).reorder(storyId, List.of(second, first));
    }

    @Test void zeroAndNegativeDurationsAreRejected() {
        assertThatThrownBy(() -> service.create(storyId, new CreateOutlineSceneRequest("A", "B", 0)))
            .isInstanceOf(StoryValidationException.class);
        assertThatThrownBy(() -> service.create(storyId, new CreateOutlineSceneRequest("A", "B", -1)))
            .isInstanceOf(StoryValidationException.class);
    }

    @Test void generatedDurationsDoNotNeedToMatchTarget() {
        when(scenes.findByStoryId(storyId)).thenReturn(List.of());
        when(generator.generate(any())).thenReturn(new StoryOutlineGenerationResult(List.of(
            new GeneratedOutlineScene("One", "First", 40),
            new GeneratedOutlineScene("Two", "Second", 55),
            new GeneratedOutlineScene("Three", "Third", 50),
            new GeneratedOutlineScene("Four", "Fourth", 45))));
        when(scenes.insert(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OutlineSnapshot result = service.generate(storyId);

        assertThat(result.plannedDurationSeconds()).isEqualTo(190);
        assertThat(result.varianceSeconds()).isEqualTo(10);
        verify(generator).generate(any());
    }

    private OutlineScene scene(int order, int duration) { return scene(UUID.randomUUID(), order, duration); }
    private OutlineScene scene(UUID id, int order, int duration) { return new OutlineScene(id, storyId, order, "Scene " + order, "Summary", duration, Instant.now(), Instant.now()); }
}
