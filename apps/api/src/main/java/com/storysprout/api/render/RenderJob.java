package com.storysprout.api.render;

import java.time.Instant;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

public record RenderJob(
        UUID id,
        UUID storyId,
        UUID outlineSceneId,
        UUID compositionId,
        long compositionVersion,
        JsonNode snapshot,
        RenderJobStatus status,
        int progress,
        int attempt,
        String failureCode,
        String failureMessage,
        String artifactKey,
        Long artifactSize,
        Instant createdAt,
        Instant updatedAt) {
}
