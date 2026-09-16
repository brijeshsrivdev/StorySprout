package com.storysprout.api.outline;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OutlineSceneRepository {
    private static final int TEMP_ORDER_BASE = 1_000_000;
    private final JdbcTemplate jdbc;

    public OutlineSceneRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public OutlineScene insert(OutlineScene scene) {
        jdbc.update("INSERT INTO story_outline_scenes (id, story_id, order_index, title, summary, duration_seconds, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", scene.id(), scene.storyId(), scene.orderIndex(), scene.title(), scene.summary(), scene.durationSeconds(), Timestamp.from(scene.createdAt()), Timestamp.from(scene.updatedAt()));
        return scene;
    }

    public List<OutlineScene> findByStoryId(UUID storyId) {
        return jdbc.query("SELECT id, story_id, order_index, title, summary, duration_seconds, created_at, updated_at FROM story_outline_scenes WHERE story_id = ? ORDER BY order_index ASC", ps -> ps.setObject(1, storyId), (rs, n) -> map(rs)).stream().toList();
    }

    public Optional<OutlineScene> findByIdAndStoryId(UUID sceneId, UUID storyId) {
        return jdbc.query("SELECT id, story_id, order_index, title, summary, duration_seconds, created_at, updated_at FROM story_outline_scenes WHERE id = ? AND story_id = ?", ps -> { ps.setObject(1, sceneId); ps.setObject(2, storyId); }, (rs, n) -> map(rs)).stream().findFirst();
    }

    public OutlineScene update(UUID sceneId, UUID storyId, String title, String summary, int durationSeconds) {
        jdbc.update("UPDATE story_outline_scenes SET title = ?, summary = ?, duration_seconds = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND story_id = ?", title, summary, durationSeconds, sceneId, storyId);
        return findByIdAndStoryId(sceneId, storyId).orElseThrow();
    }

    public void deleteAndCompact(UUID sceneId, UUID storyId) {
        jdbc.update("DELETE FROM story_outline_scenes WHERE id = ? AND story_id = ?", sceneId, storyId);
        List<OutlineScene> remaining = findByStoryId(storyId);
        List<UUID> ids = remaining.stream().map(OutlineScene::id).toList();
        moveToTemporaryPositions(storyId, ids);
        assignPositions(storyId, ids);
    }

    public void reorder(UUID storyId, List<UUID> orderedIds) {
        if (orderedIds.isEmpty()) return;
        moveToTemporaryPositions(storyId, orderedIds);
        assignPositions(storyId, orderedIds);
    }

    private void moveToTemporaryPositions(UUID storyId, List<UUID> ids) {
        for (int i = 0; i < ids.size(); i++) jdbc.update("UPDATE story_outline_scenes SET order_index = ? WHERE id = ? AND story_id = ?", TEMP_ORDER_BASE + i, ids.get(i), storyId);
    }

    private void assignPositions(UUID storyId, List<UUID> ids) {
        for (int i = 0; i < ids.size(); i++) jdbc.update("UPDATE story_outline_scenes SET order_index = ? WHERE id = ? AND story_id = ?", i + 1, ids.get(i), storyId);
    }

    private OutlineScene map(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new OutlineScene(rs.getObject("id", UUID.class), rs.getObject("story_id", UUID.class), rs.getInt("order_index"), rs.getString("title"), rs.getString("summary"), rs.getInt("duration_seconds"), rs.getTimestamp("created_at").toInstant(), rs.getTimestamp("updated_at").toInstant());
    }
}
