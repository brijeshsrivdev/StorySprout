CREATE TABLE render_jobs (
  id UUID PRIMARY KEY,
  story_id UUID NOT NULL REFERENCES stories(id),
  outline_scene_id UUID NOT NULL REFERENCES story_outline_scenes(id) ON DELETE CASCADE,
  composition_id UUID NOT NULL REFERENCES compositions(id),
  composition_version BIGINT NOT NULL CHECK (composition_version >= 1),
  snapshot_json JSONB NOT NULL,
  status VARCHAR(16) NOT NULL CHECK (status IN ('REQUESTED','QUEUED','RENDERING','COMPLETED','FAILED')),
  progress INTEGER NOT NULL DEFAULT 0 CHECK (progress IN (0,50,100)),
  attempt INTEGER NOT NULL DEFAULT 1 CHECK (attempt >= 1),
  failure_code VARCHAR(64),
  failure_message TEXT,
  artifact_key TEXT,
  artifact_size BIGINT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_render_jobs_scene ON render_jobs(story_id, outline_scene_id, created_at DESC);
CREATE INDEX idx_render_jobs_composition ON render_jobs(composition_id, composition_version);
