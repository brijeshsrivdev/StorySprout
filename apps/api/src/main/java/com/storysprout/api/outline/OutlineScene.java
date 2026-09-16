package com.storysprout.api.outline;

import java.time.Instant;
import java.util.UUID;

public record OutlineScene(
    UUID id,
    UUID storyId,
    int orderIndex,
    String title,
    String summary,
    int durationSeconds,
    Instant createdAt,
    Instant updatedAt) {}
