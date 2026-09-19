package com.storysprout.api.render;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

class RenderJobControllerTest {
    @Test
    void requestRenderUsesAcceptedEnvelopeAndExactCompositionVersion() throws Exception {
        RenderJobService service = mock(RenderJobService.class);
        UUID storyId = UUID.randomUUID();
        UUID sceneId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID compositionId = UUID.randomUUID();
        RenderJob job = new RenderJob(jobId, storyId, sceneId, compositionId, 7,
                new ObjectMapper().createObjectNode(), RenderJobStatus.QUEUED, 0, 1,
                null, null, null, null, Instant.now(), Instant.now());
        when(service.request(storyId, sceneId)).thenReturn(job);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new RenderJobController(service)).build();

        mvc.perform(post("/api/v1/stories/{storyId}/outline-scenes/{sceneId}/render-jobs", storyId, sceneId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.compositionVersion").value(7))
                .andExpect(jsonPath("$.data.status").value("QUEUED"));
    }

    @Test
    void retryRequiresFailedJobAndUsesConflictForNonFailedJobs() throws Exception {
        RenderJobService service = mock(RenderJobService.class);
        UUID storyId = UUID.randomUUID();
        UUID sceneId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        when(service.retry(storyId, sceneId, jobId))
                .thenThrow(new RenderJobConflictException("Only failed RenderJobs can be retried"));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new RenderJobController(service)).build();

        mvc.perform(post("/api/v1/stories/{storyId}/outline-scenes/{sceneId}/render-jobs/{jobId}/retry", storyId, sceneId, jobId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"));
    }

    @Test
    void artifactBeforeCompletionIsMappedToConflict() throws Exception {
        RenderJobService service = mock(RenderJobService.class);
        UUID storyId = UUID.randomUUID();
        UUID sceneId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        when(service.artifact(storyId, sceneId, jobId))
                .thenThrow(new RenderJobConflictException("Render artifact is not available yet"));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new RenderJobController(service)).build();

        mvc.perform(get("/api/v1/stories/{storyId}/outline-scenes/{sceneId}/render-jobs/{jobId}/artifact", storyId, sceneId, jobId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"));
    }

    @Test
    void missingJobIsMappedToNotFound() throws Exception {
        RenderJobService service = mock(RenderJobService.class);
        UUID storyId = UUID.randomUUID();
        UUID sceneId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        when(service.get(storyId, sceneId, jobId)).thenThrow(new RenderJobNotFoundException("RenderJob not found"));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new RenderJobController(service)).build();

        mvc.perform(get("/api/v1/stories/{storyId}/outline-scenes/{sceneId}/render-jobs/{jobId}", storyId, sceneId, jobId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }
}
