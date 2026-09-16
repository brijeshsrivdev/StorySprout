# StorySprout Current State

**Snapshot:** Story Creation and AI Provider Integration are implemented and validated through the merged CI path. Story Outline is now specified only; implementation has not started.

## Existing repository foundation
- Monorepo containing web, API, renderer, and shared packages.
- Next.js web application in `apps/web`.
- Java 21 / Spring Boot API in `apps/api`.
- TypeScript renderer skeleton.
- `packages/editor-model` Composition schema `1.0` remains unchanged.
- `packages/shared-types` includes Story Creation domain types.
- `packages/validation` remains the runtime validation boundary.
- Docker Compose provides local PostgreSQL and MinIO.
- GitHub Actions CI validates web, API, renderer, and Story Creation E2E.

## Story Creation
**Status:** VALIDATED

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
**Status:** VALIDATED

Specification: `docs/specifications/ai-provider-integration.md`
Implementation memory: `docs/features/ai-provider-integration/implementation.md`

Implemented scope:
- Spring AI 2.0.1 BOM and `spring-ai-starter-model-google-genai`.
- Google GenAI behind the existing `StoryGenerator` boundary.
- `ChatClient` as the adapter API; no direct `GoogleGenAiChatModel` use in StorySprout code.
- Initial model `gemini-2.5-flash`, externally configurable through `spring.ai.google.genai.chat.model` / `GEMINI_MODEL`.
- Gemini Developer API and Vertex AI configuration through Spring AI's documented Google GenAI properties.
- Fake provider remains the default and requires no credentials.
- Gemini adapter is conditional and therefore does not create a second active `StoryGenerator` in fake mode.
- Dedicated prompt builder using idea, target age, duration, visual style and language.
- Exact target-age validation remains `3_5`, `6_8`, `9_12`.
- Typed structured output using Spring AI `ChatClient.entity(...).useProviderStructuredOutput()`.
- Finite timeout with Java 21 virtual-thread execution and cancellation.
- Bounded adapter retry budget with transient/permanent failure classification.
- Spring AI retry layer configured to one attempt so adapter retries do not multiply with model-level retries.
- Safe operational logging without prompts, generated content, credentials, headers or raw provider payloads.
- No database, API, frontend, Composition, renderer, or StoryService contract changes.

### Approved model decision
The initial live Gemini model is explicitly approved as:

```text
gemini-2.5-flash
```

The retired models below are not used:

```text
gemini-2.0-flash
gemini-2.0-flash-001
```

The model remains configuration-driven so a future supported model can be selected without changing StoryGenerator or StoryService.

## Story Outline
**Status:** SPECIFIED — implementation not started

Specification: `docs/specifications/story-outline.md`

Defined scope:
- Open a valid Story into an editable Story Outline.
- Display Story title, relevant setup metadata and existing draft/idea for reference.
- Generate a structured ordered outline through a provider-independent `StoryOutlineGenerator` capability boundary.
- Persist outline scenes with title, summary and planned duration.
- Edit, add, delete and reorder outline scenes.
- Persist changes and preserve them across refresh.
- Keep AI regeneration of an existing outline out of V1 so creator edits cannot be silently overwritten.
- Keep Story Outline separate from Scene Setup, Editor/Composition and Renderer semantics.

Planned scene timing rule:
- Story duration remains 60, 180, or 300 seconds.
- Outline scene duration is an editable planned integer duration in seconds.
- A saved outline must contain at least one scene and its scene-duration total must equal the Story target duration.
- These are planning estimates, not frame-accurate Composition timing.

No Story Outline application code, database migration, Maven dependency, frontend dependency, or implementation document has been added by the specification task.

## Persistence
- Existing Flyway migration: `apps/api/src/main/resources/db/migration/V2__story_creation.sql` creates only `projects` and `stories`.
- AI Provider Integration added no database migration.
- Story Outline proposes a future `story_outline_scenes` table in a new migration; implementation has not started.
- Existing Story Creation migrations must not be rewritten.

## Validation status
- Story Creation and AI Provider Integration passed the merged GitHub Actions validation path.
- Normal CI remains credential-free and does not call the real Gemini API.
- A real Gemini API call has not been performed as part of normal CI validation.
- Story Outline has specification-level test scenarios only; no implementation validation exists yet.

## Not implemented
- Story Outline implementation.
- Scene Setup.
- Scenes as visual/Composition structures, characters, assets, editor/timeline, Composition editing.
- Voice/music/media generation.
- Preview/render/FFmpeg/render jobs.
- Direct YouTube publishing.
- Authentication/authorization.
- Payments/collaboration/production deployment.
- Real Gemini API smoke test in CI.

## Architecture review
Story Outline is specified as a planning/content layer between Story and future Scene Setup. It does not create or modify Composition JSON, TimelineClip, SceneObject, animation, camera, asset placement, renderer state, or render jobs. AI remains a suggestion mechanism behind a capability-specific provider-independent interface. Existing Spring AI/Gemini infrastructure is reused rather than duplicated.
