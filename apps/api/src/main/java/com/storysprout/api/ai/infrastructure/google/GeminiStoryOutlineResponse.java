package com.storysprout.api.ai.infrastructure.google;

import com.storysprout.api.outline.GeneratedOutlineScene;
import java.util.List;

public record GeminiStoryOutlineResponse(List<GeneratedOutlineScene> scenes) {}
