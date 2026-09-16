package com.storysprout.api.ai.infrastructure.google;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "storysprout.ai.provider", havingValue = "gemini")
@EnableConfigurationProperties(GeminiAiProperties.class)
public class GeminiAiConfiguration {
    @Bean
    ChatClient geminiChatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
