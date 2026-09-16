package com.storysprout.api.character;

public final class CharacterGeneration {
    private CharacterGeneration() {}
    public record Request(String idea, String storyTitle, String storyIdea, String storyDraft, String targetAge, String language, String visualStyle) {}
    public record Result(String name, String roleDescription, CharacterCategory category, String visualDescription, String personality) {}
    public interface Generator { Result generate(Request request); }
}
