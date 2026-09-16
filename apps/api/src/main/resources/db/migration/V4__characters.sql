CREATE TABLE characters (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id),
    name VARCHAR(120) NOT NULL CHECK (btrim(name) <> ''),
    role_description VARCHAR(1000) NOT NULL CHECK (btrim(role_description) <> ''),
    category VARCHAR(20) NOT NULL CHECK (category IN ('CHILD','ADULT','ANIMAL','FANTASY','OBJECT','OTHER')),
    visual_description VARCHAR(2000) NOT NULL CHECK (btrim(visual_description) <> ''),
    personality VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_characters_project ON characters(project_id);
CREATE INDEX idx_characters_updated ON characters(project_id, updated_at DESC);

CREATE TABLE story_characters (
    story_id UUID NOT NULL REFERENCES stories(id),
    character_id UUID NOT NULL REFERENCES characters(id),
    created_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (story_id, character_id)
);
CREATE INDEX idx_story_characters_character ON story_characters(character_id);
