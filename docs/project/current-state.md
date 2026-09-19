# StorySprout Current State

**Snapshot:** The repository currently contains the Story Creation, AI Provider Integration, Story Outline, Characters, Scene Setup, Editor, Preview, and Render foundations. The UI foundation branch is incrementally refining the creator journey while preserving domain and API boundaries.

## Repository foundation

- Monorepo containing web, API, renderer, and shared packages.
- Next.js web application in `apps/web`.
- Java 21 / Spring Boot API in `apps/api`.
- TypeScript renderer in `services/renderer`.
- `packages/editor-model` defines Composition schema `1.1` with explicit `SceneObject.objectType`.
- PostgreSQL stores application metadata; MinIO is local media storage.
- GitHub Actions CI exists for web, API, renderer, and E2E.

## Validated/product foundations

- Story Creation — implemented.
- AI Provider Integration — implemented.
- Story Outline — implemented.
- Characters — implemented.
- Scene Setup — implemented.
- Editor — implemented.
- Preview — implemented on the current branch.
- Render foundation — implemented on the current branch.

## UI foundation

The branch `feature/storysprout-ui-foundation` contains:

- shared StorySprout UI primitives and design tokens;
- Dashboard;
- Create Story;
- Story Setup;
- Story Outline;
- Characters;
- Scene Setup;
- Editor visual/UX polish;
- P1 creator-experience remediation;
- Preview;
- Render status/retry integration.

## P1 remediation

- Story Creation now pauses at an explicit review state for AI-generated drafts.
- Outline distinguishes an AI-generated starting point from creator-shaped content.
- Scene Setup and Editor reinforce Project → Story → Scene continuity.
- Story Character cards avoid destructive Delete for current story membership.
- Shared Field labels are programmatically associated with controls.
- Dashboard default gradient treatment was removed.
- Editor dark surfaces use shared design tokens.

## Preview

Preview is a static V1 representation of the persisted Composition:

- loads the existing Editor context;
- reads `composition.compositionJson`;
- uses the shared `@storysprout/editor-model` scene interpretation;
- preserves object order, visibility, position, scale, and semantic object identity;
- provides loading, empty, ready, error, and return-to-Editor states;
- does not introduce a second scene model or timeline.

## Render

Render foundation provides:

- RenderJob lifecycle: REQUESTED → QUEUED → RENDERING → COMPLETED, with FAILED/retry;
- exact Composition id/version capture;
- immutable JSON snapshot per render job;
- renderer isolation from PostgreSQL;
- deterministic static 1920×1080, 30fps, 16:9 MP4 output;
- MinIO/S3-compatible object storage abstraction;
- artifact retrieval;
- UI progress/status/retry behavior.

Current renderer intentionally rejects non-empty Timeline content because V1 animation timing is not implemented.

## Architecture boundaries

Characters remain reusable Project data; Scene Setup remains preparation; Editor/Composition remains canonical editable visual state; Preview reads that canonical state; Render consumes an immutable Composition snapshot; renderer remains a separate service.

## Validation status

The UI Foundation + Preview/Render validation milestone is green on the exact current branch HEAD.

GitHub Actions CI run #159 (35437795279) validated exact HEAD c67b69a0e77ad282240ce4255d6782d800d2b7aa:

- Web lint, typecheck, unit tests and production build — passed.
- API compile and tests — passed.
- Renderer typecheck, tests and build — passed.
- Browser E2E with PostgreSQL, MinIO, renderer, API and web — passed.

The UI-specific remediation restored Tailwind v4 PostCSS processing through apps/web/postcss.config.mjs. The repository already contained the intended StorySprout tokens and utility-based UI; the primary failure was the missing styling build integration.

## Not implemented

- Timeline playback/keyframes/animation generation
- audio/music/SFX timing
- lip-sync/facial animation
- AI video generation
- advanced camera controls
- direct YouTube publishing
- thumbnails/analytics
- collaboration/marketplace
- production authentication/authorization
- payments/production deployment

## Current next step

Perform a fresh UI Foundation + Preview/Render visual and functional QA audit, inspect CI for the current HEAD, reconcile findings, then update this document and `session-handoff.md` before beginning the next major feature.
