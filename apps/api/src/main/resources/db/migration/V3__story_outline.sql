CREATE TABLE story_outline_scenes (
    id UUID PRIMARY KEY,
    story_id UUID NOT NULL REFERENCES stories(id),
    order_index INTEGER NOT NULL CHECK (order_index > 0),
    title VARCHAR(200) NOT NULL CHECK (btrim(title) <> ''),
    summary VARCHAR(2000) NOT NULL CHECK (btrim(summary) <> ''),
    duration_seconds INTEGER NOT NULL CHECK (duration_seconds > 0),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_story_outline_scene_order UNIQUE (story_id, order_index)
);

CREATE INDEX idx_story_outline_scenes_story_order
    ON story_outline_scenes (story_id, order_index);
