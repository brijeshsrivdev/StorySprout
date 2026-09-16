CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT projects_name_not_blank CHECK (length(btrim(name)) > 0),
    CONSTRAINT projects_status_check CHECK (status IN ('DRAFT', 'READY'))
);

CREATE INDEX idx_projects_updated_at_desc ON projects (updated_at DESC);

CREATE TABLE stories (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    idea TEXT NOT NULL,
    target_age VARCHAR(10) NOT NULL,
    duration_minutes SMALLINT NOT NULL,
    visual_style VARCHAR(10) NOT NULL,
    language VARCHAR(10) NOT NULL,
    creation_mode VARCHAR(10) NOT NULL,
    generation_status VARCHAR(20) NOT NULL,
    draft_content TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT stories_project_fk FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT stories_idea_not_blank CHECK (length(btrim(idea)) > 0),
    CONSTRAINT stories_target_age_check CHECK (target_age IN ('3_5', '6_8', '9_12')),
    CONSTRAINT stories_duration_check CHECK (duration_minutes IN (1, 3, 5)),
    CONSTRAINT stories_visual_style_check CHECK (visual_style IN ('2D', '3D', 'HYBRID')),
    CONSTRAINT stories_language_check CHECK (language IN ('ENGLISH', 'HINDI')),
    CONSTRAINT stories_creation_mode_check CHECK (creation_mode IN ('AI', 'BLANK')),
    CONSTRAINT stories_generation_status_check CHECK (generation_status IN ('NOT_REQUESTED', 'GENERATING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_stories_project_id ON stories (project_id);
