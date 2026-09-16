package com.storysprout.api.editor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storysprout.api.character.Character;
import com.storysprout.api.character.CharacterCategory;
import com.storysprout.api.character.CharacterRepository;
import com.storysprout.api.outline.OutlineScene;
import com.storysprout.api.outline.OutlineSceneRepository;
import com.storysprout.api.scene.SceneSetup;
import com.storysprout.api.scene.SceneSetupCharacter;
import com.storysprout.api.scene.SceneSetupProp;
import com.storysprout.api.scene.SceneSetupRepository;
import com.storysprout.api.story.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;

class EditorServiceTest {
  private final StoryRepository stories=mock(StoryRepository.class); private final OutlineSceneRepository outlines=mock(OutlineSceneRepository.class);
  private final SceneSetupRepository setups=mock(SceneSetupRepository.class); private final CharacterRepository characters=mock(CharacterRepository.class); private final CompositionRepository compositions=mock(CompositionRepository.class);
  private final EditorService service=new EditorService(stories,outlines,setups,characters,compositions,new ObjectMapper());
  private final UUID storyId=UUID.randomUUID(), sceneId=UUID.randomUUID(), characterId=UUID.randomUUID();
  private final Story story=new Story(storyId,UUID.randomUUID(),"Story","idea","3_5",1,"2D","ENGLISH",StoryCreationMode.BLANK,StoryGenerationStatus.NOT_REQUESTED,null,Instant.now(),Instant.now());
  private final OutlineScene scene=new OutlineScene(sceneId,storyId,1,"Scene","summary",60,Instant.now(),Instant.now());
  private SceneSetup setup(){return new SceneSetup(UUID.randomUUID(),sceneId,null,List.of(new SceneSetupCharacter(characterId,1)),List.of(new SceneSetupProp(UUID.randomUUID(),"ball",1)),List.of(),List.of(),Instant.now(),Instant.now());}
  private Character character(){return new Character(characterId,story.projectId(),"Milo","child",CharacterCategory.CHILD,"visual",null,Instant.now(),Instant.now());}
  private void context(){when(stories.findById(storyId)).thenReturn(Optional.of(story));when(outlines.findByIdAndStoryId(sceneId,storyId)).thenReturn(Optional.of(scene));when(setups.findByOutlineSceneId(sceneId)).thenReturn(Optional.of(setup()));when(characters.findByStoryId(storyId)).thenReturn(List.of(character()));}

  @Test void initializesOnceWithExplicitTypes(){context();when(compositions.find(storyId,sceneId)).thenReturn(Optional.empty());var created=new Composition(UUID.randomUUID(),storyId,sceneId,new ObjectMapper().createObjectNode(),1,Instant.now(),Instant.now());when(compositions.insert(any(),eq(storyId),eq(sceneId),any())).thenReturn(created);service.open(storyId,sceneId);var json=org.mockito.ArgumentCaptor.forClass(com.fasterxml.jackson.databind.JsonNode.class);verify(compositions).insert(any(),eq(storyId),eq(sceneId),json.capture());assertEquals("1.1",json.getValue().get("schemaVersion").asText());assertEquals("CHARACTER",json.getValue().at("/scenes/0/objects/0/objectType").asText());assertEquals("PROP",json.getValue().at("/scenes/0/objects/1/objectType").asText());assertTrue(json.getValue().at("/scenes/0/timeline").isEmpty());}
  @Test void existingCompositionIsNotRebuilt(){context();var json=new ObjectMapper().createObjectNode().put("schemaVersion","1.1");var existing=new Composition(UUID.randomUUID(),storyId,sceneId,json,2,Instant.now(),Instant.now());when(compositions.find(storyId,sceneId)).thenReturn(Optional.of(existing));assertEquals("1.1",service.open(storyId,sceneId).compositionJson().get("schemaVersion").asText());verify(compositions,never()).insert(any(),any(),any(),any());}
  @Test void staleSaveDoesNotOverwrite(){context();var json=new ObjectMapper().createObjectNode();json.put("schemaVersion","1.1");json.put("projectId",storyId.toString());json.put("width",1920);json.put("height",1080);json.put("fps",30);json.put("durationMs",60000);var sceneJson=json.putArray("scenes").addObject();sceneJson.put("id",sceneId.toString());sceneJson.put("name","Scene");sceneJson.put("durationMs",60000);sceneJson.putArray("objects");sceneJson.putArray("timeline");var current=new Composition(UUID.randomUUID(),storyId,sceneId,json,2,Instant.now(),Instant.now());when(compositions.find(storyId,sceneId)).thenReturn(Optional.of(current));assertThrows(EditorConflictException.class,()->service.save(storyId,sceneId,1,json));verify(compositions,never()).update(any(),any(),any(),anyLong(),any());}
}
