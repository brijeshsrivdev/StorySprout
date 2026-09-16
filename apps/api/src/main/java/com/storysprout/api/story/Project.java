package com.storysprout.api.story;

import java.time.Instant;
import java.util.UUID;

public record Project(UUID id, String name, ProjectStatus status, Instant createdAt, Instant updatedAt) {}
