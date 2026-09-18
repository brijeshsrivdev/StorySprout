# StorySprout Current State

**Snapshot:** Story Creation, AI Provider Integration, Story Outline, Characters, Scene Setup, and SPEC-006 Editor are validated on `main`. The UI foundation branch is incrementally refining the existing creator journey without changing domain or API boundaries.

## Existing repository foundation
- Monorepo containing web, API, renderer, and shared packages.
- Next.js web application in `apps/web`.
- Java 21 / Spring Boot API in `apps/api`.
- TypeScript renderer skeleton.
- `packages/editor-model` defines Composition schema `1.1` with explicit `SceneObject.objectType`.
- PostgreSQL stores application metadata; MinIO is local media storage.
- GitHub Actions CI validates web, API, renderer, and E2E.

## Validated features
- Story Creation — VALIDATED.
- AI Provider Integration — VALIDATED.
- Story Outline — VALIDATED.
- Characters — VALIDATED.
- Scene Setup — VALIDATED.

## Product Experience & UI/UX Foundation branch progress
- Shared StorySprout UI primitives and design tokens are implemented.
- Dashboard, Create Story, Story Setup and Story Outline have received focused UI foundation milestones.
- Characters is now the active UI foundation milestone: reusable Project Characters are presented as a creative cast while preserving SPEC-004 behavior.
- Scene Setup, Editor and other future screens are not changed by this Characters milestone.

## Characters UI foundation
The existing Characters domain/API remains authoritative:
- Character fields remain exactly `name`, `roleDescription`, `category`, `visualDescription`, and optional `personality`.
- V1 categories remain `CHILD`, `ADULT`, `ANIMAL`, `FANTASY`, `OBJECT`, `OTHER`.
- Characters remain Project-scoped reusable records with explicit Story membership.
- Removing Story membership does not delete the reusable Project Character.
- Referenced Project Characters remain protected from deletion by the existing service/API conflict behavior.
- AI generation remains suggestion-only and does not persist Character state.
- V1 visual representation remains deterministic/placeholder-based; no image generation/upload was added.
- Characters remain outside Composition and Scene Setup semantics.

The UI now provides:
- visual character cards rather than table/admin rows;
- distinct `In this Story` and `Project Character Library` surfaces;
- creator-focused create/edit form;
- explicit shared-character editing context;
- AI Describe → Suggest → Review → Create treatment;
- loading, empty, failure, saving and AI generation/retry states;
- keyboard-visible focus and responsive layouts;
- preserved continuation to the existing Scene Setup placeholder.

## Persistence
Existing migrations V1–V5 remain unchanged. Character persistence remains defined by `V4__characters.sql`; no persistence changes are part of this UI milestone.

## Composition / Renderer boundary
Characters remain reusable Project data and do not become Composition data. Renderer behavior is unchanged.

## Validation status
- Story Creation, AI Provider Integration, Story Outline, Characters, Scene Setup and Editor remain previously validated by repository CI.
- Characters UI local execution: NOT RUN because the current execution environment cannot reach repository/build services.
- No backend/API/renderer/database validation was required or changed for this UI-only milestone.

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
Story Outline remains authoritative for narrative planning; Characters remains authoritative for reusable identity and Story membership; Scene Setup remains preparation data; Editor/Composition owns canonical editable visual state; future Timeline will own exact timing; Renderer consumes versioned Composition JSON. No bidirectional Scene Setup ↔ Composition synchronization is introduced by the Characters UI milestone.
