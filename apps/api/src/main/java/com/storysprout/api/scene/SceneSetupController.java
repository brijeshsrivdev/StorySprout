package com.storysprout.api.scene;

import com.storysprout.api.story.ApiResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/scene-setup")
public class SceneSetupController {
    private final SceneSetupService service;
    public SceneSetupController(SceneSetupService service){this.service=service;}
    @GetMapping public ApiResponse<SceneSetupResponse> get(@PathVariable UUID storyId,@PathVariable UUID outlineSceneId){return new ApiResponse<>(service.get(storyId,outlineSceneId));}
    @PostMapping public ResponseEntity<ApiResponse<SceneSetupResponse>> initialize(@PathVariable UUID storyId,@PathVariable UUID outlineSceneId){return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(service.initialize(storyId,outlineSceneId)));}
    @PatchMapping("/background") public ApiResponse<SceneSetupResponse> background(@PathVariable UUID storyId,@PathVariable UUID outlineSceneId,@RequestBody SceneSetupRequests.Background request){return new ApiResponse<>(service.background(storyId,outlineSceneId,request.backgroundPresetKey()));}
    @PutMapping("/characters") public ApiResponse<SceneSetupResponse> characters(@PathVariable UUID storyId,@PathVariable UUID outlineSceneId,@RequestBody SceneSetupRequests.Characters request){return new ApiResponse<>(service.saveCharacters(storyId,outlineSceneId,request.characterIds()));}
    @PostMapping("/props") public ApiResponse<SceneSetupResponse> addProp(@PathVariable UUID storyId,@PathVariable UUID outlineSceneId,@RequestBody SceneSetupRequests.Prop request){return new ApiResponse<>(service.addProp(storyId,outlineSceneId,request.propPresetKey()));}
    @DeleteMapping("/props/{propId}") public ApiResponse<SceneSetupResponse> removeProp(@PathVariable UUID storyId,@PathVariable UUID outlineSceneId,@PathVariable UUID propId){return new ApiResponse<>(service.removeProp(storyId,outlineSceneId,propId));}
    @PutMapping("/dialogue") public ApiResponse<SceneSetupResponse> dialogue(@PathVariable UUID storyId,@PathVariable UUID outlineSceneId,@RequestBody SceneSetupRequests.Dialogue request){return new ApiResponse<>(service.saveDialogue(storyId,outlineSceneId,request.lines()));}
    @PutMapping("/actions") public ApiResponse<SceneSetupResponse> actions(@PathVariable UUID storyId,@PathVariable UUID outlineSceneId,@RequestBody SceneSetupRequests.Actions request){return new ApiResponse<>(service.saveActions(storyId,outlineSceneId,request.actions()));}
    @GetMapping("/editor-handoff") public ApiResponse<SceneSetupResponse> handoff(@PathVariable UUID storyId,@PathVariable UUID outlineSceneId){return new ApiResponse<>(service.editorHandoff(storyId,outlineSceneId));}
}
