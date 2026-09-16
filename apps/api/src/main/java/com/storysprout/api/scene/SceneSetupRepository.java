package com.storysprout.api.scene;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SceneSetupRepository {
    private final JdbcTemplate jdbc;
    public SceneSetupRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public Optional<SceneSetup> findByOutlineSceneId(UUID outlineSceneId) { return jdbc.query("SELECT id,outline_scene_id,background_preset_key,created_at,updated_at FROM scene_setups WHERE outline_scene_id=?",ps->ps.setObject(1,outlineSceneId),(rs,n)->new SceneSetup(rs.getObject("id",UUID.class),rs.getObject("outline_scene_id",UUID.class),rs.getString("background_preset_key"),List.of(),List.of(),List.of(),List.of(),rs.getTimestamp("created_at").toInstant(),rs.getTimestamp("updated_at").toInstant())).stream().findFirst().map(this::load); }
    public SceneSetup insert(UUID id, UUID outlineSceneId) { var now=java.time.Instant.now(); jdbc.update("INSERT INTO scene_setups(id,outline_scene_id,created_at,updated_at) VALUES(?,?,?,?)",id,outlineSceneId,Timestamp.from(now),Timestamp.from(now)); return findByOutlineSceneId(outlineSceneId).orElseThrow(); }
    public SceneSetup updateBackground(UUID setupId,String key) { jdbc.update("UPDATE scene_setups SET background_preset_key=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",key,setupId); return findBySetupId(setupId).orElseThrow(); }
    public SceneSetup saveCharacters(UUID setupId,List<UUID> ids) { jdbc.update("DELETE FROM scene_setup_characters WHERE scene_setup_id=?",setupId); for(int i=0;i<ids.size();i++) jdbc.update("INSERT INTO scene_setup_characters(scene_setup_id,character_id,order_index,created_at) VALUES(?,?,?,CURRENT_TIMESTAMP)",setupId,ids.get(i),i+1); touch(setupId); return findBySetupId(setupId).orElseThrow(); }
    public SceneSetup addProp(UUID setupId,String key) { Integer max=jdbc.queryForObject("SELECT COALESCE(MAX(order_index),0) FROM scene_setup_props WHERE scene_setup_id=?",Integer.class,setupId); UUID id=UUID.randomUUID(); jdbc.update("INSERT INTO scene_setup_props(id,scene_setup_id,prop_preset_key,order_index,created_at) VALUES(?,?,?,?,CURRENT_TIMESTAMP)",id,setupId,key,max+1); touch(setupId); return findBySetupId(setupId).orElseThrow(); }
    public SceneSetup removeProp(UUID setupId,UUID propId) { jdbc.update("DELETE FROM scene_setup_props WHERE id=? AND scene_setup_id=?",propId,setupId); compactProps(setupId); touch(setupId); return findBySetupId(setupId).orElseThrow(); }
    public SceneSetup saveDialogue(UUID setupId,List<SceneSetupDialogue> lines) { jdbc.update("DELETE FROM scene_setup_dialogue WHERE scene_setup_id=?",setupId); for(int i=0;i<lines.size();i++){var d=lines.get(i);jdbc.update("INSERT INTO scene_setup_dialogue(id,scene_setup_id,sequence_index,speaker_type,character_id,text,created_at,updated_at) VALUES(?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",d.id(),setupId,i+1,d.speakerType(),d.characterId(),d.text());} touch(setupId); return findBySetupId(setupId).orElseThrow(); }
    public SceneSetup saveActions(UUID setupId,List<SceneSetupAction> actions) { jdbc.update("DELETE FROM scene_setup_actions WHERE scene_setup_id=?",setupId); for(int i=0;i<actions.size();i++){var a=actions.get(i);jdbc.update("INSERT INTO scene_setup_actions(id,scene_setup_id,character_id,sequence_index,action,created_at) VALUES(?,?,?,?,?,CURRENT_TIMESTAMP)",a.id(),setupId,a.characterId(),i+1,a.action());} touch(setupId); return findBySetupId(setupId).orElseThrow(); }
    public void deleteByOutlineSceneId(UUID outlineSceneId) { jdbc.update("DELETE FROM scene_setups WHERE outline_scene_id=?",outlineSceneId); }
    private Optional<SceneSetup> findBySetupId(UUID id){return jdbc.query("SELECT id,outline_scene_id,background_preset_key,created_at,updated_at FROM scene_setups WHERE id=?",ps->ps.setObject(1,id),(rs,n)->new SceneSetup(rs.getObject("id",UUID.class),rs.getObject("outline_scene_id",UUID.class),rs.getString("background_preset_key"),List.of(),List.of(),List.of(),List.of(),rs.getTimestamp("created_at").toInstant(),rs.getTimestamp("updated_at").toInstant())).stream().findFirst().map(this::load);}
    private SceneSetup load(SceneSetup root){
        var chars=jdbc.query("SELECT character_id,order_index FROM scene_setup_characters WHERE scene_setup_id=? ORDER BY order_index",ps->ps.setObject(1,root.id()),(rs,n)->new SceneSetupCharacter(rs.getObject("character_id",UUID.class),rs.getInt("order_index")));
        var props=jdbc.query("SELECT id,prop_preset_key,order_index FROM scene_setup_props WHERE scene_setup_id=? ORDER BY order_index",ps->ps.setObject(1,root.id()),(rs,n)->new SceneSetupProp(rs.getObject("id",UUID.class),rs.getString("prop_preset_key"),rs.getInt("order_index")));
        var dialogue=jdbc.query("SELECT id,sequence_index,speaker_type,character_id,text FROM scene_setup_dialogue WHERE scene_setup_id=? ORDER BY sequence_index",ps->ps.setObject(1,root.id()),(rs,n)->new SceneSetupDialogue(rs.getObject("id",UUID.class),rs.getInt("sequence_index"),rs.getString("speaker_type"),rs.getObject("character_id",UUID.class),rs.getString("text")));
        var actions=jdbc.query("SELECT id,character_id,sequence_index,action FROM scene_setup_actions WHERE scene_setup_id=? ORDER BY sequence_index",ps->ps.setObject(1,root.id()),(rs,n)->new SceneSetupAction(rs.getObject("id",UUID.class),rs.getObject("character_id",UUID.class),rs.getInt("sequence_index"),rs.getString("action")));
        return new SceneSetup(root.id(),root.outlineSceneId(),root.backgroundPresetKey(),chars,props,dialogue,actions,root.createdAt(),root.updatedAt());
    }
    private void compactProps(UUID setupId){var ids=jdbc.query("SELECT id FROM scene_setup_props WHERE scene_setup_id=? ORDER BY order_index",ps->ps.setObject(1,setupId),(rs,n)->rs.getObject("id",UUID.class));for(int i=0;i<ids.size();i++)jdbc.update("UPDATE scene_setup_props SET order_index=? WHERE id=?",i+1,ids.get(i));}
    private void touch(UUID id){jdbc.update("UPDATE scene_setups SET updated_at=CURRENT_TIMESTAMP WHERE id=?",id);}
}
