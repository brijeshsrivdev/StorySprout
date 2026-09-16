package com.storysprout.api.ai.infrastructure.google;

import com.storysprout.api.outline.StoryOutlineGenerationRequest;
import org.springframework.stereotype.Component;

@Component
public class GeminiStoryOutlinePromptBuilder {
    public String build(StoryOutlineGenerationRequest request) {
        String draft = request.draft() == null || request.draft().isBlank() ? "No generated draft is available; use the story idea." : request.draft();
        return """
            Create a concise children’s story outline as a sequence of scenes.
            Return only the requested structured response.
            Treat duration as an approximate planning target, not an exact budget.
            Produce positive integer durationSeconds values. Do not force the total to equal the target.

            Story idea: %s
            Story draft: %s
            Target age: %s
            Story target duration: %d minutes (%d seconds)
            Visual style: %s
            Language: %s

            Each scene needs a short title, a useful narrative summary, and a sensible approximate duration in whole seconds.
            The outline should be suitable for a future Scene Setup stage. Do not include characters, assets, animation, camera instructions, timeline clips, or frame-accurate timing.
            """.formatted(request.idea(), draft, request.targetAge(), request.durationMinutes(), request.durationMinutes() * 60, request.visualStyle(), request.language());
    }
}
