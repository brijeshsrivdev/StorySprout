package com.storysprout.api.outline;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class StoryOutlineControllerIntegrationTest {
    @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean StoryOutlineGenerator generator;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) { registry.add("spring.datasource.url", postgres::getJdbcUrl); registry.add("spring.datasource.username", postgres::getUsername); registry.add("spring.datasource.password", postgres::getPassword); }

    @BeforeEach void clean() { jdbc.update("DELETE FROM story_outline_scenes"); jdbc.update("DELETE FROM stories"); jdbc.update("DELETE FROM projects"); reset(generator); }

    @Test void outlineCrudAndVariancePersist() throws Exception {
        String projectId = createProject("Outline Project");
        String storyId = createStory(projectId);
        when(generator.generate(any())).thenReturn(new StoryOutlineGenerationResult(java.util.List.of(
            new GeneratedOutlineScene("One", "First", 40), new GeneratedOutlineScene("Two", "Second", 55), new GeneratedOutlineScene("Three", "Third", 50), new GeneratedOutlineScene("Four", "Fourth", 45))));

        mvc.perform(post("/api/v1/stories/" + storyId + "/outline/generate"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.data.targetDurationSeconds", is(180))).andExpect(jsonPath("$.data.plannedDurationSeconds", is(190))).andExpect(jsonPath("$.data.varianceSeconds", is(10))).andExpect(jsonPath("$.data.scenes", org.hamcrest.Matchers.hasSize(4)));

        String sceneId = extractId(mvc.perform(post("/api/v1/stories/" + storyId + "/outline-scenes").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Five\",\"summary\":\"Fifth\",\"durationSeconds\":20}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());

        mvc.perform(patch("/api/v1/stories/" + storyId + "/outline-scenes/" + sceneId).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Five Updated\",\"summary\":\"Fifth updated\",\"durationSeconds\":30}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.durationSeconds", is(30)));

        mvc.perform(get("/api/v1/stories/" + storyId + "/outline-scenes"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.plannedDurationSeconds", is(200))).andExpect(jsonPath("$.data.varianceSeconds", is(20)));

        mvc.perform(delete("/api/v1/stories/" + storyId + "/outline-scenes/" + sceneId)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/stories/" + storyId + "/outline-scenes")).andExpect(status().isOk()).andExpect(jsonPath("$.data.plannedDurationSeconds", is(190))).andExpect(jsonPath("$.data.scenes[0].durationSeconds", is(40))).andExpect(jsonPath("$.data.scenes[3].durationSeconds", is(45)));
    }

    @Test void reorderPreservesDurations() throws Exception {
        String projectId = createProject("Reorder Project");
        String storyId = createStory(projectId);
        when(generator.generate(any())).thenReturn(new StoryOutlineGenerationResult(java.util.List.of(new GeneratedOutlineScene("One", "First", 40), new GeneratedOutlineScene("Two", "Second", 80))));
        String body = mvc.perform(post("/api/v1/stories/" + storyId + "/outline/generate")).andReturn().getResponse().getContentAsString();
        Matcher ids = Pattern.compile("\"id\":\"([0-9a-f-]{36})\"").matcher(body); ids.find(); String first = ids.group(1); ids.find(); String second = ids.group(1);
        mvc.perform(patch("/api/v1/stories/" + storyId + "/outline-scenes/reorder").contentType(MediaType.APPLICATION_JSON).content("{\"sceneIds\":[\"" + second + "\",\"" + first + "\"]}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id", is(second))).andExpect(jsonPath("$.data[0].durationSeconds", is(80))).andExpect(jsonPath("$.data[1].durationSeconds", is(40)));
    }

    @Test void invalidDurationsAreRejected() throws Exception {
        String projectId = createProject("Validation Project"); String storyId = createStory(projectId);
        mvc.perform(post("/api/v1/stories/" + storyId + "/outline-scenes").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Bad\",\"summary\":\"Bad\",\"durationSeconds\":0}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));
        mvc.perform(post("/api/v1/stories/" + storyId + "/outline-scenes").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Bad\",\"summary\":\"Bad\",\"durationSeconds\":-2}"))
            .andExpect(status().isBadRequest());
    }

    @Test void existingOutlineBlocksRegeneration() throws Exception {
        String projectId = createProject("Conflict Project"); String storyId = createStory(projectId);
        when(generator.generate(any())).thenReturn(new StoryOutlineGenerationResult(java.util.List.of(new GeneratedOutlineScene("One", "First", 10))));
        mvc.perform(post("/api/v1/stories/" + storyId + "/outline/generate")).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/stories/" + storyId + "/outline/generate")).andExpect(status().isConflict());
    }

    private String createProject(String name) throws Exception {
        String body = mvc.perform(post("/api/v1/projects").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + name + "\"}" )).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return extractId(body);
    }

    private String createStory(String projectId) throws Exception {
        String body = mvc.perform(post("/api/v1/projects/" + projectId + "/stories").contentType(MediaType.APPLICATION_JSON).content("{\"creationMode\":\"BLANK\",\"idea\":\"A rabbit learns to share.\",\"targetAge\":\"6_8\",\"durationMinutes\":3,\"visualStyle\":\"2D\",\"language\":\"ENGLISH\"}" )).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return extractId(body);
    }

    private String extractId(String body) { Matcher matcher = Pattern.compile("\"id\":\"([0-9a-f-]{36})\"").matcher(body); if (!matcher.find()) throw new IllegalStateException("id missing: " + body); return matcher.group(1); }
}
