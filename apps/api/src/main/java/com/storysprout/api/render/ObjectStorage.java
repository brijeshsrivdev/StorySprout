package com.storysprout.api.render;

import java.io.IOException;
import java.io.InputStream;

public interface ObjectStorage {
    void put(String key, InputStream content, long contentLength, String contentType) throws IOException;
    InputStream get(String key) throws IOException;
}
