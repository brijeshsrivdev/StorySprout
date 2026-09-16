package com.storysprout.api.outline;

import java.util.List;

public record StoryOutlineGenerationResult(List<GeneratedOutlineScene> scenes) {
    public StoryOutlineGenerationResult {
        scenes = List.copyOf(scenes);
    }
}
