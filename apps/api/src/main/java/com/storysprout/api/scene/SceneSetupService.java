package com.storysprout.api.scene;

import com.storysprout.api.character.CharacterRepository;
import com.storysprout.api.story.Story;
import com.storysprout.api.story.StoryRepository;
import com.storysprout.api.story.StoryValidationException;
import com.storysprout.api.outline.OutlineScene;
import com.storysprout.api.outline.OutlineSceneRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SceneSetupService {
    private final StoryRepository stories; private final OutlineSceneRepository outlines; private final CharacterRepository characters; private final SceneSetupRepository setups;
    public SceneSetupService(StoryRepository stories, OutlineSceneRepository outlines, CharacterRepository characters, SceneSetupRepository setups){this.stories=stories;this.outlines=outlines;this.characters=characters;this.setups=setups;}
    @Transactional public SceneSetupResponse initialize(UUID storyId,UUID sceneId){Context c=context(storyId,sceneId);if(setups.findByOutlineSceneId(sceneId).isPresent())throw new SceneSetupConflictException("Scene Setup already exists");return response(setups.insert(UUID.randomUUID(),sceneId),c);}
    @Transactional(readOnly=true) public SceneSetupResponse get(UUID storyId,UUID sceneId){Context c=context(storyId,sceneId);SceneSetup s=setups.findByOutlineSceneId(sceneId).orElseThrow(()->new SceneSetupNotFoundException("Scene Setup not found"));return response(s,c);}
    @Transactional public SceneSetupResponse background(UUID storyId,UUID sceneId,String key){Context c=context(storyId,sceneId);requireSetup(sceneId);if(!SceneSetupCatalog.backgroundExists(key))throw new StoryValidationException("Unknown background preset");return response(setups.updateBackground(c.setup.id(),blankToNull(key)),c);}
    @Transactional public SceneSetupResponse saveCharacters(UUID storyId,UUID sceneId,List<UUID> ids){Context c=context(storyId,sceneId);requireSetup(sceneId);validateUnique(ids,"Character selection contains duplicates");for(UUID id:ids)if(!characters.membershipExists(storyId,id))throw new SceneSetupConflictException("Character must be attached to this Story");return response(setups.saveCharacters(c.setup.id(),ids),c);}
    @Transactional public SceneSetupResponse addProp(UUID storyId,UUID sceneId,String key){Context c=context(storyId,sceneId);requireSetup(sceneId);if(!SceneSetupCatalog.propExists(key))throw new StoryValidationException("Unknown prop preset");return response(setups.addProp(c.setup.id(),key),c);}
    @Transactional public SceneSetupResponse removeProp(UUID storyId,UUID sceneId,UUID propId){Context c=context(storyId,sceneId);requireSetup(sceneId);return response(setups.removeProp(c.setup.id(),propId),c);}
    @Transactional public SceneSetupResponse saveDialogue(UUID storyId,UUID sceneId,List<SceneSetupRequests.DialogueLine> lines){Context c=context(storyId,sceneId);requireSetup(sceneId);if(lines==null)throw new StoryValidationException("Dialogue lines are required");var out=new java.util.ArrayList<SceneSetupDialogue>();for(int i=0;i<lines.size();i++){var d=lines.get(i);if(d==null||d.text()==null||d.text().isBlank()||d.text().trim().length()>2000)throw new StoryValidationException("Dialogue text is required and must be at most 2000 characters");String speaker=d.speakerType()==null?"":d.speakerType().trim().toUpperCase();if(!speaker.equals("CHARACTER")&&!speaker.equals("NARRATOR"))throw new StoryValidationException("Speaker must be CHARACTER or NARRATOR");if(speaker.equals("NARRATOR")&&d.characterId()!=null)throw new StoryValidationException("Narrator cannot reference a Character");if(speaker.equals("CHARACTER")&&(d.characterId()==null||!characters.membershipExists(storyId,d.characterId())))throw new SceneSetupConflictException("Character speaker must be selected in this Story");out.add(new SceneSetupDialogue(UUID.randomUUID(),i+1,speaker,d.characterId(),d.text().trim()));}return response(setups.saveDialogue(c.setup.id(),out),c);}
    @Transactional public SceneSetupResponse saveActions(UUID storyId,UUID sceneId,List<SceneSetupRequests.ActionLine> actions){Context c=context(storyId,sceneId);requireSetup(sceneId);if(actions==null)throw new StoryValidationException("Actions are required");var out=new java.util.ArrayList<SceneSetupAction>();for(int i=0;i<actions.size();i++){var a=actions.get(i);if(a==null||a.characterId()==null||!characters.membershipExists(storyId,a.characterId()))throw new SceneSetupConflictException("Action must reference a Story Character");String action=a.action()==null?"":a.action().trim().toUpperCase();if(!SceneSetupCatalog.ACTIONS.contains(action))throw new StoryValidationException("Unsupported action");out.add(new SceneSetupAction(UUID.randomUUID(),a.characterId(),i+1,action));}return response(setups.saveActions(c.setup.id(),out),c);}
    @Transactional(readOnly=true) public SceneSetupResponse editorHandoff(UUID storyId,UUID sceneId){return get(storyId,sceneId);}
    private Context context(UUID storyId,UUID sceneId){Story story=stories.findById(storyId).orElseThrow(()->new SceneSetupNotFoundException("Story not found"));OutlineScene scene=outlines.findByIdAndStoryId(sceneId,storyId).orElseThrow(()->new SceneSetupNotFoundException("Outline Scene not found"));SceneSetup setup=setups.findByOutlineSceneId(sceneId).orElse(null);return new Context(story,scene,setup);}
    private SceneSetup requireSetup(UUID sceneId){return setups.findByOutlineSceneId(sceneId).orElseThrow(()->new SceneSetupNotFoundException("Scene Setup not found"));}
    private SceneSetupResponse response(SceneSetup s,Context c){return SceneSetupResponse.from(s,c.story,c.scene,characters.findByStoryId(c.story.id()));}
    private void validateUnique(List<UUID> ids,String message){if(ids==null)throw new StoryValidationException("Character IDs are required");if(ids.stream().anyMatch(java.util.Objects::isNull)||ids.size()!=ids.stream().distinct().count())throw new SceneSetupConflictException(message);}
    private String blankToNull(String s){return s==null||s.isBlank()?null:s.trim();}
    private record Context(Story story,OutlineScene scene,SceneSetup setup){}
}
