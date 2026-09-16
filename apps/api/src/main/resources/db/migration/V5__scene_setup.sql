CREATE TABLE scene_setups (
    id UUID PRIMARY KEY,
    outline_scene_id UUID NOT NULL UNIQUE REFERENCES story_outline_scenes(id),
    background_preset_key VARCHAR(80),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE scene_setup_characters (
    scene_setup_id UUID NOT NULL REFERENCES scene_setups(id) ON DELETE CASCADE,
    character_id UUID NOT NULL REFERENCES characters(id),
    order_index INTEGER NOT NULL CHECK (order_index > 0),
    created_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (scene_setup_id, character_id),
    UNIQUE (scene_setup_id, order_index)
);
CREATE INDEX idx_scene_setup_characters_character ON scene_setup_characters(character_id);
CREATE TABLE scene_setup_props (
    id UUID PRIMARY KEY,
    scene_setup_id UUID NOT NULL REFERENCES scene_setups(id) ON DELETE CASCADE,
    prop_preset_key VARCHAR(80) NOT NULL,
    order_index INTEGER NOT NULL CHECK (order_index > 0),
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (scene_setup_id, order_index)
);
CREATE TABLE scene_setup_dialogue (
    id UUID PRIMARY KEY,
    scene_setup_id UUID NOT NULL REFERENCES scene_setups(id) ON DELETE CASCADE,
    sequence_index INTEGER NOT NULL CHECK (sequence_index > 0),
    speaker_type VARCHAR(20) NOT NULL CHECK (speaker_type IN ('CHARACTER','NARRATOR')),
    character_id UUID REFERENCES characters(id),
    text VARCHAR(2000) NOT NULL CHECK (btrim(text) <> ''),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (scene_setup_id, sequence_index)
);
CREATE TABLE scene_setup_actions (
    id UUID PRIMARY KEY,
    scene_setup_id UUID NOT NULL REFERENCES scene_setups(id) ON DELETE CASCADE,
    character_id UUID NOT NULL REFERENCES characters(id),
    sequence_index INTEGER NOT NULL CHECK (sequence_index > 0),
    action VARCHAR(20) NOT NULL CHECK (action IN ('IDLE','TALK','WALK','RUN','WAVE','SIT','JUMP')),
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (scene_setup_id, sequence_index)
);
CREATE INDEX idx_scene_setup_actions_character ON scene_setup_actions(character_id);
