package com.storysprout.api.story;

import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StoryRepository {
    private final JdbcTemplate jdbc;
    public StoryRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Story insert(Story story) {
        jdbc.update("INSERT INTO stories (id, project_id, title, idea, target_age, duration_minutes, visual_style, language, creation_mode, generation_status, draft_content, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", story.id(), story.projectId(), story.title(), story.idea(), story.targetAge(), story.durationMinutes(), story.visualStyle(), story.language(), story.creationMode().name(), story.generationStatus().name(), story.draftContent(), Timestamp.from(story.createdAt()), Timestamp.from(story.updatedAt()));
        return story;
    }

    public Story updateGeneration(UUID id, StoryGenerationStatus status, String title, String draft) {
        jdbc.update("UPDATE stories SET generation_status = ?, title = COALESCE(?, title), draft_content = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?", status.name(), title, draft, id);
        return findById(id).orElseThrow();
    }

    public java.util.Optional<Story> findById(UUID id) {
        return jdbc.query("SELECT id, project_id, title, idea, target_age, duration_minutes, visual_style, language, creation_mode, generation_status, draft_content, created_at, updated_at FROM stories WHERE id = ?", ps -> ps.setObject(1, id), (rs, n) -> new Story(rs.getObject("id", UUID.class), rs.getObject("project_id", UUID.class), rs.getString("title"), rs.getString("idea"), rs.getString("target_age"), rs.getInt("duration_minutes"), rs.getString("visual_style"), rs.getString("language"), StoryCreationMode.valueOf(rs.getString("creation_mode")), StoryGenerationStatus.valueOf(rs.getString("generation_status")), rs.getString("draft_content"), rs.getTimestamp("created_at").toInstant(), rs.getTimestamp("updated_at").toInstant())).stream().findFirst();
    }
}
