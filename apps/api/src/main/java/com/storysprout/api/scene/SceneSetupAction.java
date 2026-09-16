package com.storysprout.api.scene;
import java.util.UUID;
public record SceneSetupAction(UUID id, UUID characterId, int sequenceIndex, String action) {}
