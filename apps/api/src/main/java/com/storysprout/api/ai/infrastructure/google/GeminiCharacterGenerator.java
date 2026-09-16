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
@Component @ConditionalOnProperty(name="storysprout.ai.provider",havingValue="gemini")
public class GeminiCharacterGenerator implements CharacterGeneration.Generator {
 private final ChatClient chatClient; private final GeminiCharacterPromptBuilder promptBuilder; private final GeminiAiCallExecutor executor; private final GeminiAiProperties properties;
 public GeminiCharacterGenerator(ChatClient c,GeminiCharacterPromptBuilder p,GeminiAiCallExecutor e,GeminiAiProperties props,@Value("${spring.ai.google.genai.chat.model:gemini-2.5-flash}") String model){chatClient=c;promptBuilder=p;executor=e;properties=props;}
 public CharacterGeneration.Result generate(CharacterGeneration.Request r){String prompt=promptBuilder.build(r);for(int a=1;a<=properties.getMaxAttempts();a++){try{GeminiCharacterResponse x=executor.execute(()->chatClient.prompt().user(prompt).call().entity(GeminiCharacterResponse.class,s->s.useProviderStructuredOutput()),properties.getTimeout().toMillis(),TimeUnit.MILLISECONDS);if(x==null||x.name()==null||x.name().isBlank()||x.roleDescription()==null||x.roleDescription().isBlank()||x.category()==null||x.visualDescription()==null||x.visualDescription().isBlank())throw new CharacterGenerationException("AI returned an invalid character suggestion");return new CharacterGeneration.Result(x.name().trim(),x.roleDescription().trim(),x.category(),x.visualDescription().trim(),x.personality()==null?null:x.personality().trim());}catch(TimeoutException e){throw new CharacterGenerationException("Character generation timed out");}catch(CharacterGenerationException e){throw e;}catch(TransientAiException e){if(a<properties.getMaxAttempts())continue;throw new CharacterGenerationException("Character generation temporarily unavailable");}catch(NonTransientAiException e){throw new CharacterGenerationException("Character generation is unavailable");}catch(RuntimeException e){throw new CharacterGenerationException("Character generation is unavailable");}}throw new CharacterGenerationException("Character generation is unavailable");}
}
