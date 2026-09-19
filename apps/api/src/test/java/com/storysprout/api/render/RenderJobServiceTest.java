package com.storysprout.api.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.storysprout.api.editor.Composition;
import com.storysprout.api.editor.EditorService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

class RenderJobServiceTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void requestCapturesExactCompositionVersionAndImmutableSnapshot() throws Exception {
        EditorService editor = mock(EditorService.class);
        RenderJobRepository repository = mock(RenderJobRepository.class);
        RendererClient renderer = mock(RendererClient.class);
        ObjectStorage storage = mock(ObjectStorage.class);
        AtomicReference<Runnable> queued = new AtomicReference<>();
        Executor executor = queued::set;

        UUID compositionId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID sceneId = UUID.randomUUID();
        UUID storyId = UUID.randomUUID();
        JsonNode source = mapper.readTree("""
            {"schemaVersion":"1.1","projectId":"%s","width":1920,"height":1080,"fps":30,
             "durationMs":1000,"scenes":[{"id":"%s","name":"Garden","durationMs":1000,
             "objects":[],"timeline":[]}]}
            """.formatted(projectId, sceneId));
        Composition composition = new Composition(compositionId, projectId, sceneId, source, 7, Instant.now(), Instant.now());
        when(editor.get(storyId, sceneId)).thenReturn(composition);

        AtomicReference<RenderJob> inserted = new AtomicReference<>();
        when(repository.insert(any(RenderJob.class))).thenAnswer(invocation -> {
            RenderJob j = invocation.getArgument(0);
            inserted.set(j);
            return j;
        });
        when(repository.findById(any())).thenAnswer(invocation -> Optional.ofNullable(inserted.get()));
        when(repository.transition(any(), eq(RenderJobStatus.REQUESTED), eq(RenderJobStatus.QUEUED), eq(0))).thenReturn(true);

        RenderJobService service = new RenderJobService(editor, repository, renderer, storage, mapper, executor);
        RenderJob result = service.request(storyId, sceneId);

        assertEquals(7, result.compositionVersion());
        assertEquals(compositionId, result.compositionId());
        ArgumentCaptor<RenderJob> captor = ArgumentCaptor.forClass(RenderJob.class);
        verify(repository).insert(captor.capture());
        JsonNode captured = captor.getValue().snapshot();

        ((ObjectNode) source.withArray("scenes").get(0)).put("name", "Changed Later");
        assertEquals("Garden", captured.withArray("scenes").get(0).get("name").textValue());
        assertNotNull(queued.get());
        verifyNoInteractions(renderer);
    }

    @Test
    void retryKeepsTheSameJobAndQueuesTheSameImmutableSource() {
        EditorService editor = mock(EditorService.class);
        RenderJobRepository repository = mock(RenderJobRepository.class);
        RendererClient renderer = mock(RendererClient.class);
        ObjectStorage storage = mock(ObjectStorage.class);
        AtomicReference<Runnable> queued = new AtomicReference<>();
        Executor executor = queued::set;

        UUID storyId = UUID.randomUUID();
        UUID sceneId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID compositionId = UUID.randomUUID();
        JsonNode snapshot = mapper.createObjectNode().put("schemaVersion", "1.1");

        RenderJob failed = new RenderJob(jobId, storyId, sceneId, compositionId, 7, snapshot,
                RenderJobStatus.FAILED, 0, 1, "RENDER_FAILED", "renderer failed", null, null, Instant.now(), Instant.now());
        RenderJob queuedJob = new RenderJob(jobId, storyId, sceneId, compositionId, 7, snapshot,
                RenderJobStatus.QUEUED, 0, 2, null, null, null, null, failed.createdAt(), Instant.now());

        when(repository.findByIdAndScene(jobId, storyId, sceneId)).thenReturn(Optional.of(failed), Optional.of(queuedJob));
        when(repository.retry(jobId)).thenReturn(true);
        when(repository.transition(jobId, RenderJobStatus.REQUESTED, RenderJobStatus.QUEUED, 0)).thenReturn(false);
        when(repository.findById(jobId)).thenReturn(Optional.of(queuedJob));

        RenderJobService service = new RenderJobService(editor, repository, renderer, storage, mapper, executor);
        RenderJob result = service.retry(storyId, sceneId, jobId);

        assertEquals(jobId, result.id());
        assertEquals(7, result.compositionVersion());
        assertEquals(2, result.attempt());
        assertSame(snapshot, result.snapshot());
        assertNotNull(queued.get());
    }
}
