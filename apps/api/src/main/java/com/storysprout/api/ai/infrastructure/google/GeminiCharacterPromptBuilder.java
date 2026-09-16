package com.storysprout.api.ai.infrastructure.google;
import com.storysprout.api.character.CharacterGeneration;
import org.springframework.stereotype.Component;
@Component public class GeminiCharacterPromptBuilder {
 public String build(CharacterGeneration.Request r){return "Create a reusable kids-story character suggestion. Return only the requested structured fields. Keep the character suitable for the target audience. Character idea: "+safe(r.idea())+"\nStory title: "+safe(r.storyTitle())+"\nStory idea: "+safe(r.storyIdea())+"\nStory draft: "+safe(r.storyDraft())+"\nTarget age: "+safe(r.targetAge())+"\nLanguage: "+safe(r.language())+"\nVisual style: "+safe(r.visualStyle())+"\nFields: name, roleDescription, category (CHILD, ADULT, ANIMAL, FANTASY, OBJECT, OTHER), visualDescription, personality. Visual description is character identity only, not camera/animation/composition instructions.";}
 private String safe(String s){return s==null?"(not provided)":s.trim();}
}
