# StorySprout Current State

**Snapshot:** Story Creation, AI Provider Integration, Story Outline, Characters, and Scene Setup are validated. SPEC-006 Editor implementation is now present on `feature/editor`; final CI validation is pending.

## Existing repository foundation
- Monorepo containing web, API, renderer, and shared packages.
- Next.js web application in `apps/web`.
- Java 21 / Spring Boot API in `apps/api`.
- TypeScript renderer skeleton.
- `packages/editor-model` now defines Composition schema `1.1` with explicit `SceneObject.objectType`.
- PostgreSQL stores application metadata; MinIO is local media storage.
- GitHub Actions CI validates web, API, renderer, and E2E.

## Validated features
- Story Creation — VALIDATED.
- AI Provider Integration — VALIDATED.
- Story Outline — VALIDATED.
- Characters — VALIDATED.
- Scene Setup — VALIDATED.

## SPEC-006 Editor
**Status:** IN_PROGRESS — implementation complete on feature branch; CI validation pending
Specification: `docs/specifications/editor.md`
Final clarification: `docs/specifications/editor-clarifications.md`
Implementation memory: `docs/features/editor/implementation.md`

Implemented on the feature branch:
- One Outline Scene per Editor session using `storyId + outlineSceneId`.
- Scene Setup → Composition one-time initialization and idempotent reopen behavior.
- Existing Composition remains authoritative after initialization.
- Composition schema `1.1` with explicit `SceneObject.objectType = CHARACTER | PROP`.
- Explicit legacy 1.0 → 1.1 migration utility requiring complete caller-supplied semantic types; no inference from asset IDs/names/URLs.
- Fixed logical 1920×1080 stage and viewport-to-logical transform conversion.
- Character/Prop selection, move, uniform scale, visibility, delete and layer ordering.
- Add Story Characters and controlled Props without creating library/Scene Setup records.
- Contextual properties and controlled background editing.
- Explicit Save with dirty/saving/failure/conflict states and optimistic versioning.
- Empty Timeline boundary; no clips/timing/animation/dialogue/audio state is created.
- Dialogue and Action Intent remain Scene Setup-owned data.
- Composition persistence in PostgreSQL JSONB.
- Outline Scene deletion cascades to subordinate Composition.

## Scene Setup
**Status:** VALIDATED
Specification: `docs/specifications/scene-setup.md`
Implementation memory: `docs/features/scene-setup/implementation.md`

Implemented:
- One Outline Scene at a time using stable Outline Scene ID.
- Read-only Story/Outline scene context including planned duration.
- One persisted Scene Setup per Outline Scene; empty setup is valid.
- Controlled deterministic background preset catalog with select/replace/remove.
- Existing Story Character selection with duplicate prevention.
- Scene-local controlled prop instances; repeated presets supported.
- Manual ordered Character/Narrator dialogue with reorder controls.
- Fixed action intents: IDLE, TALK, WALK, RUN, WAVE, SIT, JUMP, with reorder controls.
- Explicit Save Changes for editable setup sections.
- Previous/Next scene navigation.
- Editor now replaces the former placeholder through SPEC-006 implementation.
- Outline deletion removes Scene Setup before deleting the Outline Scene in the same transaction.
- No AI capability added.

## Persistence
- Existing migrations V1–V5 remain unchanged.
- `V6__editor_composition.sql` creates `compositions` with JSONB canonical state, Project/Outline Scene ownership, schema version, optimistic version and timestamps.
- `(project_id, outline_scene_id)` is unique.
- Outline Scene FK uses `ON DELETE CASCADE` for subordinate Composition cleanup.
- No normalized Composition-object/layer/timeline tables and no autosave.

## Composition / Renderer boundary
`packages/editor-model` Composition schema `1.1` is now executable. `SceneObject.objectType` is canonical semantic state; `assetId` remains the visual reference only. Editor-created Composition uses fixed 1920×1080 dimensions, 30 fps, explicit object types and an empty Timeline. Renderer implementation remains unchanged and continues to consume versioned Composition JSON.

## Validation status
- Story Creation: PASS in merged CI.
- AI Provider Integration: PASS in merged CI.
- Story Outline: PASS in merged CI.
- Characters: PASS in merged CI.
- Scene Setup: PASS in GitHub Actions run `35101048667`.
- Editor model tests: added; CI validation pending.
- Editor backend tests: added; CI validation pending.
- Editor E2E: implemented for Story → Outline → Characters → Scene Setup → Editor → move → scale → Save → reload; CI validation pending.
- Full repository CI for Editor branch: PENDING.
- Local Maven/Node/Docker validation: NOT RUN because the execution environment cannot reach the repository/build services.
- Real Gemini smoke test: NOT RUN; not part of Editor validation.

## Not implemented
- Timeline.
- Playback timing, keyframes, audio timing, lip-sync, animation generation/playback.
- Character image generation/upload and advanced asset management.
- Voice/music/media generation.
- Preview/render/FFmpeg/render jobs.
- Direct YouTube publishing.
- Authentication/authorization.
- Payments/collaboration/production deployment.

## Architecture review
Scene Setup remains the preparation model between Characters and Editor. Story Outline remains authoritative for narrative planning; Characters remains authoritative for reusable identity and Story membership; Scene Setup remains preparation data; Editor/Composition owns canonical editable visual state; future Timeline will own exact timing; Renderer consumes versioned Composition JSON. No bidirectional Scene Setup ↔ Composition synchronization is implemented. ADR-014 is now implemented through schema `1.1` and explicit semantic object typing.
