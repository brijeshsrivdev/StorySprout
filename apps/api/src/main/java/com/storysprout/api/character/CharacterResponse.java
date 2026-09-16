package com.storysprout.api.character;
import java.time.Instant;
import java.util.UUID;
public record CharacterResponse(UUID id,UUID projectId,String name,String roleDescription,CharacterCategory category,String visualDescription,String personality,Instant createdAt,Instant updatedAt){static CharacterResponse from(Character c){return new CharacterResponse(c.id(),c.projectId(),c.name(),c.roleDescription(),c.category(),c.visualDescription(),c.personality(),c.createdAt(),c.updatedAt());}}
