package com.storysprout.api.ai.infrastructure.google;

import com.storysprout.api.character.CharacterGeneration;
import com.storysprout.api.character.CharacterGenerationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="storysprout.ai.provider",havingValue="gemini")
public class GeminiCharacterGenerator implements CharacterGeneration.Generator {
    private final ChatClient chatClient; private final GeminiCharacterPromptBuilder promptBuilder; private final GeminiAiCallExecutor executor; private final GeminiAiProperties properties; private final String model;
    public GeminiCharacterGenerator(ChatClient chatClient,GeminiCharacterPromptBuilder promptBuilder,GeminiAiCallExecutor executor,GeminiAiProperties properties,@Value("${spring.ai.google.genai.chat.model:gemini-2.5-flash}") String model){this.chatClient=chatClient;this.promptBuilder=promptBuilder;this.executor=executor;this.properties=properties;this.model=model;}
    @Override public CharacterGeneration.Result generate(CharacterGeneration.Request request){String prompt=promptBuilder.build(request);for(int attempt=1;attempt<=properties.getMaxAttempts();attempt++){try{GeminiCharacterResponse r=executor.execute(()->chatClient.prompt().user(prompt).call().entity(GeminiCharacterResponse.class,s->s.useProviderStructuredOutput()),properties.getTimeout().toMillis(),TimeUnit.MILLISECONDS);if(r==null||r.name()==null||r.name().isBlank()||r.roleDescription()==null||r.roleDescription().isBlank()||r.category()==null||r.visualDescription()==null||r.visualDescription().isBlank()||r.personality()==null)throw new CharacterGenerationException("AI returned an invalid character suggestion");return new CharacterGeneration.Result(r.name().trim(),r.roleDescription().trim(),r.category(),r.visualDescription().trim(),r.personality().trim());}catch(TimeoutException e){throw new CharacterGenerationException("Character generation timed out");}catch(CharacterGenerationException e){throw e;}catch(TransientAiException e){if(attempt<properties.getMaxAttempts())continue;throw new CharacterGenerationException("Character generation temporarily unavailable");}catch(NonTransientAiException e){throw new CharacterGenerationException("Character generation is unavailable");}catch(RuntimeException e){throw new CharacterGenerationException("Character generation is unavailable");}}throw new CharacterGenerationException("Character generation is unavailable");}
}
