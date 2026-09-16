package com.storysprout.api.story;

import java.time.Instant;
import java.util.UUID;

public record StoryResponse(UUID id, UUID projectId, String title, String idea, String targetAge, int durationMinutes, String visualStyle, String language, StoryCreationMode creationMode, StoryGenerationStatus generationStatus, String draftContent, Instant createdAt, Instant updatedAt, String continuationPath) {
    static StoryResponse from(Story s) { return new StoryResponse(s.id(), s.projectId(), s.title(), s.idea(), s.targetAge(), s.durationMinutes(), s.visualStyle(), s.language(), s.creationMode(), s.generationStatus(), s.draftContent(), s.createdAt(), s.updatedAt(), "/dashboard"); }
}
