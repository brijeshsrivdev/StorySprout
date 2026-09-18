package com.storysprout.api.render;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class MinioObjectStorageTest {
    @Container
    static final GenericContainer<?> minio = new GenericContainer<>("minio/minio:latest")
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", "storysprout")
            .withEnv("MINIO_ROOT_PASSWORD", "storysprout")
            .withCommand("server /data")
            .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forListeningPort());

    @Test
    void storesAndRetrievesRenderedArtifact() throws Exception {
        MinioObjectStorage storage = new MinioObjectStorage(
                "http://" + minio.getHost() + ":" + minio.getMappedPort(9000),
                "storysprout",
                "storysprout",
                "test-renders");

        byte[] payload = "mp4-placeholder".getBytes(StandardCharsets.UTF_8);
        storage.put("renders/test.mp4", new java.io.ByteArrayInputStream(payload), payload.length, "video/mp4");

        try (InputStream input = storage.get("renders/test.mp4")) {
            assertArrayEquals(payload, input.readAllBytes());
        }
    }
}
