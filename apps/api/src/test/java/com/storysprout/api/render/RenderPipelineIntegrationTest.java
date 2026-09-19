package com.storysprout.api.render;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.atomic.AtomicReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RenderPipelineIntegrationTest {
    @Container static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");
    @Container static final GenericContainer<?> minio = new GenericContainer<>("minio/minio:latest")
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", "storysprout")
            .withEnv("MINIO_ROOT_PASSWORD", "storysprout")
            .withCommand("server /data")
            .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forListeningPort());

    static HttpServer renderer;
    static AtomicReference<String> rendererBody;

    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    final HttpClient http = HttpClient.newHttpClient();
    final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void startRenderer() throws IOException {
        rendererBody = new AtomicReference<>();
        renderer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        renderer.createContext("/render", exchange -> {
            rendererBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] mp4 = "fake-mp4".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "video/mp4");
            exchange.sendResponseHeaders(200, mp4.length);
            exchange.getResponseBody().write(mp4);
            exchange.close();
        });
        renderer.start();
    }

    @AfterAll
    static void stopRenderer() {
        if (renderer != null) renderer.stop(0);
    }

    @DynamicPropertySource
    static void infrastructure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("storysprout.renderer.base-url", () -> "http://localhost:" + renderer.getAddress().getPort());
        registry.add("storysprout.storage.endpoint", () -> "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
        registry.add("storysprout.storage.access-key", () -> "storysprout");
        registry.add("storysprout.storage.secret-key", () -> "storysprout");
        registry.add("storysprout.storage.bucket", () -> "integration-renders");
    }

    @BeforeAll
    static void verifyRendererStarted() {
        if (renderer == null) throw new IllegalStateException("Renderer stub was not started");
    }

    @Test
    void renderRequestPersistsSnapshotRendersStoresArtifactAndServesIt() throws Exception {
        String projectId = extractId(post("/api/v1/projects", "{\"name\":\"Render Project\"}"));
        String storyId = extractId(post("/api/v1/projects/" + projectId + "/stories",
                "{\"creationMode\":\"BLANK\",\"idea\":\"A rabbit renders a garden scene.\",\"targetAge\":\"6_8\",\"durationMinutes\":3,\"visualStyle\":\"2D\",\"language\":\"ENGLISH\"}"));
        String sceneId = extractId(post("/api/v1/stories/" + storyId + "/outline-scenes",
                "{\"title\":\"Garden\",\"summary\":\"A rabbit visits a garden.\",\"durationSeconds\":1}"));

        post("/api/v1/stories/" + storyId + "/outline-scenes/" + sceneId + "/scene-setup", "");

        var editor = get("/api/v1/stories/" + storyId + "/outline-scenes/" + sceneId + "/editor");
        assertThat(editor.statusCode()).isEqualTo(200);
        JsonNode editorJson = mapper.readTree(editor.body()).get("data");
        long version = editorJson.get("composition").get("version").asLong();
        assertThat(editorJson.get("composition").get("compositionJson").get("schemaVersion").asText()).isEqualTo("1.1");

        var renderResponse = post("/api/v1/stories/" + storyId + "/outline-scenes/" + sceneId + "/render-jobs", "");
        assertThat(renderResponse.statusCode()).isEqualTo(202);
        String jobId = extractId(renderResponse.body());

        String snapshot = null;
        String status = null;
        for (int i = 0; i < 40; i++) {
            var current = get("/api/v1/stories/" + storyId + "/outline-scenes/" + sceneId + "/render-jobs/" + jobId);
            status = jsonField(current.body(), "status");
            snapshot = jdbc.queryForObject("SELECT snapshot_json::text FROM render_jobs WHERE id = ?",
                    String.class, UUID.fromString(jobId));
            if ("COMPLETED".equals(status)) break;
            Thread.sleep(250);
        }

        assertThat(status).isEqualTo("COMPLETED");
        assertThat(jdbc.queryForObject("SELECT composition_version FROM render_jobs WHERE id = ?",
                Long.class, UUID.fromString(jobId))).isEqualTo(version);
        JsonNode snapshotJson = mapper.readTree(snapshot);
        assertThat(snapshotJson.get("schemaVersion").asText()).isEqualTo("1.1");
        assertThat(snapshotJson.get("projectId").asText()).isEqualTo(projectId);
        assertThat(rendererBody.get()).contains(jobId).contains(String.valueOf(version)).contains("\"schemaVersion\":\"1.1\"");

        var completed = get("/api/v1/stories/" + storyId + "/outline-scenes/" + sceneId + "/render-jobs/" + jobId);
        String artifactUrl = jsonField(completed.body(), "artifactUrl");
        var artifact = get(artifactUrl.replace("http://localhost:" + port, ""));
        assertThat(artifact.statusCode()).isEqualTo(200);
        assertThat(artifact.headers().firstValue("content-type").orElse("")).contains("video/mp4");
        assertThat(artifact.body()).contains("fake-mp4");
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body))
                .timeout(Duration.ofSeconds(10))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(path.startsWith("http") ? path : "http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10))
                .GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private String extractId(HttpResponse<String> response) {
        assertThat(response.statusCode()).withFailMessage(response.body()).isLessThan(300);
        return extractId(response.body());
    }

    private String extractId(String body) {
        Matcher matcher = Pattern.compile("\"id\":\"([0-9a-f-]{36})\"").matcher(body);
        if (!matcher.find()) throw new IllegalStateException("id missing: " + body);
        return matcher.group(1);
    }

    private String jsonField(String body, String field) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(field) + "\":\"([^\"]*)\"").matcher(body);
        if (!matcher.find()) throw new IllegalStateException(field + " missing: " + body);
        return matcher.group(1);
    }
}
