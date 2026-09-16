package com.storysprout.api.character;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.storysprout.api.story.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
class CharacterServiceTest {
 @Mock ProjectRepository projects; @Mock StoryRepository stories; @Mock CharacterRepository chars; @Mock CharacterGeneration.Generator generator; CharacterService service; UUID project=UUID.randomUUID(),storyId=UUID.randomUUID(); Story story;
 @BeforeEach void set(){MockitoAnnotations.openMocks(this);service=new CharacterService(projects,stories,chars,generator);when(projects.findById(project)).thenReturn(Optional.of(new Project(project,"P",ProjectStatus.DRAFT,Instant.now(),Instant.now())));story=new Story(storyId,project,"S","idea","6_8",3,"2D","ENGLISH",StoryCreationMode.BLANK,StoryGenerationStatus.NOT_REQUESTED,null,Instant.now(),Instant.now());when(stories.findById(storyId)).thenReturn(Optional.of(story));}
 @Test void createPersistsProjectCharacter(){when(chars.insert(any())).thenAnswer(i->i.getArgument(0));var c=service.create(project,new CharacterRequests.Create("Milo","A rabbit",CharacterCategory.ANIMAL,"Brown rabbit","Kind"));assertThat(c.projectId()).isEqualTo(project);verify(chars).insert(any());}
 @Test void aiSuggestionDoesNotPersist(){when(generator.generate(any())).thenReturn(new CharacterGeneration.Result("Milo","A rabbit",CharacterCategory.ANIMAL,"Brown rabbit",null));var r=service.generate(new CharacterRequests.Generate("A brave rabbit",storyId.toString()),project);assertThat(r.name()).isEqualTo("Milo");verify(chars,never()).insert(any());}
 @Test void crossProjectMembershipRejected(){UUID other=UUID.randomUUID();Character c=new Character(UUID.randomUUID(),other,"Milo","Rabbit",CharacterCategory.ANIMAL,"Brown",null,Instant.now(),Instant.now());when(chars.findById(c.id())).thenReturn(Optional.of(c));assertThatThrownBy(()->service.addToStory(storyId,c.id())).isInstanceOf(CharacterConflictException.class);verify(chars,never()).addToStory(any(),any());}
 @Test void removingMembershipDoesNotDeleteCharacter(){UUID id=UUID.randomUUID();when(chars.membershipExists(storyId,id)).thenReturn(true);service.removeFromStory(storyId,id);verify(chars).removeFromStory(storyId,id);verify(chars,never()).delete(any(),any());}
 @Test void usedCharacterCannotBeDeleted(){UUID id=UUID.randomUUID();when(chars.findByIdAndProjectId(id,project)).thenReturn(Optional.of(new Character(id,project,"Milo","Rabbit",CharacterCategory.ANIMAL,"Brown",null,Instant.now(),Instant.now())));when(chars.isUsed(id)).thenReturn(true);assertThatThrownBy(()->service.delete(project,id)).isInstanceOf(CharacterConflictException.class);verify(chars,never()).delete(any(),any());}
}
