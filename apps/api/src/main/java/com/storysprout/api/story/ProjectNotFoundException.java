package com.storysprout.api.story;

import java.util.UUID;

public class ProjectNotFoundException extends RuntimeException {
    private final UUID projectId;
    public ProjectNotFoundException(UUID projectId) { super("Project not found: " + projectId); this.projectId = projectId; }
    public UUID projectId() { return projectId; }
}
