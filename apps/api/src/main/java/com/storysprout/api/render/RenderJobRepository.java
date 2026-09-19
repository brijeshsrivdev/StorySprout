package com.storysprout.api.render;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Repository
public class RenderJobRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public RenderJobRepository(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public RenderJob insert(RenderJob job) {
        jdbc.update("""
            INSERT INTO render_jobs
            (id, story_id, outline_scene_id, composition_id, composition_version, snapshot_json,
             status, progress, attempt, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
            job.id(), job.storyId(), job.outlineSceneId(), job.compositionId(), job.compositionVersion(),
            job.snapshot().toString(), job.status().name(), job.progress(), job.attempt());
        return findById(job.id()).orElseThrow();
    }

    public Optional<RenderJob> findById(UUID id) {
        return jdbc.query("""
            SELECT id, story_id, outline_scene_id, composition_id, composition_version, snapshot_json,
                   status, progress, attempt, failure_code, failure_message, artifact_key, artifact_size,
                   created_at, updated_at
            FROM render_jobs WHERE id=?
            """, ps -> ps.setObject(1, id), (rs, row) -> map(rs)).stream().findFirst();
    }

    public Optional<RenderJob> findByIdAndScene(UUID id, UUID storyId, UUID sceneId) {
        return jdbc.query("""
            SELECT id, story_id, outline_scene_id, composition_id, composition_version, snapshot_json,
                   status, progress, attempt, failure_code, failure_message, artifact_key, artifact_size,
                   created_at, updated_at
            FROM render_jobs
            WHERE id=? AND story_id=? AND outline_scene_id=?
            """, ps -> {
                ps.setObject(1, id);
                ps.setObject(2, storyId);
                ps.setObject(3, sceneId);
            }, (rs, row) -> map(rs)).stream().findFirst();
    }

    public boolean transition(UUID id, RenderJobStatus expected, RenderJobStatus next, int progress) {
        return jdbc.update("""
            UPDATE render_jobs
            SET status=?, progress=?, updated_at=CURRENT_TIMESTAMP
            WHERE id=? AND status=?
            """, next.name(), progress, id, expected.name()) == 1;
    }

    public boolean fail(UUID id, RenderJobStatus expected, String code, String message) {
        return jdbc.update("""
            UPDATE render_jobs
            SET status='FAILED', progress=0, failure_code=?, failure_message=?, updated_at=CURRENT_TIMESTAMP
            WHERE id=? AND status=?
            """, code, message, id, expected.name()) == 1;
    }

    public boolean complete(UUID id, String artifactKey, long artifactSize) {
        return jdbc.update("""
            UPDATE render_jobs
            SET status='COMPLETED', progress=100, artifact_key=?, artifact_size=?,
                failure_code=NULL, failure_message=NULL, updated_at=CURRENT_TIMESTAMP
            WHERE id=? AND status='RENDERING'
            """, artifactKey, artifactSize, id) == 1;
    }

    public boolean retry(UUID id) {
        return jdbc.update("""
            UPDATE render_jobs
            SET status='QUEUED', progress=0, attempt=attempt+1,
                failure_code=NULL, failure_message=NULL, artifact_key=NULL, artifact_size=NULL,
                updated_at=CURRENT_TIMESTAMP
            WHERE id=? AND status='FAILED'
            """, id) == 1;
    }

    private RenderJob map(ResultSet rs) throws SQLException {
        try {
            return new RenderJob(
                    rs.getObject("id", UUID.class),
                    rs.getObject("story_id", UUID.class),
                    rs.getObject("outline_scene_id", UUID.class),
                    rs.getObject("composition_id", UUID.class),
                    rs.getLong("composition_version"),
                    mapper.readTree(rs.getString("snapshot_json")),
                    RenderJobStatus.valueOf(rs.getString("status")),
                    rs.getInt("progress"),
                    rs.getInt("attempt"),
                    rs.getString("failure_code"),
                    rs.getString("failure_message"),
                    rs.getString("artifact_key"),
                    rs.getObject("artifact_size", Long.class),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant());
        } catch (Exception e) {
            throw new IllegalStateException("Stored RenderJob snapshot is invalid", e);
        }
    }
}
