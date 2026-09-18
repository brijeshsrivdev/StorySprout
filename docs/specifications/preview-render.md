# Preview + Render Foundation Specification

## Status
Implemented on `feature/storysprout-ui-foundation`.

## Purpose
Establish the first complete creator outcome from the canonical Editor Composition:

**Editor → Preview → Render → finished MP4**

Composition 1.1 remains the only source of truth. Preview and Render consume the same Composition semantics.

## Preview contract
Preview loads the persisted Composition through the existing Editor context endpoint. It is intentionally static in V1.

Preview must reflect:
- background preset
- object semantic type and visual reference
- object position
- object scale
- object visibility
- object array/layer order

Preview must not introduce timing, animation, Timeline behavior or a second scene model.

Preview states:
- loading
- ready
- empty Composition
- recoverable load/validation error
- return to Editor

## RenderJob contract
Spring Boot owns RenderJob lifecycle and PostgreSQL persistence.

Lifecycle:
`REQUESTED → QUEUED → RENDERING → COMPLETED`

Failure:
`REQUESTED | QUEUED | RENDERING → FAILED`

Retry:
`FAILED → QUEUED`

Retry preserves the original immutable snapshot and Composition version and increments the execution attempt. Completed jobs are not retried.

A RenderJob records:
- id
- Story id
- Outline Scene id
- Composition id
- exact Composition version
- immutable Composition snapshot JSON
- status
- coarse progress (0, 50, 100)
- execution attempt
- failure code/message
- artifact key and size
- timestamps

## Snapshot/version semantics
When a render is requested:
1. Load the current canonical Composition.
2. Capture its persisted version.
3. Serialize a deep immutable snapshot.
4. Persist that snapshot with the RenderJob.
5. Renderer receives that exact snapshot.
6. Renderer never reads the current Composition from PostgreSQL.

A later Composition version cannot change an existing RenderJob.

Permanent rule:

**RenderJob → Composition Version → immutable Snapshot → Artifact**

## Renderer contract
Endpoint:
`POST /render`

Request:
- RenderJob id
- Composition id
- Composition version
- canonical Composition 1.1 snapshot

Response:
- `200 application/octet-stream` / MP4 bytes
- `422` for unsupported/invalid render input or renderer failure

The renderer:
- is independent of PostgreSQL
- never mutates Composition
- validates schema/stage/fps/timeline boundaries
- interprets Composition using the shared editor-model semantics
- generates deterministic static placeholder visuals
- invokes FFmpeg
- produces 1920×1080, 30fps, 16:9 MP4

## ObjectStorage contract
The application exposes only:
- `put(key, stream, length, contentType)`
- `get(key)`

The local implementation uses MinIO/S3-compatible storage. The application does not expose MinIO-specific types outside the adapter.

PostgreSQL stores artifact metadata/reference, never MP4 binary data.

## API contract
Base path remains `/api/v1` and application responses use the existing `{ "data": ... }` envelope.

Endpoints:
- `POST /stories/{storyId}/outline-scenes/{sceneId}/render-jobs`
- `GET /stories/{storyId}/outline-scenes/{sceneId}/render-jobs/{jobId}`
- `POST /stories/{storyId}/outline-scenes/{sceneId}/render-jobs/{jobId}/retry`
- `GET /stories/{storyId}/outline-scenes/{sceneId}/render-jobs/{jobId}/artifact`

Nested Story/Scene identity is required for RenderJob access so a job cannot be retrieved through an unrelated Story/Scene path.

## Async execution
V1 uses a bounded in-process Spring executor:
- core threads: 1
- maximum threads: 2
- queue capacity: 8

No Redis, Kafka, RabbitMQ, cloud queue or Kubernetes job infrastructure is introduced.

## Artifact lifecycle
A successful execution writes:
`renders/{renderJobId}/attempt-{attempt}.mp4`

The RenderJob is marked COMPLETED only after the artifact has been stored successfully.

A failed execution preserves the source Composition and RenderJob snapshot.

## Supported V1 rendering
- Composition schema 1.1
- one scene
- 1920×1080
- 30fps
- static background presets
- CHARACTER and PROP objects
- position
- scale
- visibility
- object/layer order
- deterministic placeholder visuals
- MP4/H.264 output

## Explicit non-goals
- Timeline
- keyframes
- animation
- character/facial animation
- audio/music/SFX
- lip-sync
- AI video generation
- advanced camera system
- YouTube publishing
- thumbnails
- analytics
- collaboration
- marketplace
- 4K
- mobile

## Security/business boundary
Render creation requires a valid Story + Outline Scene relationship through existing repositories and the canonical Composition lookup. No new authorization framework is introduced. Authentication/authorization remains an existing repository boundary and is not invented by this milestone.

## Observability
Render execution logs can identify:
- RenderJob id
- Composition id/version
- renderer invocation/failure
- artifact key/creation

No separate observability platform is introduced.
