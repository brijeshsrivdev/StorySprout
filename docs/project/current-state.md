# StorySprout Current State

**Snapshot:** Story Creation, AI Provider Integration, Story Outline, Characters, and Scene Setup are validated in GitHub Actions. Scene Setup is the latest completed feature on `feature/scene-setup`.

## Existing repository foundation
- Monorepo containing web, API, renderer, and shared packages.
- Next.js web application in `apps/web`.
- Java 21 / Spring Boot API in `apps/api`.
- TypeScript renderer skeleton.
- `packages/editor-model` Composition schema `1.0` remains unchanged.
- PostgreSQL stores application metadata; MinIO is local media storage.
- GitHub Actions CI validates web, API, renderer, and E2E.

## Validated features
- Story Creation — VALIDATED.
- AI Provider Integration — VALIDATED.
- Story Outline — VALIDATED.
- Characters — VALIDATED.
- Scene Setup — VALIDATED.

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
- Editor placeholder handoff only.
- Outline deletion removes Scene Setup before deleting the Outline Scene in the same transaction.
- No AI capability added.

## Persistence
- Existing migrations V1–V4 remain unchanged.
- `V5__scene_setup.sql` creates `scene_setups`, `scene_setup_characters`, `scene_setup_props`, `scene_setup_dialogue`, and `scene_setup_actions`.
- Scene Setup has no duration field.
- Child rows cascade from their Scene Setup root.

## Composition / Renderer boundary
`packages/editor-model` Composition schema `1.0` is unchanged. Scene Setup does not persist transforms, exact timing, keyframes, camera state, audio timing, Timeline clips, or renderer fields. Renderer code is unchanged.

## Validation status
- Story Creation: PASS in merged CI.
- AI Provider Integration: PASS in merged CI.
- Story Outline: PASS in merged CI.
- Characters: PASS in merged CI.
- Scene Setup: PASS in GitHub Actions run `35101048667`.
- Real Gemini smoke test: NOT RUN and not applicable to Scene Setup.

## Not implemented
- Visual Editor/Timeline/Composition persistence/editing.
- Character image generation/upload and advanced asset management.
- Voice/music/media generation.
- Preview/render/FFmpeg/render jobs.
- Direct YouTube publishing.
- Authentication/authorization.
- Payments/collaboration/production deployment.

## Architecture review
Scene Setup is a preparation model between Characters and the future Editor. Story Outline remains authoritative for scene planning; Characters remains authoritative for reusable identity and Story membership; Editor/Timeline will own Composition and exact timing; Renderer consumes Composition JSON. Scene Setup does not create or modify Composition.
