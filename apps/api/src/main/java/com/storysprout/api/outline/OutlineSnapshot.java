package com.storysprout.api.outline;

import com.storysprout.api.story.Story;
import java.util.List;

public record OutlineSnapshot(Story story, List<OutlineScene> scenes, int targetDurationSeconds, int plannedDurationSeconds, int varianceSeconds) {
    public OutlineSnapshot { scenes = List.copyOf(scenes); }
}
