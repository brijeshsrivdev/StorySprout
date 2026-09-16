package com.storysprout.api.outline;

import com.storysprout.api.story.ApiResponse;
import com.storysprout.api.story.StoryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class StoryOutlineController {
    private final StoryOutlineService service;

    public StoryOutlineController(StoryOutlineService service) { this.service = service; }

    @GetMapping("/projects/{projectId}/stories/{storyId}")
    public ApiResponse<StoryResponse> getStory(@PathVariable UUID projectId, @PathVariable UUID storyId) {
        return new ApiResponse<>(StoryResponse.from(service.getStory(projectId, storyId)));
    }

    @GetMapping("/projects/{projectId}/stories")
    public ApiResponse<List<StoryResponse>> listStories(@PathVariable UUID projectId) {
        return new ApiResponse<>(service.listStories(projectId).stream().map(StoryResponse::from).toList());
    }

    @GetMapping("/stories/{storyId}/outline-scenes")
    public ApiResponse<OutlineResponse> getOutline(@PathVariable UUID storyId) {
        return new ApiResponse<>(OutlineResponse.from(service.getOutline(storyId)));
    }

    @PostMapping("/stories/{storyId}/outline/generate")
    public ResponseEntity<ApiResponse<OutlineResponse>> generate(@PathVariable UUID storyId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(OutlineResponse.from(service.generate(storyId))));
    }

    @PostMapping("/stories/{storyId}/outline-scenes")
    public ResponseEntity<ApiResponse<OutlineSceneResponse>> create(@PathVariable UUID storyId, @RequestBody CreateOutlineSceneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(OutlineSceneResponse.from(service.create(storyId, request))));
    }

    @PatchMapping("/stories/{storyId}/outline-scenes/{sceneId}")
    public ApiResponse<OutlineSceneResponse> update(@PathVariable UUID storyId, @PathVariable UUID sceneId, @RequestBody UpdateOutlineSceneRequest request) {
        return new ApiResponse<>(OutlineSceneResponse.from(service.update(storyId, sceneId, request)));
    }

    @DeleteMapping("/stories/{storyId}/outline-scenes/{sceneId}")
    public ApiResponse<Void> delete(@PathVariable UUID storyId, @PathVariable UUID sceneId) {
        service.delete(storyId, sceneId);
        return new ApiResponse<>(null);
    }

    @PatchMapping("/stories/{storyId}/outline-scenes/reorder")
    public ApiResponse<List<OutlineSceneResponse>> reorder(@PathVariable UUID storyId, @RequestBody ReorderOutlineScenesRequest request) {
        return new ApiResponse<>(service.reorder(storyId, request).stream().map(OutlineSceneResponse::from).toList());
    }
}
