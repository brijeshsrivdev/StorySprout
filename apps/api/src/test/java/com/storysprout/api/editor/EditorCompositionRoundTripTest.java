package com.storysprout.api.editor;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Guards the Jackson 2 -> Jackson 3 boundary for the editor Composition JsonNode.
 * Spring Boot 4 serializes/deserializes with Jackson 3 (tools.jackson); a Jackson 2
 * JsonNode field would serialize as its bean getters and fail to deserialize (HTTP 500).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class EditorCompositionRoundTripTest {
    @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");
    @LocalServerPort int port;
    @MockitoBean com.storysprout.api.character.CharacterGeneration.Generator characterGenerator;
    final HttpClient http = HttpClient.newHttpClient();
    final ObjectMapper mapper = new ObjectMapper();

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void editorContextSerializesRealCompositionAndSavePersists() throws Exception {
        String pid = extractId(req("POST", "/api/v1/projects", "{\"name\":\"P\"}").body());
        String sid = extractId(req("POST", "/api/v1/projects/" + pid + "/stories",
                "{\"creationMode\":\"BLANK\",\"idea\":\"Milo finds a butterfly.\",\"targetAge\":\"6_8\",\"durationMinutes\":3,\"visualStyle\":\"2D\",\"language\":\"ENGLISH\"}").body());
        String cid = extractId(req("POST", "/api/v1/projects/" + pid + "/characters",
                "{\"name\":\"Milo\",\"roleDescription\":\"A curious little rabbit\",\"category\":\"ANIMAL\",\"visualDescription\":\"Small brown rabbit\",\"personality\":null}").body());
        assertThat(req("POST", "/api/v1/stories/" + sid + "/characters/" + cid, null).statusCode()).isLessThan(300);
        String sceneId = extractId(req("POST", "/api/v1/stories/" + sid + "/outline-scenes",
                "{\"title\":\"Garden Discovery\",\"summary\":\"Milo discovers a butterfly.\",\"durationSeconds\":45}").body());
        req("POST", "/api/v1/stories/" + sid + "/outline-scenes/" + sceneId + "/scene-setup", null);
        req("PUT", "/api/v1/stories/" + sid + "/outline-scenes/" + sceneId + "/scene-setup/characters", "{\"characterIds\":[\"" + cid + "\"]}");

        // GET editor: compositionJson must serialize as the real tree (has "scenes"), not JsonNode bean getters.
        HttpResponse<String> editor = req("GET", "/api/v1/stories/" + sid + "/outline-scenes/" + sceneId + "/editor", null);
        assertThat(editor.statusCode()).withFailMessage(editor.body()).isEqualTo(200);
        JsonNode ctx = mapper.readTree(editor.body()).get("data");
        JsonNode compJson = ctx.get("composition").get("compositionJson");
        assertThat(compJson.has("scenes")).withFailMessage("compositionJson lost its tree: %s", compJson).isTrue();
        assertThat(compJson.get("schemaVersion").asText()).isEqualTo("1.1");
        long version = ctx.get("composition").get("version").asLong();
        String projectId = ctx.get("composition").get("projectId").asText();

        // Edit the character object and PUT it back.
        ObjectNode composition = (ObjectNode) compJson;
        ObjectNode object = (ObjectNode) composition.get("scenes").get(0).get("objects").get(0);
        object.put("x", 900);
        object.put("scale", 1.5);
        ObjectNode body = mapper.createObjectNode();
        body.put("version", version);
        body.set("composition", composition);
        HttpResponse<String> save = req("PUT", "/api/v1/stories/" + sid + "/outline-scenes/" + sceneId + "/composition", body.toString());
        assertThat(save.statusCode()).withFailMessage("save failed: %s %s", save.statusCode(), save.body()).isEqualTo(200);

        // Re-fetch and confirm the edit persisted and the projectId is preserved.
        JsonNode saved = mapper.readTree(req("GET", "/api/v1/stories/" + sid + "/outline-scenes/" + sceneId + "/composition", null).body())
                .get("data").get("compositionJson");
        JsonNode savedObject = saved.get("scenes").get(0).get("objects").get(0);
        assertThat(savedObject.get("x").asInt()).isEqualTo(900);
        assertThat(savedObject.get("scale").asDouble()).isEqualTo(1.5);
        assertThat(saved.get("projectId").asText()).isEqualTo(projectId);
    }

    private HttpResponse<String> req(String method, String path, String reqBody) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).header("Content-Type", "application/json");
        HttpRequest request = switch (method) {
            case "POST" -> b.POST(HttpRequest.BodyPublishers.ofString(reqBody == null ? "" : reqBody)).build();
            case "PUT" -> b.PUT(HttpRequest.BodyPublishers.ofString(reqBody == null ? "" : reqBody)).build();
            case "PATCH" -> b.method("PATCH", HttpRequest.BodyPublishers.ofString(reqBody == null ? "" : reqBody)).build();
            case "DELETE" -> b.DELETE().build();
            default -> b.GET().build();
        };
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String extractId(String body) {
        Matcher m = Pattern.compile("\"id\":\"([0-9a-f-]{36})\"").matcher(body);
        if (!m.find()) throw new IllegalStateException("id missing: " + body);
        return m.group(1);
    }
}
