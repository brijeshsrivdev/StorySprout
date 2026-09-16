package com.storysprout.api.story;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public record StoryCreateRequest(
    @NotNull StoryCreationMode creationMode,
    @NotBlank(message = "Story idea is required") String idea,
    @NotBlank(message = "Target age is required") @Pattern(regexp = "3_5|6_8|9_12", message = "Target age must be 3_5, 6_8, or 9_12") String targetAge,
    @Min(value = 1, message = "Duration must be 1, 3, or 5 minutes") @Max(value = 5, message = "Duration must be 1, 3, or 5 minutes") int durationMinutes,
    @NotBlank(message = "Visual style is required") @Pattern(regexp = "2D|3D|HYBRID", message = "Visual style must be 2D, 3D, or HYBRID") String visualStyle,
    @NotBlank(message = "Language is required") @Pattern(regexp = "ENGLISH|HINDI", message = "Language must be ENGLISH or HINDI") String language) {}
