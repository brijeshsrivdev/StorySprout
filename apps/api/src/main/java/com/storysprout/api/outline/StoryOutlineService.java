package com.storysprout.api.outline;

import com.storysprout.api.story.ProjectNotFoundException;
import com.storysprout.api.story.Story;
import com.storysprout.api.story.StoryRepository;
import com.storysprout.api.story.StoryValidationException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoryOutlineService {
    private final StoryRepository stories;
    private final OutlineSceneRepository scenes;
    private final StoryOutlineGenerator generator;

    public StoryOutlineService(StoryRepository stories, OutlineSceneRepository scenes, StoryOutlineGenerator generator) {
        this.stories = stories;
        this.scenes = scenes;
        this.generator = generator;
    }

    @Transactional(readOnly = true)
    public Story getStory(UUID projectId, UUID storyId) {
        Story story = stories.findById(storyId).orElseThrow(() -> new OutlineNotFoundException("Story not found"));
        if (!story.projectId().equals(projectId)) throw new OutlineNotFoundException("Story not found");
        return story;
    }

    @Transactional(readOnly = true)
    public List<Story> listStories(UUID projectId) {
        return stories.findAllByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public OutlineSnapshot getOutline(UUID storyId) {
        requireStory(storyId);
        return snapshot(storyId);
    }

    @Transactional
    public OutlineSnapshot generate(UUID storyId) {
        Story story = requireStory(storyId);
        if (!scenes.findByStoryId(storyId).isEmpty()) throw new OutlineConflictException("An outline already exists");
        StoryOutlineGenerationResult generated;
        try {
            generated = generator.generate(new StoryOutlineGenerationRequest(story.idea(), story.draftContent(), story.targetAge(), story.durationMinutes(), story.visualStyle(), story.language()));
            validateGenerated(generated);
        } catch (RuntimeException ex) {
            if (ex instanceof OutlineGenerationException) throw ex;
            throw new OutlineGenerationException("Outline generation failed");
        }
        Instant now = Instant.now();
        int order = 1;
        for (GeneratedOutlineScene scene : generated.scenes()) {
            scenes.insert(new OutlineScene(UUID.randomUUID(), storyId, order++, scene.title().trim(), scene.summary().trim(), scene.durationSeconds(), now, now));
        }
        return snapshot(storyId);
    }

    @Transactional
    public OutlineScene create(UUID storyId, CreateOutlineSceneRequest request) {
        requireStory(storyId);
        validateScene(request.title(), request.summary(), request.durationSeconds());
        int next = scenes.findByStoryId(storyId).size() + 1;
        Instant now = Instant.now();
        return scenes.insert(new OutlineScene(UUID.randomUUID(), storyId, next, request.title().trim(), request.summary().trim(), request.durationSeconds(), now, now));
    }

    @Transactional
    public OutlineScene update(UUID storyId, UUID sceneId, UpdateOutlineSceneRequest request) {
        requireScene(storyId, sceneId);
        validateScene(request.title(), request.summary(), request.durationSeconds());
        return scenes.update(sceneId, storyId, request.title().trim(), request.summary().trim(), request.durationSeconds());
    }

    @Transactional
    public void delete(UUID storyId, UUID sceneId) {
        requireScene(storyId, sceneId);
        scenes.deleteAndCompact(sceneId, storyId);
    }

    @Transactional
    public List<OutlineScene> reorder(UUID storyId, ReorderOutlineScenesRequest request) {
        requireStory(storyId);
        List<OutlineScene> current = scenes.findByStoryId(storyId);
        if (request.sceneIds() == null || request.sceneIds().size() != current.size() || new HashSet<>(request.sceneIds()).size() != current.size() || !new HashSet<>(request.sceneIds()).equals(current.stream().map(OutlineScene::id).collect(java.util.stream.Collectors.toSet()))) {
            throw new StoryValidationException("Reorder must contain each current scene exactly once");
        }
        scenes.reorder(storyId, request.sceneIds());
        return scenes.findByStoryId(storyId);
    }

    private Story requireStory(UUID storyId) {
        return stories.findById(storyId).orElseThrow(() -> new OutlineNotFoundException("Story not found"));
    }

    private OutlineScene requireScene(UUID storyId, UUID sceneId) {
        requireStory(storyId);
        return scenes.findByIdAndStoryId(sceneId, storyId).orElseThrow(() -> new OutlineNotFoundException("Outline scene not found"));
    }

    private OutlineSnapshot snapshot(UUID storyId) {
        Story story = requireStory(storyId);
        List<OutlineScene> sceneList = scenes.findByStoryId(storyId);
        int planned = sceneList.stream().mapToInt(OutlineScene::durationSeconds).sum();
        return new OutlineSnapshot(story, sceneList, story.durationMinutes() * 60, planned, planned - story.durationMinutes() * 60);
    }

    private void validateGenerated(StoryOutlineGenerationResult result) {
        if (result == null || result.scenes() == null || result.scenes().isEmpty()) throw new OutlineGenerationException("AI returned no scenes");
        result.scenes().forEach(scene -> validateScene(scene.title(), scene.summary(), scene.durationSeconds()));
    }

    private void validateScene(String title, String summary, Integer durationSeconds) {
        if (title == null || title.isBlank() || title.trim().length() > 200) throw new StoryValidationException("Scene title is required and must be at most 200 characters");
        if (summary == null || summary.isBlank() || summary.trim().length() > 2000) throw new StoryValidationException("Scene summary is required and must be at most 2000 characters");
        if (durationSeconds == null || durationSeconds < 1) throw new StoryValidationException("Scene duration must be a positive integer number of seconds");
    }
}
