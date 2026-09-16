package com.storysprout.api.ai.infrastructure.google;

import com.storysprout.api.story.StoryGenerationException;
import com.storysprout.api.story.StoryGenerationRequest;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class GeminiStoryPromptBuilder {
    private static final Set<String> TARGET_AGES = Set.of("3_5", "6_8", "9_12");
    private static final Set<Integer> DURATIONS = Set.of(1, 3, 5);
    private static final Set<String> VISUAL_STYLES = Set.of("2D", "3D", "HYBRID");
    private static final Set<String> LANGUAGES = Set.of("ENGLISH", "HINDI");

    public String build(StoryGenerationRequest request) {
        validate(request);
        return "Create a short children’s story using the exact creator setup below.\n"
            + "Return only the requested structured fields; do not add scenes, characters, assets, dialogue metadata, timelines, or rendering instructions.\n\n"
            + "Story idea: " + request.idea().trim() + "\n"
            + "Target age: " + request.targetAge() + "\n"
            + "Duration minutes: " + request.durationMinutes() + "\n"
            + "Visual style: " + request.visualStyle() + "\n"
            + "Language: " + request.language() + "\n\n"
            + "The generated title should be concise and suitable for children. The generated draft should be a complete, age-appropriate story matching the requested duration and language.";
    }

    private void validate(StoryGenerationRequest request) {
        if (request == null || request.idea() == null || request.idea().isBlank()) {
            throw new StoryGenerationException("Story idea is required for AI generation");
        }
        if (!TARGET_AGES.contains(request.targetAge())) {
            throw new StoryGenerationException("Unsupported target age for AI generation");
        }
        if (!DURATIONS.contains(request.durationMinutes())) {
            throw new StoryGenerationException("Unsupported duration for AI generation");
        }
        if (!VISUAL_STYLES.contains(request.visualStyle())) {
            throw new StoryGenerationException("Unsupported visual style for AI generation");
        }
        if (!LANGUAGES.contains(request.language())) {
            throw new StoryGenerationException("Unsupported language for AI generation");
        }
    }
}
