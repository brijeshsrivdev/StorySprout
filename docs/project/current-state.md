# StorySprout Current State

**Snapshot:** Story Creation implementation is complete in the repository; final CI/E2E validation is being verified. AI Provider Integration is implemented with Spring AI 2.0.1 and Google GenAI behind the existing StoryGenerator boundary; final implementation validation is pending CI results.

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
**Status:** IMPLEMENTED — final CI validation pending

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
- Title/draft validation before converting to `StoryGenerationResult`.
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

## Persistence
- New Flyway migration: `apps/api/src/main/resources/db/migration/V2__story_creation.sql`.
- Only `projects` and `stories` are created by the existing Story Creation migration.
- Foundation `V1__foundation.sql` was not modified.
- AI Provider Integration adds no database migration.

## Validation status
- Local execution is unavailable in this environment because the repository cannot be cloned through the container's network.
- The AI implementation has been pushed through the GitHub repository path and GitHub Actions is the authoritative executable validation path.
- Do not mark AI Provider Integration `VALIDATED` until the implementation CI run is confirmed successful.
- Normal CI does not require or call the real Gemini API.
- A real Gemini API call has not been performed as part of the normal implementation validation.

## Not implemented
- Story Outline.
- Scenes, characters, assets, editor/timeline, Composition editing.
- Voice/music/media generation.
- Preview/render/FFmpeg/render jobs.
- Direct YouTube publishing.
- Authentication/authorization.
- Payments/collaboration/production deployment.
- Real Gemini API smoke test in CI.

## Architecture review
AI Provider Integration is now implemented as infrastructure behind the provider-independent StoryGenerator boundary. Spring AI and Google GenAI types remain outside StoryService and domain contracts. The approved Gemini model is configuration-driven. No Story Creation API, persistence schema, Composition schema, frontend behavior, or renderer semantics were changed. Story Outline remains explicitly out of scope.
