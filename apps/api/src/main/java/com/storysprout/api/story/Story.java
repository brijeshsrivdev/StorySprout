package com.storysprout.api.story;

import java.time.Instant;
import java.util.UUID;

public record Story(
    UUID id, UUID projectId, String title, String idea, String targetAge, int durationMinutes,
    String visualStyle, String language, StoryCreationMode creationMode,
    StoryGenerationStatus generationStatus, String draftContent, Instant createdAt, Instant updatedAt) {}
