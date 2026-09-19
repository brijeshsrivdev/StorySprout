# Render Implementation

## Scope
Render establishes the first real MP4 artifact path from a canonical Composition 1.1 snapshot.

## Data model
Migration `V7__render_jobs.sql` adds `render_jobs`.

The RenderJob stores:
- Story/Scene relationship
- Composition identity
- exact Composition version
- immutable JSON snapshot
- lifecycle status
- coarse progress
- execution attempt
- failure information
- artifact reference/size
- timestamps

The snapshot is stored in PostgreSQL JSONB. It is never replaced when the source Composition changes.

## Orchestration
`RenderJobService`:
1. resolves the canonical Composition through existing Editor/Story/Scene boundaries
2. deep-copies its JSON into an immutable snapshot
3. creates a REQUESTED RenderJob
4. transitions it to QUEUED
5. submits it to the bounded render executor
6. transitions it to RENDERING
7. invokes the renderer with the persisted snapshot/version
8. stores the returned MP4 through ObjectStorage
9. marks the job COMPLETED

Any renderer/storage failure marks the job FAILED without modifying Composition.

## Retry
Retry is available only for FAILED jobs.

Retry:
- keeps the same RenderJob
- keeps the same Composition id/version
- keeps the same immutable snapshot
- clears prior failure/artifact metadata
- increments `attempt`
- creates a new queued execution

A retry never resolves a newer Composition.

## Renderer
The existing renderer service was extended rather than replaced.

`services/renderer/src/render.ts`:
- validates Composition 1.1 boundaries
- uses the shared editor-model scene interpreter
- maps deterministic background/object visuals
- constructs an FFmpeg filter graph
- generates static 1920×1080 frames
- encodes H.264 MP4
- writes MP4 to the HTTP response

The renderer has no PostgreSQL dependency and no RenderJob persistence.

## Storage
Added application-level `ObjectStorage`.

Local adapter:
`MinioObjectStorage`

Configured by:
- `STORYSPROUT_STORAGE_ENDPOINT`
- `STORYSPROUT_STORAGE_ACCESS_KEY`
- `STORYSPROUT_STORAGE_SECRET_KEY`
- `STORYSPROUT_STORAGE_BUCKET`

MP4 binary data is not stored in PostgreSQL.

## API
- POST RenderJob
- GET RenderJob
- POST retry
- GET artifact

All application endpoints use the existing `/api/v1` path and `{ "data": ... }` success envelope.

## UI
The existing Editor was not redesigned.

Added:
- Render button
- queued/rendering/completed/failed status strip
- coarse progress
- retry for failed jobs
- completed MP4 link

Render is disabled while the Composition has unsaved edits.

## Tests
Added:
- RenderJob lifecycle transition tests
- RenderJob snapshot/version unit test
- renderer Composition validation test
- deterministic MP4 test
- 1920×1080/30fps output verification
- Timeline rejection test

Full repository execution was unavailable in the current environment.
