package com.storysprout.api.story;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectRepository {
    private final JdbcTemplate jdbc;
    public ProjectRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Project insert(Project project) {
        jdbc.update("INSERT INTO projects (id, name, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)", project.id(), project.name(), project.status().name(), Timestamp.from(project.createdAt()), Timestamp.from(project.updatedAt()));
        return project;
    }

    public Optional<Project> findById(UUID id) {
        return jdbc.query("SELECT id, name, status, created_at, updated_at FROM projects WHERE id = ?", ps -> ps.setObject(1, id), (rs, n) -> new Project(rs.getObject("id", UUID.class), rs.getString("name"), ProjectStatus.valueOf(rs.getString("status")), rs.getTimestamp("created_at").toInstant(), rs.getTimestamp("updated_at").toInstant())).stream().findFirst();
    }

    public List<Project> findAllByUpdatedDesc() {
        return jdbc.query("SELECT id, name, status, created_at, updated_at FROM projects ORDER BY updated_at DESC", (rs, n) -> new Project(rs.getObject("id", UUID.class), rs.getString("name"), ProjectStatus.valueOf(rs.getString("status")), rs.getTimestamp("created_at").toInstant(), rs.getTimestamp("updated_at").toInstant()));
    }

    public void touch(UUID id, Instant updatedAt) { jdbc.update("UPDATE projects SET updated_at = ? WHERE id = ?", Timestamp.from(updatedAt), id); }
}
