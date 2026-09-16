package com.storysprout.api.ai.infrastructure.google;

import static org.assertj.core.api.Assertions.assertThat;

import com.storysprout.api.story.DeterministicStoryGenerator;
import com.storysprout.api.story.StoryGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class StoryGeneratorSelectionTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(DeterministicStoryGenerator.class, GeminiStoryGenerator.class, GeminiStoryPromptBuilder.class, GeminiAiConfiguration.class, GeminiAiCallExecutor.class);

    @Test
    void fakeProviderHasExactlyOneStoryGeneratorWithoutGemini() {
        contextRunner.withPropertyValues("storysprout.ai.provider=fake").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).getBeans(StoryGenerator.class).hasSize(1);
            assertThat(context).hasSingleBean(DeterministicStoryGenerator.class);
        });
    }
}
