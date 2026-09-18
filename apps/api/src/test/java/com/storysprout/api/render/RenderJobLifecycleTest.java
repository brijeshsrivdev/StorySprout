package com.storysprout.api.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RenderJobLifecycleTest {
    @Test
    void allowsOnlyTheDeclaredForwardTransitions() {
        assertTrue(RenderJobLifecycle.canTransition(RenderJobStatus.REQUESTED, RenderJobStatus.QUEUED));
        assertTrue(RenderJobLifecycle.canTransition(RenderJobStatus.QUEUED, RenderJobStatus.RENDERING));
        assertTrue(RenderJobLifecycle.canTransition(RenderJobStatus.RENDERING, RenderJobStatus.COMPLETED));
        assertTrue(RenderJobLifecycle.canTransition(RenderJobStatus.RENDERING, RenderJobStatus.FAILED));
        assertTrue(RenderJobLifecycle.canTransition(RenderJobStatus.FAILED, RenderJobStatus.QUEUED));
        assertFalse(RenderJobLifecycle.canTransition(RenderJobStatus.COMPLETED, RenderJobStatus.QUEUED));
    }
}
