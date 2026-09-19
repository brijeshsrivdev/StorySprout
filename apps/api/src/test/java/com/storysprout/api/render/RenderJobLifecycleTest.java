package com.storysprout.api.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RenderJobLifecycleTest {
    @Test
    void rejectsAllOtherDeclaredStateTransitions() {
        for (RenderJobStatus from : RenderJobStatus.values()) {
            for (RenderJobStatus to : RenderJobStatus.values()) {
                boolean allowed = switch (from) {
                    case REQUESTED -> to == RenderJobStatus.QUEUED || to == RenderJobStatus.FAILED;
                    case QUEUED -> to == RenderJobStatus.RENDERING || to == RenderJobStatus.FAILED;
                    case RENDERING -> to == RenderJobStatus.COMPLETED || to == RenderJobStatus.FAILED;
                    case FAILED -> to == RenderJobStatus.QUEUED;
                    case COMPLETED -> false;
                };
                assertEquals(allowed, RenderJobLifecycle.canTransition(from, to), from + " -> " + to);
            }
        }
    }

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
