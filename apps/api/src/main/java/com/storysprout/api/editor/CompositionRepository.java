package com.storysprout.api.editor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CompositionRepository {
    private final JdbcTemplate jdbc; private final ObjectMapper mapper;
    public CompositionRepository(JdbcTemplate jdbc,ObjectMapper mapper){this.jdbc=jdbc;this.mapper=mapper;}
    public Optional<Composition> find(UUID projectId,UUID outlineSceneId){return jdbc.query("SELECT id,project_id,outline_scene_id,composition_json,version,created_at,updated_at FROM compositions WHERE project_id=? AND outline_scene_id=?",ps->{ps.setObject(1,projectId);ps.setObject(2,outlineSceneId);},(rs,n)->new Composition(rs.getObject("id",UUID.class),rs.getObject("project_id",UUID.class),rs.getObject("outline_scene_id",UUID.class),parse(rs.getString("composition_json")),rs.getLong("version"),rs.getTimestamp("created_at").toInstant(),rs.getTimestamp("updated_at").toInstant())).stream().findFirst();}
    public Composition insert(UUID id,UUID projectId,UUID outlineSceneId,JsonNode json){jdbc.update("INSERT INTO compositions(id,project_id,outline_scene_id,schema_version,composition_json,version,created_at,updated_at) VALUES(?,?,?,?,?::jsonb,1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP) ON CONFLICT(project_id,outline_scene_id) DO NOTHING",id,projectId,outlineSceneId,"1.1",json.toString());return find(projectId,outlineSceneId).orElseThrow();}
    public Optional<Composition> update(UUID id,UUID projectId,UUID outlineSceneId,long expectedVersion,JsonNode json){int count=jdbc.update("UPDATE compositions SET composition_json=?::jsonb,schema_version='1.1',version=version+1,updated_at=CURRENT_TIMESTAMP WHERE id=? AND project_id=? AND outline_scene_id=? AND version=?",json.toString(),id,projectId,outlineSceneId,expectedVersion);return count==1?find(projectId,outlineSceneId):Optional.empty();}
    private JsonNode parse(String value){try{return mapper.readTree(value);}catch(Exception e){throw new IllegalStateException("Stored Composition JSON is invalid",e);}}
}
