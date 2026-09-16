package com.storysprout.api.outline;

public record StoryOutlineGenerationRequest(
    String idea,
    String draft,
    String targetAge,
    int durationMinutes,
    String visualStyle,
    String language) {}
