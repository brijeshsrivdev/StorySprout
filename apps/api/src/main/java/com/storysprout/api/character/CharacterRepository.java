package com.storysprout.api.character;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CharacterRepository {
    private final JdbcTemplate jdbc;
    public CharacterRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Character insert(Character c) {
        jdbc.update("INSERT INTO characters (id, project_id, name, role_description, category, visual_description, personality, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", c.id(), c.projectId(), c.name(), c.roleDescription(), c.category().name(), c.visualDescription(), c.personality(), Timestamp.from(c.createdAt()), Timestamp.from(c.updatedAt()));
        return c;
    }
    public List<Character> findByProjectId(UUID projectId) {
        return jdbc.query("SELECT id, project_id, name, role_description, category, visual_description, personality, created_at, updated_at FROM characters WHERE project_id=? ORDER BY name ASC, id ASC", ps -> ps.setObject(1, projectId), (rs,n) -> map(rs)).stream().toList();
    }
    public Optional<Character> findById(UUID id) {
        return jdbc.query("SELECT id, project_id, name, role_description, category, visual_description, personality, created_at, updated_at FROM characters WHERE id=?", ps -> ps.setObject(1,id), (rs,n)->map(rs)).stream().findFirst();
    }
    public Optional<Character> findByIdAndProjectId(UUID id, UUID projectId) {
        return jdbc.query("SELECT id, project_id, name, role_description, category, visual_description, personality, created_at, updated_at FROM characters WHERE id=? AND project_id=?", ps->{ps.setObject(1,id);ps.setObject(2,projectId);}, (rs,n)->map(rs)).stream().findFirst();
    }
    public Character update(UUID id, UUID projectId, String name, String role, CharacterCategory category, String visual, String personality) {
        jdbc.update("UPDATE characters SET name=?, role_description=?, category=?, visual_description=?, personality=?, updated_at=CURRENT_TIMESTAMP WHERE id=? AND project_id=?", name, role, category.name(), visual, personality, id, projectId);
        return findByIdAndProjectId(id, projectId).orElseThrow();
    }
    public void delete(UUID id, UUID projectId) { jdbc.update("DELETE FROM characters WHERE id=? AND project_id=?", id, projectId); }
    public boolean isUsed(UUID id) { return jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM story_characters WHERE character_id=?)", Boolean.class, id); }
    public void addToStory(UUID storyId, UUID characterId) { jdbc.update("INSERT INTO story_characters(story_id, character_id, created_at) VALUES (?,?,CURRENT_TIMESTAMP)", storyId, characterId); }
    public boolean membershipExists(UUID storyId, UUID characterId) { return jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM story_characters WHERE story_id=? AND character_id=?)", Boolean.class, storyId, characterId); }
    public List<Character> findByStoryId(UUID storyId) { return jdbc.query("SELECT c.id,c.project_id,c.name,c.role_description,c.category,c.visual_description,c.personality,c.created_at,c.updated_at FROM characters c JOIN story_characters sc ON sc.character_id=c.id WHERE sc.story_id=? ORDER BY c.name ASC,c.id ASC", ps->ps.setObject(1,storyId),(rs,n)->map(rs)).stream().toList(); }
    public void removeFromStory(UUID storyId, UUID characterId) { jdbc.update("DELETE FROM story_characters WHERE story_id=? AND character_id=?", storyId, characterId); }
    private Character map(java.sql.ResultSet rs) throws java.sql.SQLException { return new Character(rs.getObject("id",UUID.class),rs.getObject("project_id",UUID.class),rs.getString("name"),rs.getString("role_description"),CharacterCategory.valueOf(rs.getString("category")),rs.getString("visual_description"),rs.getString("personality"),rs.getTimestamp("created_at").toInstant(),rs.getTimestamp("updated_at").toInstant()); }
}
