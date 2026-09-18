package com.storysprout.api.editor;

import tools.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public record SaveCompositionRequest(@NotNull Long version, @NotNull JsonNode composition) {}
