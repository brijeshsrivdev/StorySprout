package com.storysprout.api.ai.infrastructure.google;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storysprout.ai.gemini")
public class GeminiAiProperties {
    private Duration timeout = Duration.ofSeconds(20);
    private int maxAttempts = 2;

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("Gemini timeout must be positive");
        }
        this.timeout = timeout;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        if (maxAttempts < 1 || maxAttempts > 2) {
            throw new IllegalArgumentException("Gemini max attempts must be between 1 and 2");
        }
        this.maxAttempts = maxAttempts;
    }
}
