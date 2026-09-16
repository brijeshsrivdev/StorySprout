package com.storysprout.api.story;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(UUID id, String name, ProjectStatus status, Instant createdAt, Instant updatedAt) {
    static ProjectResponse from(Project p) { return new ProjectResponse(p.id(), p.name(), p.status(), p.createdAt(), p.updatedAt()); }
}
