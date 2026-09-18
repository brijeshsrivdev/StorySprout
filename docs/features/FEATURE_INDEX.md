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

## Current feature inventory
| Feature | Status | Specification | Implementation | Tests | Major dependencies |
|---|---|---|---|---|---|
| Repository Foundation | IMPLEMENTED | Pre-SDD foundation | Foundation implementation is represented by repository history and current-state documentation | API health test + CI checks | Next.js, Spring Boot, PostgreSQL, MinIO |
| Story Creation | VALIDATED | `docs/specifications/story-creation.md` | `docs/features/story-creation/implementation.md` | Frontend unit/component, backend unit/integration, API/persistence, Playwright E2E | Next.js, Spring Boot, PostgreSQL, Testcontainers, internal StoryGenerator boundary |
| AI Provider Integration | VALIDATED | `docs/specifications/ai-provider-integration.md` | `docs/features/ai-provider-integration/implementation.md` | Gemini adapter, prompt, structured-output, reliability, configuration and provider-selection tests; CI Web/API/Renderer/E2E passed | Spring AI 2.0.1, Google GenAI/Gemini, existing StoryGenerator boundary |
| Story Outline | VALIDATED | `docs/specifications/story-outline.md` | `docs/features/story-outline/implementation.md` | Backend unit/integration, frontend duration unit, PostgreSQL/Testcontainers, Playwright E2E; full CI validation passed | Existing Story Creation, Spring AI/Gemini infrastructure, PostgreSQL, Next.js |
| Characters | VALIDATED | `docs/specifications/characters.md` | `docs/features/characters/implementation.md` | Backend unit/integration previously validated; Characters UI E2E added for the UI foundation milestone, local execution unavailable | Story Outline, existing Spring AI/Gemini infrastructure, PostgreSQL, Next.js |
| Scene Setup | IMPLEMENTED | `docs/specifications/scene-setup.md` | `docs/features/scene-setup/implementation.md` | Scene Setup UI E2E updated; local execution unavailable; prior backend/E2E validation retained | Story Outline, Characters, PostgreSQL, Next.js |
| Editor | VALIDATED | `docs/specifications/editor.md` + `docs/specifications/editor-clarifications.md` | `docs/features/editor/implementation.md` — functional Editor plus visual/UX polish | Model regression, backend service tests, Scene Setup → Editor Playwright flow; Editor UI assertions added for the visual milestone; new UI execution unavailable | Scene Setup, Characters, Story Outline, Composition schema 1.1, PostgreSQL, Next.js |
