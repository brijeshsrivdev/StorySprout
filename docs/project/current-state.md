# StorySprout Current State

**Snapshot:** Story Creation, AI Provider Integration, Story Outline, Characters, and Scene Setup are validated in GitHub Actions. SPEC-006 Editor is now specified; implementation has not started.

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

## SPEC-006 Editor
**Status:** SPECIFIED — implementation not started
Specification: `docs/specifications/editor.md`

Defined by the specification:
- One Outline Scene per Editor session using `storyId + outlineSceneId`.
- Scene Setup initializes a Composition only when one does not already exist.
- Existing Composition is authoritative on reopen; no silent Scene Setup synchronization.
- Existing Composition schema `1.0` is sufficient; no model change is specified.
- 16:9 / 1920×1080 logical stage with Character and Prop objects.
- Basic selection, move, uniform scale, visibility, deletion, adding available Characters/Props, and simple layer ordering.
- Explicit Save with dirty/error/conflict states and optimistic Composition versioning.
- Composition JSON persisted as the canonical document in PostgreSQL with a root `compositions` record.
- Timeline, animation, dialogue/audio timing, and advanced authoring remain separate/deferred.

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
- Composition persistence is not implemented yet; SPEC-006 proposes a new `compositions` JSONB root table for the implementation phase.

## Composition / Renderer boundary
`packages/editor-model` Composition schema `1.0` is unchanged. Scene Setup does not persist transforms, exact timing, keyframes, camera state, audio timing, Timeline clips, or renderer fields. Renderer code is unchanged.

## Validation status
- Story Creation: PASS in merged CI.
- AI Provider Integration: PASS in merged CI.
- Story Outline: PASS in merged CI.
- Characters: PASS in merged CI.
- Scene Setup: PASS in GitHub Actions run `35101048667`.
- SPEC-006 Editor: SPECIFIED only; implementation validation NOT RUN because implementation has not started.
- Real Gemini smoke test: NOT RUN and not applicable to Scene Setup.

## Not implemented
- Visual Editor implementation/Composition persistence.
- Timeline.
- Character image generation/upload and advanced asset management.
- Voice/music/media generation.
- Preview/render/FFmpeg/render jobs.
- Direct YouTube publishing.
- Authentication/authorization.
- Payments/collaboration/production deployment.

## Architecture review
Scene Setup is the preparation model between Characters and Editor. Story Outline remains authoritative for narrative planning; Characters remains authoritative for reusable identity and Story membership; Scene Setup remains preparation data; Editor/Composition will own canonical editable visual state; future Timeline will own exact timing; Renderer consumes Composition JSON. No bidirectional Scene Setup ↔ Composition synchronization is permitted by SPEC-006.
