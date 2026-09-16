# StorySprout Current State

**Snapshot:** Story Creation, AI Provider Integration, Story Outline, and Characters are implemented. Characters is the current feature on `feature/characters`; final CI validation is pending.

## Existing repository foundation
- Monorepo containing web, API, renderer, and shared packages.
- Next.js web application in `apps/web`.
- Java 21 / Spring Boot API in `apps/api`.
- TypeScript renderer skeleton.
- `packages/editor-model` Composition schema `1.0` remains unchanged.
- `packages/shared-types` includes Story Creation domain types.
- `packages/validation` remains the runtime validation boundary.
- Docker Compose provides local PostgreSQL and MinIO.
- GitHub Actions CI validates web, API, renderer, and E2E.

## Story Creation
**Status:** VALIDATED
Specification: `docs/specifications/story-creation.md`
Implementation memory: `docs/features/story-creation/implementation.md`

Implemented scope includes Dashboard, AI/Blank Story Setup, exact approved target ages, Project/Story persistence, deterministic AI fake, validation, recoverable generation failures, and frontend/backend/API/persistence/E2E tests.

## AI Provider Integration
**Status:** VALIDATED
Specification: `docs/specifications/ai-provider-integration.md`
Implementation memory: `docs/features/ai-provider-integration/implementation.md`

Spring AI 2.0.1 Google GenAI integration uses the provider-independent AI boundaries, existing ChatClient pattern, configurable `gemini-2.5-flash`, external credentials, finite timeout and bounded retry. Normal CI remains credential-free.

## Story Outline
**Status:** VALIDATED
Specification: `docs/specifications/story-outline.md`
Implementation memory: `docs/features/story-outline/implementation.md`

Story Outline is a planning layer with ordered narrative scenes, CRUD, reorder, positive integer planning durations, Target/Planned/Variance display, deterministic fake generation and Gemini adapter. Exact timing remains future Editor/Timeline/Composition. Scene Setup remains a placeholder.

## Characters
**Status:** IMPLEMENTED — final validation pending
Specification: `docs/specifications/characters.md`
Implementation memory: `docs/features/characters/implementation.md`

Implemented scope:
- Story Outline → Characters navigation.
- Project-scoped reusable Character records.
- Story–Character membership with duplicate prevention and cross-project validation.
- Character create/edit/delete.
- Removing Story membership preserves the reusable Project Character.
- Referenced Project Characters cannot be deleted; API returns controlled conflict.
- V1 fields: name, roleDescription, category, visualDescription, optional personality.
- Categories: CHILD, ADULT, ANIMAL, FANTASY, OBJECT, OTHER.
- Deterministic placeholder/avatar only; no image-generation or upload pipeline.
- Provider-independent `CharacterGenerator` boundary.
- Deterministic fake generator for credential-free CI.
- Gemini Character adapter reuses existing Spring AI ChatClient, GeminiAiCallExecutor, GeminiAiProperties and configurable `gemini-2.5-flash`.
- AI suggestions populate editable UI fields and are not persisted until explicit Create.
- Characters → existing Scene Setup placeholder continuation.
- No Composition, Timeline, animation, dialogue, audio, renderer or asset state is created.

## Persistence
- Existing Flyway migrations V1–V3 remain unchanged.
- `V4__characters.sql` creates `characters` and `story_characters`.
- Character records are owned by Projects; membership is many-to-many between Stories and Project Characters.
- Character deletion is blocked while memberships exist.
- Membership removal never deletes the Character.

## Validation status
- Story Creation: PASS in merged CI.
- AI Provider Integration: PASS in merged CI.
- Story Outline: PASS in merged CI.
- Characters: validation pending on feature branch.
- Real Gemini API smoke test: NOT RUN in normal CI by design.

## Not implemented
- Scene Setup implementation.
- Character image generation/upload and advanced asset management.
- Scenes as visual/Composition structures, editor/timeline, Composition editing.
- Voice/music/media generation.
- Preview/render/FFmpeg/render jobs.
- Direct YouTube publishing.
- Authentication/authorization.
- Payments/collaboration/production deployment.

## Architecture review
Characters are a planning/reusable-identity layer between Story Outline and future Scene Setup. Project Characters are canonical creator-owned data and Story membership identifies which reusable characters a Story can use. No Character data enters `packages/editor-model` or Composition in SPEC-004. AI remains a suggestion mechanism behind a capability-specific provider-independent interface. Existing Spring AI/Gemini infrastructure is reused rather than duplicated.
