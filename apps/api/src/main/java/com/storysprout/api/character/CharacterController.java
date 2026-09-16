package com.storysprout.api.character;

import com.storysprout.api.story.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CharacterController {
    private final CharacterService service;
    public CharacterController(CharacterService service){this.service=service;}
    @GetMapping("/projects/{projectId}/characters") public ApiResponse<List<CharacterResponse>> project(@PathVariable UUID projectId){return new ApiResponse<>(service.listProject(projectId).stream().map(CharacterResponse::from).toList());}
    @GetMapping("/stories/{storyId}/characters") public ApiResponse<List<CharacterResponse>> story(@PathVariable UUID storyId){return new ApiResponse<>(service.listStory(storyId).stream().map(CharacterResponse::from).toList());}
    @PostMapping("/projects/{projectId}/characters") public ResponseEntity<ApiResponse<CharacterResponse>> create(@PathVariable UUID projectId,@Valid @RequestBody CharacterRequests.Create request){return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(CharacterResponse.from(service.create(projectId,request))));}
    @PatchMapping("/projects/{projectId}/characters/{characterId}") public ApiResponse<CharacterResponse> update(@PathVariable UUID projectId,@PathVariable UUID characterId,@Valid @RequestBody CharacterRequests.Update request){return new ApiResponse<>(CharacterResponse.from(service.update(projectId,characterId,request)));}
    @DeleteMapping("/projects/{projectId}/characters/{characterId}") public ApiResponse<Void> delete(@PathVariable UUID projectId,@PathVariable UUID characterId){service.delete(projectId,characterId);return new ApiResponse<>(null);}
    @PostMapping("/stories/{storyId}/characters/{characterId}") public ResponseEntity<ApiResponse<Void>> add(@PathVariable UUID storyId,@PathVariable UUID characterId){service.addToStory(storyId,characterId);return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(null));}
    @DeleteMapping("/stories/{storyId}/characters/{characterId}") public ApiResponse<Void> remove(@PathVariable UUID storyId,@PathVariable UUID characterId){service.removeFromStory(storyId,characterId);return new ApiResponse<>(null);}
    @PostMapping("/projects/{projectId}/characters/generate") public ApiResponse<CharacterGeneration.Result> generate(@PathVariable UUID projectId,@RequestBody CharacterRequests.Generate request){return new ApiResponse<>(service.generate(request,projectId));}
}
