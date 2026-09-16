package com.storysprout.api.scene;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SceneSetup(UUID id, UUID outlineSceneId, String backgroundPresetKey, List<SceneSetupCharacter> characters,
                         List<SceneSetupProp> props, List<SceneSetupDialogue> dialogue, List<SceneSetupAction> actions,
                         Instant createdAt, Instant updatedAt) {
    public SceneSetup { characters=List.copyOf(characters); props=List.copyOf(props); dialogue=List.copyOf(dialogue); actions=List.copyOf(actions); }
}
