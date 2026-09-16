package com.storysprout.api.outline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class StoryOutlineControllerIntegrationTest {
    @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");
    @LocalServerPort int port;
    @org.springframework.beans.factory.annotation.Autowired JdbcTemplate jdbc;
    @MockitoBean StoryOutlineGenerator generator;
    final HttpClient http = HttpClient.newHttpClient();

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) { registry.add("spring.datasource.url", postgres::getJdbcUrl); registry.add("spring.datasource.username", postgres::getUsername); registry.add("spring.datasource.password", postgres::getPassword); }

    @BeforeEach void clean() { jdbc.update("DELETE FROM story_outline_scenes"); jdbc.update("DELETE FROM stories"); jdbc.update("DELETE FROM projects"); reset(generator); }

    @Test void outlineCrudAndVariancePersist() throws Exception {
        String projectId = createProject("Outline Project"); String storyId = createStory(projectId);
        when(generator.generate(any())).thenReturn(new StoryOutlineGenerationResult(List.of(new GeneratedOutlineScene("One", "First", 40), new GeneratedOutlineScene("Two", "Second", 55), new GeneratedOutlineScene("Three", "Third", 50), new GeneratedOutlineScene("Four", "Fourth", 45))));
        HttpResponse<String> generated = request("POST", "/api/v1/stories/" + storyId + "/outline/generate", null);
        assertThat(generated.statusCode()).isEqualTo(201); assertThat(generated.body()).contains("\"plannedDurationSeconds\":190").contains("\"varianceSeconds\":10");
        String sceneId = extractId(request("POST", "/api/v1/stories/" + storyId + "/outline-scenes", "{\"title\":\"Five\",\"summary\":\"Fifth\",\"durationSeconds\":20}").body());
        HttpResponse<String> updated = request("PATCH", "/api/v1/stories/" + storyId + "/outline-scenes/" + sceneId, "{\"title\":\"Five Updated\",\"summary\":\"Fifth updated\",\"durationSeconds\":30}");
        assertThat(updated.statusCode()).withFailMessage(updated.body()).isEqualTo(200);
        assertThat(request("GET", "/api/v1/stories/" + storyId + "/outline-scenes", null).body()).contains("\"plannedDurationSeconds\":220").contains("\"varianceSeconds\":40");
        assertThat(request("DELETE", "/api/v1/stories/" + storyId + "/outline-scenes/" + sceneId, null).statusCode()).isEqualTo(200);
        String finalBody = request("GET", "/api/v1/stories/" + storyId + "/outline-scenes", null).body();
        assertThat(finalBody).contains("\"plannedDurationSeconds\":190").contains("\"durationSeconds\":40").contains("\"durationSeconds\":45");
    }

    @Test void reorderPreservesDurations() throws Exception {
        String projectId = createProject("Reorder Project"); String storyId = createStory(projectId);
        when(generator.generate(any())).thenReturn(new StoryOutlineGenerationResult(List.of(new GeneratedOutlineScene("One", "First", 40), new GeneratedOutlineScene("Two", "Second", 80))));
        String body = request("POST", "/api/v1/stories/" + storyId + "/outline/generate", null).body(); Matcher ids = Pattern.compile("\"id\":\"([0-9a-f-]{36})\"").matcher(body); ids.find(); ids.find(); String first = ids.group(1); ids.find(); String second = ids.group(1);
        HttpResponse<String> response = request("PATCH", "/api/v1/stories/" + storyId + "/outline-scenes/reorder", "{\"sceneIds\":[\"" + second + "\",\"" + first + "\"]}");
        assertThat(response.statusCode()).withFailMessage(response.body()).isEqualTo(200); assertThat(response.body()).contains("\"durationSeconds\":80").contains("\"durationSeconds\":40");
    }

    @Test void invalidDurationsAreRejected() throws Exception {
        String projectId = createProject("Validation Project"); String storyId = createStory(projectId);
        assertThat(request("POST", "/api/v1/stories/" + storyId + "/outline-scenes", "{\"title\":\"Bad\",\"summary\":\"Bad\",\"durationSeconds\":0}").statusCode()).isEqualTo(400);
        assertThat(request("POST", "/api/v1/stories/" + storyId + "/outline-scenes", "{\"title\":\"Bad\",\"summary\":\"Bad\",\"durationSeconds\":-2}").statusCode()).isEqualTo(400);
    }

    @Test void existingOutlineBlocksRegeneration() throws Exception {
        String projectId = createProject("Conflict Project"); String storyId = createStory(projectId);
        when(generator.generate(any())).thenReturn(new StoryOutlineGenerationResult(List.of(new GeneratedOutlineScene("One", "First", 10))));
        assertThat(request("POST", "/api/v1/stories/" + storyId + "/outline/generate", null).statusCode()).isEqualTo(201);
        assertThat(request("POST", "/api/v1/stories/" + storyId + "/outline/generate", null).statusCode()).isEqualTo(409);
    }

    private String createProject(String name) throws Exception { return extractId(request("POST", "/api/v1/projects", "{\"name\":\"" + name + "\"}").body()); }
    private String createStory(String projectId) throws Exception { return extractId(request("POST", "/api/v1/projects/" + projectId + "/stories", "{\"creationMode\":\"BLANK\",\"idea\":\"A rabbit learns to share.\",\"targetAge\":\"6_8\",\"durationMinutes\":3,\"visualStyle\":\"2D\",\"language\":\"ENGLISH\"}").body()); }
    private HttpResponse<String> request(String method, String path, String body) throws Exception { HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).header("Content-Type", "application/json"); HttpRequest request = switch (method) { case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body)).build(); case "PATCH" -> builder.method("PATCH", HttpRequest.BodyPublishers.ofString(body == null ? "" : body)).build(); case "DELETE" -> builder.DELETE().build(); default -> builder.GET().build(); }; return http.send(request, HttpResponse.BodyHandlers.ofString()); }
    private String extractId(String body) { Matcher matcher = Pattern.compile("\"id\":\"([0-9a-f-]{36})\"").matcher(body); if (!matcher.find()) throw new IllegalStateException("id missing: " + body); return matcher.group(1); }
}
