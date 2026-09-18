package com.storysprout.api.render;

public final class RenderJobLifecycle {
    private RenderJobLifecycle() {}

    public static boolean canTransition(RenderJobStatus from, RenderJobStatus to) {
        return switch (from) {
            case REQUESTED -> to == RenderJobStatus.QUEUED || to == RenderJobStatus.FAILED;
            case QUEUED -> to == RenderJobStatus.RENDERING || to == RenderJobStatus.FAILED;
            case RENDERING -> to == RenderJobStatus.COMPLETED || to == RenderJobStatus.FAILED;
            case FAILED -> to == RenderJobStatus.QUEUED;
            case COMPLETED -> false;
        };
    }
}
