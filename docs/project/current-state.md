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
- Editor — VALIDATED.

## UI foundation progress
- Shared StorySprout UI primitives and design tokens are implemented.
- Dashboard, Create Story, Story Setup, Story Outline and Characters have focused UI foundation milestones on `feature/storysprout-ui-foundation`.
- Scene Setup is a completed UI foundation milestone: the existing preparation model is presented as a visual staging board.
- Editor visual/UX polish is the latest completed UI foundation milestone; Preview, Render and future Timeline functionality are not changed.

## Scene Setup UI foundation
The existing SPEC-005 domain remains authoritative:
- Outline Scene title, summary, order and planned duration remain read-only on Scene Setup.
- Backgrounds remain the six controlled presets.
- Characters remain limited to Story Characters and one instance per Character per scene.
- Props remain scene-local and duplicate instances are allowed.
- Dialogue remains ordered manual Character/Narrator text.
- Actions remain fixed intent values: IDLE, TALK, WALK, RUN, WAVE, SIT, JUMP.
- No AI, Composition, timeline, animation, audio, renderer or duration editing was added.
- Prop add/remove retains the existing immediate persistence semantics.
- Open Editor remains preparation-data navigation only.

The UI now provides:
- scene identity and progression context;
- visual background selection;
- prominent Story Character staging;
- scene-local prop ingredients;
- conversational dialogue treatment;
- lightweight action-intent controls;
- explicit save-state feedback;
- loading/error/empty/recovery states;
- keyboard/focus semantics and responsive layout.

## Editor UI foundation
- Existing Composition schema 1.1, fixed 1920 × 1080 stage semantics, object identity, transforms, visibility, deletion, layer ordering, explicit Save and optimistic conflict behavior remain unchanged.
- The Editor now presents a dark, compact creative workstation with a creator toolkit, dominant stage, deterministic Character/Prop placeholders, contextual inspector groups, stronger empty states and an intentionally non-functional Timeline boundary.
- No backend/API/database/renderer/Composition changes were made.

## Validation status
- Scene Setup backend and prior E2E validation remain valid from CI run `35101048667`; functional Editor validation remains valid from CI run `35349746183`.
- Scene Setup UI foundation local execution: NOT RUN because the current environment cannot reach repository/build services.
- The Editor visual milestone is not claimed as CI-validated until a fresh CI run reports green.

## Architecture boundaries
Characters remain reusable Project data; Scene Setup remains pre-editor preparation; Editor/Composition remains canonical editable visual state; renderer remains a separate rendering service consuming immutable Composition snapshots.

## Not implemented
- Timeline.
- Playback timing, keyframes, audio timing, lip-sync, animation generation/playback.
- Character image generation/upload and advanced asset management.
- Voice/music/media generation.
- Preview/render/FFmpeg/render jobs.
- Direct YouTube publishing.
- Authentication/authorization.
- Payments/collaboration/production deployment.

## P1 remediation milestone
- Shared `Field` now programmatically associates its visible label with the rendered control and preserves explicit control IDs where supplied.
- Story Character cards no longer expose Delete while the Character is a current Story member; Project Library deletion remains available under existing server-side reference protection.
- Successful AI Story Creation pauses at an explicit `Your Story Draft` review state before continuing to Story Outline.
- AI-generated Outline content is presented as an `AI-generated starting point` and becomes `Creator-shaped outline` after creator edits in the current session; persistence semantics are unchanged.
- Scene Setup and Editor now reinforce `Project → Story → Scene` and the reusable Project Character → Story Character relationship.
- Dashboard story cards no longer use the default gradient; Editor dark workspace surfaces use shared editor design tokens.
- No backend, API, database, Composition, renderer, Preview, Render, or Timeline work was introduced.

## P1 remediation validation
Frontend tests/build/lint were not executed in this environment; repository/build service execution remains unavailable. No CI result is claimed for this commit.
