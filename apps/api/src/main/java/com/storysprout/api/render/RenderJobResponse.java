package com.storysprout.api.render;

import java.time.Instant;
import java.util.UUID;

public record RenderJobResponse(
        UUID id,
        UUID storyId,
        UUID outlineSceneId,
        UUID compositionId,
        long compositionVersion,
        RenderJobStatus status,
        int progress,
        int attempt,
        String failureCode,
        String failureMessage,
        String artifactUrl,
        Long artifactSize,
        Instant createdAt,
        Instant updatedAt) {
}
