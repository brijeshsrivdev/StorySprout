package com.storysprout.api.character;

public final class CharacterRequests {
    private CharacterRequests() {}
    public record Create(String name, String roleDescription, CharacterCategory category, String visualDescription, String personality) {}
    public record Update(String name, String roleDescription, CharacterCategory category, String visualDescription, String personality) {}
    public record Generate(String idea, String storyId) {}
}
