# Feature Implementation Index

Every meaningful completed feature gets durable implementation memory at:

`docs/features/<feature>/implementation.md`

The implementation document must describe the feature end-to-end and include a **FILE-BY-FILE IMPLEMENTATION MAP**.

## Required implementation-document sections
- Feature purpose
- Specification reference
- Acceptance criteria reference
- User flow
- End-to-end architecture flow
- Frontend implementation
- Backend implementation
- Database/persistence implementation
- AI/external services
- Renderer impact
- API contracts
- Data model
- Error handling
- Security considerations
- Automated tests
- Known limitations
- Future improvements
- Related decisions
- File-by-file implementation map

## Status vocabulary
- `PLANNED` — intended but not implemented.
- `SPECIFIED` — specification and acceptance criteria exist.
- `IN_PROGRESS` — implementation underway.
- `IMPLEMENTED` — implementation exists; final validation may still be pending.
- `VALIDATED` — appropriate automated validation has passed.
- `DEPRECATED` — retained for historical reference but no longer active.

## Current feature inventory
| Feature | Status | Specification | Implementation | Tests | Major dependencies |
|---|---|---|---|---|---|
| Repository Foundation | IMPLEMENTED | Pre-SDD foundation | Foundation implementation is represented by repository history and current-state documentation | API health test + CI checks | Next.js, Spring Boot, PostgreSQL, MinIO |
| Story Creation | IMPLEMENTED | `docs/specifications/story-creation.md` | `docs/features/story-creation/implementation.md` | Frontend unit/component, backend unit/integration, API/persistence, Playwright E2E | Next.js, Spring Boot, PostgreSQL, Testcontainers, internal StoryGenerator boundary |
| AI Provider Integration | IMPLEMENTED | `docs/specifications/ai-provider-integration.md` | `docs/features/ai-provider-integration/implementation.md` | Gemini adapter, prompt, structured-output, reliability, configuration and provider-selection tests | Spring AI 2.0.1, Google GenAI/Gemini, existing StoryGenerator boundary |

AI Provider Integration implementation uses the approved initial model `gemini-2.5-flash`. Real-provider validation remains opt-in/manual and normal CI remains credential-free.
