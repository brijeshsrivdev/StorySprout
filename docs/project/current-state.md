# StorySprout Current State

**Snapshot:** Story Creation implementation is complete in the repository; final CI/E2E validation is being verified. AI Provider Integration is specified only; no real provider integration has been implemented.

## Existing repository foundation
- Monorepo containing web, API, renderer, and shared packages.
- Next.js web application in `apps/web`.
- Java 21 / Spring Boot API in `apps/api`.
- TypeScript renderer skeleton.
- `packages/editor-model` Composition schema `1.0` remains unchanged.
- `packages/shared-types` now includes Story Creation domain types.
- `packages/validation` remains the runtime validation boundary.
- Docker Compose provides local PostgreSQL and MinIO.
- GitHub Actions CI validates web, API, renderer, and Story Creation E2E.

## Story Creation
**Status:** IMPLEMENTED (pending final validation result)

Specification: `docs/specifications/story-creation.md`
Implementation memory: `docs/features/story-creation/implementation.md`

Implemented scope:
- Dashboard project list, empty state, loading/error states and Create Story.
- AI/Blank mode selection.
- Story Setup with idea, target age, duration, visual style and language.
- Approved target-age values are exactly `3_5` (Ages 3–5), `6_8` (Ages 6–8), and `9_12` (Ages 9–12).
- Project and Story persistence through the specified APIs.
- Validation and reusable API error envelope.
- Blank flow without AI invocation.
- Provider-independent StoryGenerator with deterministic credential-free implementation.
- AI generation status transitions and recoverable failure persistence.
- Frontend unit/component tests, backend service tests, PostgreSQL/Testcontainers API tests, and Playwright E2E coverage.

## AI Provider Integration
**Status:** SPECIFIED — implementation not started

Specification: `docs/specifications/ai-provider-integration.md`

The specification establishes:
- Spring AI 2.0.1 as the proposed AI integration framework for the Spring Boot 4.1.1 API.
- Google GenAI/Gemini behind the existing `StoryGenerator` boundary.
- `ChatClient` as the normal adapter-level Spring AI API; direct `GoogleGenAiChatModel` use requires explicit justification.
- Externalized provider/model configuration and runtime-only credentials.
- Deterministic fake-generator selection for normal tests and local runs.
- Bounded timeout/retry behavior, controlled provider failures and structured-output validation.
- No Spring AI or Google types in StoryService/domain contracts.
- No database or Composition changes.

### Current compatibility blocker
The requested `gemini-2.0-flash` model is not a viable live implementation target as of September 16, 2026 because Google's current deprecation documentation lists `gemini-2.0-flash` and `gemini-2.0-flash-001` with a June 1, 2026 shutdown date. A currently supported Gemini model must be explicitly selected before implementation. The specification does not silently substitute a model.

## Persistence
- New Flyway migration: `apps/api/src/main/resources/db/migration/V2__story_creation.sql`.
- Only `projects` and `stories` are created.
- Foundation `V1__foundation.sql` was not modified.
- AI Provider Integration requires no database migration.

## Validation status
- Local execution is unavailable in this environment because the repository cannot be cloned through the container's network.
- GitHub Actions is the authoritative executable validation path and has been triggered for the implementation commit.
- Do not mark Story Creation `VALIDATED` until the implementation CI/E2E run is confirmed successful.
- AI Provider Integration has not entered implementation or validation.

## Not implemented
- Real AI provider integration.
- Spring AI dependencies.
- GeminiStoryGenerator.
- Authentication/authorization.
- Story Outline.
- Scenes, characters, assets, editor/timeline, Composition editing.
- Voice/music/media generation.
- Preview/render/FFmpeg/render jobs.
- Direct YouTube publishing.
- Payments/collaboration/production deployment.

## Architecture review
AI Provider Integration adds an explicit specification and records the intended infrastructure boundary, while preserving the existing provider-independent AI architecture. No application source code, database migration, Composition schema, or Story Creation behavior was changed by this specification task.
