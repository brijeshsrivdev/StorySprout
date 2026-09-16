package com.storysprout.api.story;

import java.util.List;

public record ApiErrorResponse(ApiError error) {
    public static ApiErrorResponse of(String code, String message, List<ApiFieldError> fields) { return new ApiErrorResponse(new ApiError(code, message, fields)); }
    public record ApiError(String code, String message, List<ApiFieldError> fields) {}
}
