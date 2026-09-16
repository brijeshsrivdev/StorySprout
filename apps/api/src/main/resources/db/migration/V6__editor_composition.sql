CREATE TABLE compositions (
  id UUID PRIMARY KEY,
  project_id UUID NOT NULL REFERENCES projects(id),
  outline_scene_id UUID NOT NULL REFERENCES story_outline_scenes(id) ON DELETE CASCADE,
  schema_version VARCHAR(16) NOT NULL CHECK (schema_version = '1.1'),
  composition_json JSONB NOT NULL,
  version BIGINT NOT NULL DEFAULT 1 CHECK (version >= 1),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_compositions_project_scene UNIQUE (project_id, outline_scene_id)
);
CREATE INDEX idx_compositions_project_id ON compositions(project_id);
