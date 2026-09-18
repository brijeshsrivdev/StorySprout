package com.storysprout.api.editor;

import tools.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

public record Composition(UUID id, UUID projectId, UUID outlineSceneId, JsonNode compositionJson, long version, Instant createdAt, Instant updatedAt) {}
