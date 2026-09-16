package com.storysprout.api.story;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "storysprout.ai.provider", havingValue = "fake", matchIfMissing = true)
public class DeterministicStoryGenerator implements StoryGenerator {
    @Override
    public StoryGenerationResult generate(StoryGenerationRequest request) {
        String title = "A Little Sharing Adventure";
        String draft = "Once upon a time, a little friend discovered that sharing can make an ordinary day feel special. "
            + "Together with a new friend, they learned to take turns, help each other, and celebrate small acts of kindness. "
            + "By the end of the day, both friends discovered that kindness grows when it is shared.";
        return new StoryGenerationResult(title, draft);
    }
}
