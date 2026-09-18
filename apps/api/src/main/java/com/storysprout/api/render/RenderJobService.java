package com.storysprout.api.render;

import com.storysprout.api.editor.Composition;
import com.storysprout.api.editor.EditorNotFoundException;
import com.storysprout.api.editor.EditorService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class RenderJobService {
    private final EditorService editor;
    private final RenderJobRepository jobs;
    private final RendererClient renderer;
    private final ObjectStorage storage;
    private final ObjectMapper mapper;
    private final Executor executor;

    public RenderJobService(
            EditorService editor,
            RenderJobRepository jobs,
            RendererClient renderer,
            ObjectStorage storage,
            ObjectMapper mapper,
            @Qualifier("renderExecutor") Executor executor) {
        this.editor = editor;
        this.jobs = jobs;
        this.renderer = renderer;
        this.storage = storage;
        this.mapper = mapper;
        this.executor = executor;
    }

    public RenderJob request(UUID storyId, UUID sceneId) {
        final Composition composition;
        try {
            composition = editor.get(storyId, sceneId);
        } catch (EditorNotFoundException e) {
            throw new RenderJobNotFoundException(e.getMessage());
        }
        JsonNode snapshot = immutableSnapshot(composition.compositionJson());
        RenderJob job = new RenderJob(
                UUID.randomUUID(),
                storyId,
                sceneId,
                composition.id(),
                composition.version(),
                snapshot,
                RenderJobStatus.REQUESTED,
                0,
                1,
                null,
                null,
                null,
                null,
                java.time.Instant.now(),
                java.time.Instant.now());
        RenderJob created = jobs.insert(job);
        queue(created.id());
        return jobs.findById(created.id()).orElseThrow();
    }

    public RenderJob get(UUID storyId, UUID sceneId, UUID jobId) {
        return jobs.findByIdAndScene(jobId, storyId, sceneId)
                .orElseThrow(() -> new RenderJobNotFoundException("RenderJob not found"));
    }

    public RenderJob retry(UUID storyId, UUID sceneId, UUID jobId) {
        RenderJob job = get(storyId, sceneId, jobId);
        if (job.status() != RenderJobStatus.FAILED) {
            throw new RenderJobConflictException("Only failed RenderJobs can be retried");
        }
        if (!jobs.retry(jobId)) {
            throw new RenderJobConflictException("RenderJob changed before retry could start");
        }
        queue(jobId);
        return get(storyId, sceneId, jobId);
    }

    public java.io.InputStream artifact(UUID storyId, UUID sceneId, UUID jobId) {
        RenderJob job = get(storyId, sceneId, jobId);
        if (job.status() != RenderJobStatus.COMPLETED || job.artifactKey() == null) {
            throw new RenderJobConflictException("Render artifact is not available yet");
        }
        try {
            return storage.get(job.artifactKey());
        } catch (IOException e) {
            throw new RenderJobException("Render artifact could not be retrieved", e);
        }
    }

    private void queue(UUID jobId) {
        if (!jobs.transition(jobId, RenderJobStatus.REQUESTED, RenderJobStatus.QUEUED, 0)
                && !jobs.findById(jobId).map(j -> j.status() == RenderJobStatus.QUEUED).orElse(false)) {
            throw new RenderJobException("Unable to queue RenderJob");
        }
        try {
            executor.execute(() -> execute(jobId));
        } catch (RejectedExecutionException e) {
            jobs.fail(jobId, RenderJobStatus.QUEUED, "RENDER_QUEUE_REJECTED", "Render queue is full");
            throw new RenderJobException("Render queue is full", e);
        }
    }

    private void execute(UUID jobId) {
        if (!jobs.transition(jobId, RenderJobStatus.QUEUED, RenderJobStatus.RENDERING, 50)) {
            return;
        }
        try {
            RenderJob job = jobs.findById(jobId).orElseThrow();
            byte[] artifact = renderer.render(job);
            String key = "renders/" + job.id() + "/attempt-" + job.attempt() + ".mp4";
            storage.put(key, new ByteArrayInputStream(artifact), artifact.length, "video/mp4");
            if (!jobs.complete(job.id(), key, artifact.length)) {
                throw new RenderJobException("RenderJob completion was rejected");
            }
        } catch (Exception e) {
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            jobs.fail(jobId, RenderJobStatus.RENDERING, "RENDER_FAILED", message);
        }
    }

    private JsonNode immutableSnapshot(JsonNode source) {
        try {
            return mapper.readTree(mapper.writeValueAsString(source));
        } catch (Exception e) {
            throw new RenderJobException("Unable to snapshot Composition", e);
        }
    }
}
