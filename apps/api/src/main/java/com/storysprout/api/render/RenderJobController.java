package com.storysprout.api.render;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stories/{storyId}/outline-scenes/{sceneId}/render-jobs")
public class RenderJobController {
    private final RenderJobService service;

    public RenderJobController(RenderJobService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> request(@PathVariable UUID storyId, @PathVariable UUID sceneId) {
        try {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("data", response(service.request(storyId, sceneId))));
        } catch (RenderJobNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("NOT_FOUND", e.getMessage()));
        } catch (RenderJobException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error("RENDER_REQUEST_FAILED", e.getMessage()));
        }
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<?> get(@PathVariable UUID storyId, @PathVariable UUID sceneId, @PathVariable UUID jobId) {
        try {
            return ResponseEntity.ok(Map.of("data", response(service.get(storyId, sceneId, jobId))));
        } catch (RenderJobNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("NOT_FOUND", e.getMessage()));
        }
    }

    @PostMapping("/{jobId}/retry")
    public ResponseEntity<?> retry(@PathVariable UUID storyId, @PathVariable UUID sceneId, @PathVariable UUID jobId) {
        try {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("data", response(service.retry(storyId, sceneId, jobId))));
        } catch (RenderJobNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("NOT_FOUND", e.getMessage()));
        } catch (RenderJobConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error("CONFLICT", e.getMessage()));
        }
    }

    @GetMapping("/{jobId}/artifact")
    public ResponseEntity<?> artifact(@PathVariable UUID storyId, @PathVariable UUID sceneId, @PathVariable UUID jobId) {
        try {
            InputStream stream = service.artifact(storyId, sceneId, jobId);
            RenderJob job = service.get(storyId, sceneId, jobId);
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("video/mp4"))
                    .contentLength(job.artifactSize())
                    .body(new InputStreamResource(stream));
        } catch (RenderJobNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("NOT_FOUND", e.getMessage()));
        } catch (RenderJobConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error("CONFLICT", e.getMessage()));
        }
    }

    private RenderJobResponse response(RenderJob job) {
        String artifactUrl = job.artifactKey() == null ? null :
                "/api/v1/stories/" + job.storyId() + "/outline-scenes/" + job.outlineSceneId() +
                "/render-jobs/" + job.id() + "/artifact";
        return new RenderJobResponse(
                job.id(), job.storyId(), job.outlineSceneId(), job.compositionId(), job.compositionVersion(),
                job.status(), job.progress(), job.attempt(), job.failureCode(), job.failureMessage(),
                artifactUrl, job.artifactSize(), job.createdAt(), job.updatedAt());
    }

    private Map<String, Object> error(String code, String message) {
        return Map.of("error", Map.of("code", code, "message", message, "fields", java.util.List.of()));
    }
}
