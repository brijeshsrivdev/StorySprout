package com.storysprout.api.outline;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "storysprout.ai.provider", havingValue = "fake", matchIfMissing = true)
public class DeterministicStoryOutlineGenerator implements StoryOutlineGenerator {
    @Override
    public StoryOutlineGenerationResult generate(StoryOutlineGenerationRequest request) {
        int target = request.durationMinutes() * 60;
        int first = Math.max(1, target / 4);
        int second = Math.max(1, target / 4);
        int third = Math.max(1, target / 4);
        int fourth = Math.max(1, target - first - second - third);
        return new StoryOutlineGenerationResult(List.of(
            new GeneratedOutlineScene("A Small Problem", "The main character discovers a small problem and decides to help.", first),
            new GeneratedOutlineScene("A Helpful Friend", "A friend joins the journey and offers a new idea.", second),
            new GeneratedOutlineScene("Trying Together", "The characters try their idea and learn from what happens.", third),
            new GeneratedOutlineScene("A Kind Ending", "The characters solve the problem and share what they learned.", fourth)
        ));
    }
}
