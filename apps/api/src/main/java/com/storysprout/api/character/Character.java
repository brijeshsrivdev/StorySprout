package com.storysprout.api.character;

import java.time.Instant;
import java.util.UUID;

public record Character(UUID id, UUID projectId, String name, String roleDescription, CharacterCategory category,
                        String visualDescription, String personality, Instant createdAt, Instant updatedAt) {}
