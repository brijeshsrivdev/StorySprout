package com.storysprout.api.story;

public record StoryGenerationRequest(String idea, String targetAge, int durationMinutes, String visualStyle, String language) {}
