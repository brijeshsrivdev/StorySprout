package com.storysprout.api.character;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="storysprout.ai.provider", havingValue="fake", matchIfMissing=true)
public class DeterministicCharacterGenerator implements CharacterGeneration.Generator {
    @Override public CharacterGeneration.Result generate(CharacterGeneration.Request request) {
        return new CharacterGeneration.Result("Milo", "A curious young rabbit who helps friends solve small problems.", CharacterCategory.ANIMAL, "Soft brown rabbit with long ears, a cream belly and a small blue scarf.", "Curious, kind and eager to help.");
    }
}
