package com.storysprout.api.story;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class StoryControllerIntegrationTest {
    @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean StoryGenerator generator;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) { registry.add("spring.datasource.url", postgres::getJdbcUrl); registry.add("spring.datasource.username", postgres::getUsername); registry.add("spring.datasource.password", postgres::getPassword); }
    @BeforeEach void clean() { jdbc.update("DELETE FROM stories"); jdbc.update("DELETE FROM projects"); reset(generator); }

    @Test void emptyProjectList() throws Exception { mvc.perform(get("/api/v1/projects")).andExpect(status().isOk()).andExpect(jsonPath("$.data", hasSize(0))); }
    @Test void projectCreationAndOrdering() throws Exception { mvc.perform(post("/api/v1/projects").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"First\"}")); mvc.perform(post("/api/v1/projects").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Second\"}")); mvc.perform(get("/api/v1/projects")).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].name", is("Second"))); }
    @Test void blankStoryDoesNotCallAi() throws Exception { String projectId = createProject("Blank Project"); mvc.perform(post("/api/v1/projects/" + projectId + "/stories").contentType(MediaType.APPLICATION_JSON).content(blankBody())).andExpect(status().isCreated()).andExpect(jsonPath("$.data.creationMode", is("BLANK"))).andExpect(jsonPath("$.data.generationStatus", is("NOT_REQUESTED"))); verifyNoInteractions(generator); }
    @Test void aiStoryPersistsGeneratedDraft() throws Exception { when(generator.generate(any())).thenReturn(new StoryGenerationResult("Rabbit Shares", "A rabbit shares a carrot.")); String projectId = createProject("AI Project"); mvc.perform(post("/api/v1/projects/" + projectId + "/stories").contentType(MediaType.APPLICATION_JSON).content(aiBody())).andExpect(status().isCreated()).andExpect(jsonPath("$.data.generationStatus", is("COMPLETED"))).andExpect(jsonPath("$.data.draftContent", is("A rabbit shares a carrot."))); verify(generator, times(1)).generate(new StoryGenerationRequest("A rabbit learns to share.", "6_8", 3, "2D", "ENGLISH")); }
    @Test void aiFailurePersistsFailedStoryAndReturns422() throws Exception { when(generator.generate(any())).thenThrow(new StoryGenerationException("generation unavailable")); String projectId = createProject("Failure Project"); mvc.perform(post("/api/v1/projects/" + projectId + "/stories").contentType(MediaType.APPLICATION_JSON).content(aiBody())).andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.error.code", is("GENERATION_FAILED"))).andExpect(jsonPath("$.error.fields[0].field", is("storyId"))); Integer count = jdbc.queryForObject("SELECT count(*) FROM stories WHERE project_id = ? AND generation_status = 'FAILED'", Integer.class, UUID.fromString(projectId)); org.assertj.core.api.Assertions.assertThat(count).isEqualTo(1); }
    @Test void invalidRequestAndUnknownProjectAreRejected() throws Exception { mvc.perform(post("/api/v1/projects").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \"}")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR"))); mvc.perform(post("/api/v1/projects/00000000-0000-0000-0000-000000000000/stories").contentType(MediaType.APPLICATION_JSON).content(blankBody())).andExpect(status().isNotFound()).andExpect(jsonPath("$.error.code", is("PROJECT_NOT_FOUND"))); org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject("SELECT count(*) FROM stories", Integer.class)).isZero(); }
    @Test void databaseConstraintsProtectDomain() throws Exception { String projectId = createProject("Constraint Project"); org.assertj.core.api.Assertions.assertThatThrownBy(() -> jdbc.update("INSERT INTO stories (id, project_id, title, idea, target_age, duration_minutes, visual_style, language, creation_mode, generation_status, created_at, updated_at) VALUES (gen_random_uuid(), ?,'x','x','13_15',3,'2D','ENGLISH','BLANK','NOT_REQUESTED',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)", UUID.fromString(projectId))).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class); org.assertj.core.api.Assertions.assertThatThrownBy(() -> jdbc.update("DELETE FROM projects WHERE id = ?", UUID.fromString(projectId))).isNotNull(); }

    private String createProject(String name) throws Exception { String body = mvc.perform(post("/api/v1/projects").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + name + "\"}")).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(); Matcher matcher = Pattern.compile("\\\"id\\\":\\\"([0-9a-f-]{36})\\\"").matcher(body); if (!matcher.find()) throw new IllegalStateException("Project id missing from response: " + body); return matcher.group(1); }
    private String blankBody() { return "{\"creationMode\":\"BLANK\",\"idea\":\"A rabbit learns to share.\",\"targetAge\":\"6_8\",\"durationMinutes\":3,\"visualStyle\":\"2D\",\"language\":\"ENGLISH\"}"; }
    private String aiBody() { return "{\"creationMode\":\"AI\",\"idea\":\"A rabbit learns to share.\",\"targetAge\":\"6_8\",\"durationMinutes\":3,\"visualStyle\":\"2D\",\"language\":\"ENGLISH\"}"; }
}
