package com.storysprout.api.editor;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}")
public class EditorController {
    private final EditorService editor;
    public EditorController(EditorService editor){this.editor=editor;}

    @GetMapping("/editor")
    public ResponseEntity<?> context(@PathVariable UUID storyId,@PathVariable UUID outlineSceneId){return ResponseEntity.ok(java.util.Map.of("data",editor.open(storyId,outlineSceneId)));}

    @PostMapping("/editor/initialize")
    public ResponseEntity<?> initialize(@PathVariable UUID storyId,@PathVariable UUID outlineSceneId){return ResponseEntity.ok(java.util.Map.of("data",editor.open(storyId,outlineSceneId)));}

    @GetMapping("/composition")
    public ResponseEntity<?> composition(@PathVariable UUID storyId,@PathVariable UUID outlineSceneId){return ResponseEntity.ok(java.util.Map.of("data",editor.get(storyId,outlineSceneId)));}

    @PutMapping("/composition")
    public ResponseEntity<?> save(@PathVariable UUID storyId,@PathVariable UUID outlineSceneId,@RequestBody SaveCompositionRequest request){return ResponseEntity.ok(java.util.Map.of("data",editor.save(storyId,outlineSceneId,request.version(),request.composition())));}
}
