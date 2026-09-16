package com.storysprout.api.outline;

import com.storysprout.api.story.Story;
import java.time.Instant;
import java.util.UUID;

public record StoryOutlineStoryResponse(UUID id, UUID projectId, String title, String idea, String targetAge, int durationMinutes, String visualStyle, String language, String creationMode, String generationStatus, String draftContent, Instant createdAt, Instant updatedAt) {
    static StoryOutlineStoryResponse from(Story story) {
        return new StoryOutlineStoryResponse(story.id(), story.projectId(), story.title(), story.idea(), story.targetAge(), story.durationMinutes(), story.visualStyle(), story.language(), story.creationMode().name(), story.generationStatus().name(), story.draftContent(), story.createdAt(), story.updatedAt());
    }
}
