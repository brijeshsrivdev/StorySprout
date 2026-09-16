package com.storysprout.api.outline;

import java.util.List;
import java.util.UUID;

public record ReorderOutlineScenesRequest(List<UUID> sceneIds) {}
