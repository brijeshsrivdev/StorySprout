package com.storysprout.api.outline;

import java.time.Instant;
import java.util.UUID;

public record OutlineSceneResponse(UUID id, UUID storyId, int orderIndex, String title, String summary, int durationSeconds, Instant createdAt, Instant updatedAt) {
    static OutlineSceneResponse from(OutlineScene scene) {
        return new OutlineSceneResponse(scene.id(), scene.storyId(), scene.orderIndex(), scene.title(), scene.summary(), scene.durationSeconds(), scene.createdAt(), scene.updatedAt());
    }
}
