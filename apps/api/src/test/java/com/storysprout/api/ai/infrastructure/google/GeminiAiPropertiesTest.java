package com.storysprout.api.ai.infrastructure.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class GeminiAiPropertiesTest {
    @Test
    void defaultsUseBoundedTimeoutAndRetryBudget() {
        GeminiAiProperties properties = new GeminiAiProperties();

        assertThat(properties.getTimeout()).hasSeconds(20);
        assertThat(properties.getMaxAttempts()).isEqualTo(2);
    }

    @Test
    void retryBudgetCannotBecomeUnbounded() {
        GeminiAiProperties properties = new GeminiAiProperties();

        assertThatThrownBy(() -> properties.setMaxAttempts(3))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
