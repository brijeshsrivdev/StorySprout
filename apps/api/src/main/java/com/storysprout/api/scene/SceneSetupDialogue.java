package com.storysprout.api.scene;
import java.util.UUID;
public record SceneSetupDialogue(UUID id, int sequenceIndex, String speakerType, UUID characterId, String text) {}
