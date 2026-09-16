package com.storysprout.api.story;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class StoryController {
    private final StoryService service;
    public StoryController(StoryService service) { this.service = service; }

    @GetMapping("/projects")
    public ApiResponse<java.util.List<ProjectResponse>> listProjects() { return new ApiResponse<>(service.listProjects().stream().map(ProjectResponse::from).toList()); }

    @PostMapping("/projects")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@Valid @RequestBody ProjectCreateRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(ProjectResponse.from(service.createProject(request)))); }

    @PostMapping("/projects/{projectId}/stories")
    public ResponseEntity<?> createStory(@PathVariable UUID projectId, @Valid @RequestBody StoryCreateRequest request) {
        StoryCreationResult result = service.createStory(projectId, request);
        if (result.generationFailure() != null) {
            return ResponseEntity.status(422).body(ApiErrorResponse.of("GENERATION_FAILED", "Story generation failed. Your setup was saved.", List.of(new ApiFieldError("storyId", result.story().id().toString()), new ApiFieldError("generationStatus", result.story().generationStatus().name()))));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(StoryResponse.from(result.story())));
    }
}
