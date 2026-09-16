package com.storysprout.api.editor;

import com.fasterxml.jackson.databind.JsonNode;
import com.storysprout.api.character.Character;
import com.storysprout.api.scene.SceneSetup;
import com.storysprout.api.scene.SceneSetupCatalog;
import java.util.List;
import java.util.UUID;

public record EditorContext(UUID storyId, String storyTitle, UUID outlineSceneId, int sceneOrder, String sceneTitle,
                            String sceneSummary, int plannedDurationSeconds, SceneSetup sceneSetup,
                            List<Character> characters, List<SceneSetupCatalog.Preset> backgroundOptions,
                            List<SceneSetupCatalog.Preset> propOptions, Composition composition,
                            String initializationStatus) {}
