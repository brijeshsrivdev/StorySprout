package com.storysprout.api.ai.infrastructure.google;
import com.storysprout.api.character.CharacterCategory;
public record GeminiCharacterResponse(String name,String roleDescription,CharacterCategory category,String visualDescription,String personality) {}
