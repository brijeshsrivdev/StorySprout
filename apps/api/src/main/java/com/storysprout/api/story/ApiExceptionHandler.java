package com.storysprout.api.story;

import com.storysprout.api.outline.OutlineConflictException;
import com.storysprout.api.outline.OutlineGenerationException;
import com.storysprout.api.outline.OutlineNotFoundException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(StoryValidationException.class)
    ResponseEntity<ApiErrorResponse> validation(StoryValidationException ex) { return ResponseEntity.badRequest().body(ApiErrorResponse.of("VALIDATION_ERROR", ex.getMessage(), List.of())); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> beanValidation(MethodArgumentNotValidException ex) {
        List<ApiFieldError> fields = ex.getBindingResult().getFieldErrors().stream().map(e -> new ApiFieldError(e.getField(), e.getDefaultMessage())).toList();
        return ResponseEntity.badRequest().body(ApiErrorResponse.of("VALIDATION_ERROR", "Request validation failed", fields));
    }
    @ExceptionHandler(ProjectNotFoundException.class)
    ResponseEntity<ApiErrorResponse> projectNotFound(ProjectNotFoundException ex) { return ResponseEntity.status(404).body(ApiErrorResponse.of("PROJECT_NOT_FOUND", "Project not found", List.of(new ApiFieldError("projectId", ex.projectId().toString())))); }
    @ExceptionHandler(OutlineNotFoundException.class)
    ResponseEntity<ApiErrorResponse> outlineNotFound(OutlineNotFoundException ex) { return ResponseEntity.status(404).body(ApiErrorResponse.of("NOT_FOUND", ex.getMessage(), List.of())); }
    @ExceptionHandler(OutlineConflictException.class)
    ResponseEntity<ApiErrorResponse> outlineConflict(OutlineConflictException ex) { return ResponseEntity.status(409).body(ApiErrorResponse.of("OUTLINE_EXISTS", ex.getMessage(), List.of())); }
    @ExceptionHandler(OutlineGenerationException.class)
    ResponseEntity<ApiErrorResponse> outlineGeneration(OutlineGenerationException ex) { return ResponseEntity.status(422).body(ApiErrorResponse.of("OUTLINE_GENERATION_FAILED", ex.getMessage(), List.of())); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> unexpected(Exception ex) { return ResponseEntity.internalServerError().body(ApiErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred.", List.of())); }
}
