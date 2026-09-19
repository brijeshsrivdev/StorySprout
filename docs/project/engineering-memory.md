# StorySprout Engineering Memory

## Product identity

StorySprout is an AI-assisted animation studio for building persistent children's story worlds.

The creator is the director. AI proposes and assists; the creator reviews, edits, and decides. Structured project data and Composition are the source of truth.

## Product journey

Dashboard → Create Story → Story Setup → Story Outline → Characters → Scene Setup → Editor → Preview → Render → Finished Video.

V1 targets desktop web creators and 1–5 minute children's animated episodes. The product is not a generic AI video generator.

## Core architectural principles

- Persistent Story World: reusable Project-level Characters and story-scoped cast relationships.
- Creator control: AI suggestions must not silently become canonical creator decisions.
- Composition source of truth: the persisted Composition model is the editable visual state.
- Renderer separation: the renderer consumes immutable Composition snapshots and does not read application database state.
- Progressive complexity: keep V1 editing simple; do not introduce professional animation tooling unless explicitly specified.
- Determinism where required: Preview and static V1 rendering must represent the same canonical scene interpretation.
- Backend/API/DB boundaries must not be changed for UI-only work.
- PostgreSQL stores application metadata; object storage stores media.
- Local object storage is MinIO; production storage is abstracted behind storage interfaces.

## V1 scope boundaries

Included:
- Story creation and AI-assisted story generation
- Outline
- Reusable characters
- Scene setup
- Composition-based editor
- Static Preview
- Render job lifecycle
- Static 1080p MP4 rendering

Explicitly outside current V1 scope unless separately specified:
- Timeline/keyframes/playback timing
- animation generation/playback
- facial animation/lip-sync
- audio/music/SFX timing
- AI video generation
- advanced camera controls
- YouTube publishing
- thumbnails/analytics
- collaboration/marketplace
- 4K/mobile
- production authentication/payments

## Engineering quality

Use SDD + TDD. Prefer small, feature-level vertical slices. Keep implementation documentation with the feature. Never use a missing chat history as justification to recreate functionality that already exists in the repository.
