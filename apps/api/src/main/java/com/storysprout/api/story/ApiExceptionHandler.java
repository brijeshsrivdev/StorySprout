package com.storysprout.api.story;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
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
    ResponseEntity<ApiErrorResponse> notFound(ProjectNotFoundException ex) { return ResponseEntity.status(404).body(ApiErrorResponse.of("PROJECT_NOT_FOUND", "Project not found", List.of(new ApiFieldError("projectId", ex.projectId().toString())))); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> unexpected(Exception ex) { return ResponseEntity.internalServerError().body(ApiErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred.", List.of())); }
}
