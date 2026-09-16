package com.storysprout.api.story;

import jakarta.validation.constraints.NotBlank;

public record ProjectCreateRequest(@NotBlank(message = "Project name is required") String name) {}
