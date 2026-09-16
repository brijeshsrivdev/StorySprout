package com.storysprout.api.outline;

import java.util.List;

public record OutlineResponse(StoryOutlineStoryResponse story, List<OutlineSceneResponse> scenes, int targetDurationSeconds, int plannedDurationSeconds, int varianceSeconds) {
    public OutlineResponse { scenes = List.copyOf(scenes); }
    static OutlineResponse from(OutlineSnapshot snapshot) {
        return new OutlineResponse(StoryOutlineStoryResponse.from(snapshot.story()), snapshot.scenes().stream().map(OutlineSceneResponse::from).toList(), snapshot.targetDurationSeconds(), snapshot.plannedDurationSeconds(), snapshot.varianceSeconds());
    }
}
