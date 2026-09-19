package com.storysprout.api.render;

public class RenderJobException extends RuntimeException {
    public RenderJobException(String message) { super(message); }
    public RenderJobException(String message, Throwable cause) { super(message, cause); }
}
