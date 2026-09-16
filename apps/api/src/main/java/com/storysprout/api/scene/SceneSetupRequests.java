package com.storysprout.api.scene;

import java.util.List;
import java.util.UUID;

public final class SceneSetupRequests {
    private SceneSetupRequests() {}
    public record Background(String backgroundPresetKey) {}
    public record Characters(List<UUID> characterIds) {}
    public record Prop(String propPresetKey) {}
    public record DialogueLine(String speakerType, UUID characterId, String text) {}
    public record Dialogue(List<DialogueLine> lines) {}
    public record ActionLine(UUID characterId, String action) {}
    public record Actions(List<ActionLine> actions) {}
}
