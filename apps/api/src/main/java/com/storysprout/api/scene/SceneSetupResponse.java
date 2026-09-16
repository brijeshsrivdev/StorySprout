package com.storysprout.api.scene;

import com.storysprout.api.character.Character;
import java.util.List;
import java.util.UUID;

public record SceneSetupResponse(UUID id, UUID storyId, UUID outlineSceneId, String storyTitle, int sceneOrder,
                                 String sceneTitle, String sceneSummary, int plannedDurationSeconds,
                                 String backgroundPresetKey, List<SelectedCharacter> characters,
                                 List<SceneSetupProp> props, List<SceneSetupDialogue> dialogue,
                                 List<SceneSetupAction> actions, List<SceneSetupCatalog.Preset> backgroundOptions,
                                 List<SceneSetupCatalog.Preset> propOptions) {
    public record SelectedCharacter(UUID id, String name, String category, int orderIndex) {}
    public static SceneSetupResponse from(SceneSetup setup, com.storysprout.api.story.Story story,
                                           com.storysprout.api.outline.OutlineScene scene, List<Character> chars) {
        var byId=chars.stream().collect(java.util.stream.Collectors.toMap(Character::id,c->c));
        return new SceneSetupResponse(setup.id(),story.id(),scene.id(),story.title(),scene.orderIndex(),scene.title(),scene.summary(),scene.durationSeconds(),setup.backgroundPresetKey(),
            setup.characters().stream().map(c->{Character x=byId.get(c.characterId());return new SelectedCharacter(c.characterId(),x==null?"Orphaned Character":x.name(),x==null?"UNKNOWN":x.category().name(),c.orderIndex());}).toList(),
            setup.props(),setup.dialogue(),setup.actions(),SceneSetupCatalog.BACKGROUNDS,SceneSetupCatalog.PROPS);
    }
}
