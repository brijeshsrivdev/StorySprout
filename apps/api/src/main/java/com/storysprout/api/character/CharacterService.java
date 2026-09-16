package com.storysprout.api.character;

import com.storysprout.api.story.ProjectRepository;
import com.storysprout.api.story.Story;
import com.storysprout.api.story.StoryRepository;
import com.storysprout.api.story.StoryValidationException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CharacterService {
    private final ProjectRepository projects;
    private final StoryRepository stories;
    private final CharacterRepository characters;
    private final CharacterGeneration.Generator generator;

    public CharacterService(ProjectRepository projects, StoryRepository stories, CharacterRepository characters, CharacterGeneration.Generator generator) {
        this.projects=projects; this.stories=stories; this.characters=characters; this.generator=generator;
    }
    @Transactional(readOnly=true) public List<Character> listProject(UUID projectId) { requireProject(projectId); return characters.findByProjectId(projectId); }
    @Transactional(readOnly=true) public List<Character> listStory(UUID storyId) { requireStory(storyId); return characters.findByStoryId(storyId); }
    @Transactional public Character create(UUID projectId, CharacterRequests.Create r) { requireProject(projectId); validate(r.name(),r.roleDescription(),r.category(),r.visualDescription(),r.personality()); Instant now=Instant.now(); return characters.insert(new Character(UUID.randomUUID(),projectId,r.name().trim(),r.roleDescription().trim(),r.category(),r.visualDescription().trim(),trimNullable(r.personality()),now,now)); }
    @Transactional public Character update(UUID projectId, UUID id, CharacterRequests.Update r) { requireProject(projectId); Character c=characters.findByIdAndProjectId(id,projectId).orElseThrow(()->new CharacterNotFoundException("Character not found")); validate(r.name(),r.roleDescription(),r.category(),r.visualDescription(),r.personality()); return characters.update(c.id(),projectId,r.name().trim(),r.roleDescription().trim(),r.category(),r.visualDescription().trim(),trimNullable(r.personality())); }
    @Transactional public void delete(UUID projectId, UUID id) { requireProject(projectId); characters.findByIdAndProjectId(id,projectId).orElseThrow(()->new CharacterNotFoundException("Character not found")); if(characters.isUsed(id)) throw new CharacterConflictException("Character is used by one or more stories"); characters.delete(id,projectId); }
    @Transactional public void addToStory(UUID storyId, UUID characterId) { Story story=requireStory(storyId); Character c=characters.findById(characterId).orElseThrow(()->new CharacterNotFoundException("Character not found")); if(!c.projectId().equals(story.projectId())) throw new CharacterConflictException("Character belongs to a different project"); if(characters.membershipExists(storyId,characterId)) throw new CharacterConflictException("Character is already part of this story"); characters.addToStory(storyId,characterId); }
    @Transactional public void removeFromStory(UUID storyId, UUID characterId) { requireStory(storyId); if(!characters.membershipExists(storyId,characterId)) throw new CharacterNotFoundException("Story character membership not found"); characters.removeFromStory(storyId,characterId); }
    @Transactional(readOnly=true) public CharacterGeneration.Result generate(CharacterRequests.Generate r, UUID projectId) { requireProject(projectId); Story story=null; if(r.storyId()!=null&&!r.storyId().isBlank()) { story=requireStory(UUID.fromString(r.storyId())); if(!story.projectId().equals(projectId)) throw new CharacterConflictException("Story belongs to a different project"); } CharacterGeneration.Result result=generator.generate(new CharacterGeneration.Request(r.idea(),story==null?null:story.title(),story==null?null:story.idea(),story==null?null:story.draftContent(),story==null?null:story.targetAge(),story==null?null:story.language(),story==null?null:story.visualStyle())); validate(result.name(),result.roleDescription(),result.category(),result.visualDescription(),result.personality()); return result; }
    private com.storysprout.api.story.Project requireProject(UUID id){return projects.findById(id).orElseThrow(()->new CharacterNotFoundException("Project not found"));}
    private Story requireStory(UUID id){return stories.findById(id).orElseThrow(()->new CharacterNotFoundException("Story not found"));}
    private void validate(String name,String role,CharacterCategory cat,String visual,String personality){if(name==null||name.isBlank()||name.trim().length()>120)throw new StoryValidationException("Character name is required and must be at most 120 characters");if(role==null||role.isBlank()||role.trim().length()>1000)throw new StoryValidationException("Role description is required and must be at most 1000 characters");if(cat==null)throw new StoryValidationException("Character category is required");if(visual==null||visual.isBlank()||visual.trim().length()>2000)throw new StoryValidationException("Visual description is required and must be at most 2000 characters");if(personality!=null&&personality.trim().length()>1000)throw new StoryValidationException("Personality must be at most 1000 characters");}
    private String trimNullable(String v){return v==null||v.isBlank()?null:v.trim();}
}
