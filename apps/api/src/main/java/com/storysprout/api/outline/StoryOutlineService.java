package com.storysprout.api.outline;

import com.storysprout.api.story.Story;
import com.storysprout.api.story.StoryRepository;
import com.storysprout.api.story.StoryValidationException;
import com.storysprout.api.scene.SceneSetupRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoryOutlineService {
    private final StoryRepository stories;
    private final OutlineSceneRepository scenes;
    private final StoryOutlineGenerator generator;
    private final SceneSetupRepository sceneSetups;
    public StoryOutlineService(StoryRepository stories, OutlineSceneRepository scenes, StoryOutlineGenerator generator, SceneSetupRepository sceneSetups) { this.stories=stories;this.scenes=scenes;this.generator=generator;this.sceneSetups=sceneSetups; }
    @Transactional(readOnly=true) public Story getStory(UUID projectId,UUID storyId){Story story=stories.findById(storyId).orElseThrow(()->new OutlineNotFoundException("Story not found"));if(!story.projectId().equals(projectId))throw new OutlineNotFoundException("Story not found");return story;}
    @Transactional(readOnly=true) public List<Story> listStories(UUID projectId){return stories.findAllByProjectId(projectId);}
    @Transactional(readOnly=true) public OutlineSnapshot getOutline(UUID storyId){requireStory(storyId);return snapshot(storyId);}
    @Transactional public OutlineSnapshot generate(UUID storyId){Story story=requireStory(storyId);if(!scenes.findByStoryId(storyId).isEmpty())throw new OutlineConflictException("An outline already exists");StoryOutlineGenerationResult generated;try{generated=generator.generate(new StoryOutlineGenerationRequest(story.idea(),story.draftContent(),story.targetAge(),story.durationMinutes(),story.visualStyle(),story.language()));validateGenerated(generated);}catch(RuntimeException ex){if(ex instanceof OutlineGenerationException)throw ex;throw new OutlineGenerationException("Outline generation failed");}Instant now=Instant.now();int order=1;for(GeneratedOutlineScene s:generated.scenes())scenes.insert(new OutlineScene(UUID.randomUUID(),storyId,order++,s.title().trim(),s.summary().trim(),s.durationSeconds(),now,now));return snapshot(storyId);}
    @Transactional public OutlineScene create(UUID storyId,CreateOutlineSceneRequest r){requireStory(storyId);validateScene(r.title(),r.summary(),r.durationSeconds());int next=scenes.findByStoryId(storyId).size()+1;Instant now=Instant.now();return scenes.insert(new OutlineScene(UUID.randomUUID(),storyId,next,r.title().trim(),r.summary().trim(),r.durationSeconds(),now,now));}
    @Transactional public OutlineScene update(UUID storyId,UUID sceneId,UpdateOutlineSceneRequest r){requireScene(storyId,sceneId);validateScene(r.title(),r.summary(),r.durationSeconds());return scenes.update(sceneId,storyId,r.title().trim(),r.summary().trim(),r.durationSeconds());}
    @Transactional public void delete(UUID storyId,UUID sceneId){requireScene(storyId,sceneId);sceneSetups.deleteByOutlineSceneId(sceneId);scenes.deleteAndCompact(sceneId,storyId);}
    @Transactional public List<OutlineScene> reorder(UUID storyId,ReorderOutlineScenesRequest r){requireStory(storyId);List<OutlineScene> current=scenes.findByStoryId(storyId);if(r.sceneIds()==null||r.sceneIds().size()!=current.size()||new HashSet<>(r.sceneIds()).size()!=current.size()||!new HashSet<>(r.sceneIds()).equals(current.stream().map(OutlineScene::id).collect(java.util.stream.Collectors.toSet())))throw new StoryValidationException("Reorder must contain each current scene exactly once");scenes.reorder(storyId,r.sceneIds());return scenes.findByStoryId(storyId);}
    private Story requireStory(UUID id){return stories.findById(id).orElseThrow(()->new OutlineNotFoundException("Story not found"));}
    private OutlineScene requireScene(UUID storyId,UUID sceneId){requireStory(storyId);return scenes.findByIdAndStoryId(sceneId,storyId).orElseThrow(()->new OutlineNotFoundException("Outline scene not found"));}
    private OutlineSnapshot snapshot(UUID storyId){Story story=requireStory(storyId);List<OutlineScene> list=scenes.findByStoryId(storyId);int planned=list.stream().mapToInt(OutlineScene::durationSeconds).sum();return new OutlineSnapshot(story,list,story.durationMinutes()*60,planned,planned-story.durationMinutes()*60);}
    private void validateGenerated(StoryOutlineGenerationResult r){if(r==null||r.scenes()==null||r.scenes().isEmpty())throw new OutlineGenerationException("AI returned no scenes");r.scenes().forEach(s->validateScene(s.title(),s.summary(),s.durationSeconds()));}
    private void validateScene(String title,String summary,Integer duration){if(title==null||title.isBlank()||title.trim().length()>200)throw new StoryValidationException("Scene title is required and must be at most 200 characters");if(summary==null||summary.isBlank()||summary.trim().length()>2000)throw new StoryValidationException("Scene summary is required and must be at most 2000 characters");if(duration==null||duration<1)throw new StoryValidationException("Scene duration must be a positive integer number of seconds");}
}
