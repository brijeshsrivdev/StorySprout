package com.storysprout.api.story;

import com.storysprout.api.character.CharacterConflictException;
import com.storysprout.api.character.CharacterGenerationException;
import com.storysprout.api.character.CharacterNotFoundException;
import com.storysprout.api.outline.OutlineConflictException;
import com.storysprout.api.outline.OutlineGenerationException;
import com.storysprout.api.outline.OutlineNotFoundException;
import com.storysprout.api.scene.SceneSetupConflictException;
import com.storysprout.api.scene.SceneSetupNotFoundException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(StoryValidationException.class) ResponseEntity<ApiErrorResponse> validation(StoryValidationException ex){return ResponseEntity.badRequest().body(ApiErrorResponse.of("VALIDATION_ERROR",ex.getMessage(),List.of()));}
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiErrorResponse> beanValidation(MethodArgumentNotValidException ex){return ResponseEntity.badRequest().body(ApiErrorResponse.of("VALIDATION_ERROR","Request validation failed",ex.getBindingResult().getFieldErrors().stream().map(e->new ApiFieldError(e.getField(),e.getDefaultMessage())).toList()));}
    @ExceptionHandler(ProjectNotFoundException.class) ResponseEntity<ApiErrorResponse> projectNotFound(ProjectNotFoundException ex){return ResponseEntity.status(404).body(ApiErrorResponse.of("PROJECT_NOT_FOUND","Project not found",List.of()));}
    @ExceptionHandler(OutlineNotFoundException.class) ResponseEntity<ApiErrorResponse> outlineNotFound(OutlineNotFoundException ex){return ResponseEntity.status(404).body(ApiErrorResponse.of("NOT_FOUND",ex.getMessage(),List.of()));}
    @ExceptionHandler(OutlineConflictException.class) ResponseEntity<ApiErrorResponse> outlineConflict(OutlineConflictException ex){return ResponseEntity.status(409).body(ApiErrorResponse.of("OUTLINE_EXISTS",ex.getMessage(),List.of()));}
    @ExceptionHandler(OutlineGenerationException.class) ResponseEntity<ApiErrorResponse> outlineGeneration(OutlineGenerationException ex){return ResponseEntity.status(422).body(ApiErrorResponse.of("OUTLINE_GENERATION_FAILED",ex.getMessage(),List.of()));}
    @ExceptionHandler(CharacterNotFoundException.class) ResponseEntity<ApiErrorResponse> characterNotFound(CharacterNotFoundException ex){return ResponseEntity.status(404).body(ApiErrorResponse.of("CHARACTER_NOT_FOUND",ex.getMessage(),List.of()));}
    @ExceptionHandler(CharacterConflictException.class) ResponseEntity<ApiErrorResponse> characterConflict(CharacterConflictException ex){return ResponseEntity.status(409).body(ApiErrorResponse.of("CHARACTER_CONFLICT",ex.getMessage(),List.of()));}
    @ExceptionHandler(CharacterGenerationException.class) ResponseEntity<ApiErrorResponse> characterGeneration(CharacterGenerationException ex){return ResponseEntity.status(422).body(ApiErrorResponse.of("CHARACTER_GENERATION_FAILED",ex.getMessage(),List.of()));}
    @ExceptionHandler(SceneSetupNotFoundException.class) ResponseEntity<ApiErrorResponse> sceneSetupNotFound(SceneSetupNotFoundException ex){return ResponseEntity.status(404).body(ApiErrorResponse.of("NOT_FOUND",ex.getMessage(),List.of()));}
    @ExceptionHandler(SceneSetupConflictException.class) ResponseEntity<ApiErrorResponse> sceneSetupConflict(SceneSetupConflictException ex){return ResponseEntity.status(409).body(ApiErrorResponse.of("SCENE_SETUP_CONFLICT",ex.getMessage(),List.of()));}
    @ExceptionHandler(Exception.class) ResponseEntity<ApiErrorResponse> unexpected(Exception ex){return ResponseEntity.internalServerError().body(ApiErrorResponse.of("INTERNAL_ERROR","An unexpected error occurred.",List.of()));}
}
