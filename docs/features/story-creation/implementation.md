# Story Creation — Implementation

**Status:** IMPLEMENTED (final validation pending)

## 1. Feature purpose

Story Creation is the first complete StorySprout vertical slice. It lets a creator create a draft project, choose AI or Blank mode, capture story setup, persist it, and continue with a saved story. AI generation is provider-independent and only produces story title/draft content; it does not create editor or Composition state.

## 2. Specification reference

- `docs/specifications/story-creation.md` — SPEC-001
- Approved V1 target-age values: `3_5` (Ages 3–5), `6_8` (Ages 6–8), `9_12` (Ages 9–12).

## 3. Acceptance criteria status

| AC | Status |
|---|---|
| AC-01 Empty Dashboard | Implemented |
| AC-02 Project List | Implemented |
| AC-03 Create Project | Implemented |
| AC-04 Creation Mode | Implemented |
| AC-05 Story Setup | Implemented |
| AC-06 Validation | Implemented |
| AC-07 Blank Story | Implemented |
| AC-08 AI Story | Implemented with deterministic fake generator |
| AC-09 AI Failure | Implemented; setup remains persisted and API returns 422 |
| AC-10 Persistence Across Refresh | Implemented |
| AC-11 Invalid Project | Implemented |

## 4. User flow

`Dashboard → Create Story → AI/Blank → Story Setup → Continue/Generate Story → persisted Story → Dashboard`

The Dashboard loads projects from the API. Create Story first creates a DRAFT project shell. Story Setup then submits the selected mode and setup. Blank mode saves immediately. AI mode persists an initial `GENERATING` story, invokes `StoryGenerator`, and updates it to `COMPLETED` or `FAILED`.

## 5. End-to-end architecture/data flow

`Next.js UI → /api/v1 rewrite → Spring controller → StoryService → PostgreSQL`

For AI:

`StoryService → StoryGenerator → generated title/draft → StoryRepository → PostgreSQL`

The frontend never owns canonical story persistence. The Composition Model is untouched. No scene, asset, timeline, render, or provider SDK participates in this slice.

## 6. Frontend implementation

- Dashboard is a client component that loads project metadata and renders loading, empty, ready, and error states.
- Create Story provides mutually exclusive AI/Blank choices and prevents duplicate project creation while saving.
- Story Setup captures idea, target age, duration, visual style, and language.
- Target age is restricted to exactly `3_5`, `6_8`, and `9_12`.
- Submit state disables the action and shows `Saving…` or `Generating…`.
- AI failure is recoverable without clearing setup.
- Next.js rewrite keeps browser API calls same-origin while forwarding `/api/v1/*` to the Spring API.
- Minimal Vitest + Testing Library setup covers component behavior.
- Playwright E2E tests cover the primary blank, AI, refresh, and failure flows.

## 7. Backend implementation

The feature uses a small feature-local package under `com.storysprout.api.story`.

- Records model Project and Story state.
- JDBC repositories persist only projects and stories.
- `StoryService` owns project creation, validation, blank/AI branching, generation status transitions, and project touch/update ordering.
- `StoryController` exposes the three specified endpoints.
- Bean validation handles request shape; service validation enforces exact business values.
- Errors use the shared API error envelope.
- No authentication or unrelated CRUD was introduced.

## 8. Database implementation

Migration `V2__story_creation.sql` creates only:
- `projects`
- `stories`

It adds the required foreign key, domain-value CHECK constraints, nonblank checks, and indexes for project ordering and project-to-story lookup. The foundation `V1__foundation.sql` remains unchanged.

AI failures are persisted because the generator exception is handled inside the transaction after the initial story row has been created; the story is then updated to `FAILED` and the controller returns HTTP 422.

## 9. AI implementation

`StoryGenerator` is the application-level contract. Its request contains only story setup values and its result contains generated title/draft text.

`DeterministicStoryGenerator` is intentionally provider-free and deterministic. It exists so the V1 slice is executable without credentials or an external AI provider. Production provider integration remains a future feature and must sit behind the same interface.

## 10. API contracts

- `GET /api/v1/projects` — returns `{ "data": Project[] }`, ordered by `updatedAt DESC`.
- `POST /api/v1/projects` — accepts `{ "name": "Untitled Story" }` and returns a DRAFT project.
- `POST /api/v1/projects/{projectId}/stories` — accepts creation mode plus story setup and returns the persisted Story on success.
- Controlled AI failure returns HTTP 422 with the standard error envelope and story identifier/status fields.
- Validation returns 400; unknown project returns 404; unexpected errors return 500.

## 11. Validation/error handling

UI validation blocks missing idea/target age before network submission. Backend validation is authoritative and rejects unsupported target ages, durations, styles, languages, and modes. Database CHECK constraints provide a final persistence guard.

The API error envelope is documented in `docs/api/api-conventions.md` and contains `code`, `message`, and optional field details. Internal stack traces and credentials are not returned.

## 12. Testing strategy and actual tests

### Frontend
`apps/web/tests/story-creation.test.tsx` covers empty dashboard, project cards, mode exclusivity, approved target ages, validation, blank persistence call, AI success, and AI failure recovery.

### Backend unit
`StoryServiceTest` covers blank/no-generator, exact AI request, successful generation, AI failure persistence, and missing project.

### Backend integration/persistence/API
`StoryControllerIntegrationTest` uses Testcontainers PostgreSQL and Flyway to exercise empty list, project creation/order, blank story, AI story, AI failure/422, validation/not-found, constraints, and FK deletion protection.

### E2E
`apps/web/tests/e2e/story-creation.spec.ts` covers blank persistence + refresh, AI generation + dashboard persistence, and recoverable AI failure at the UI boundary.

Final pass/fail results are recorded in current-state after validation.

## 13. Known limitations

- AI generation is deterministic and not a real AI provider.
- Authentication/authorization is intentionally absent.
- Story Outline, scenes, characters, assets, editor/timeline, Composition editing, preview, rendering, and publishing are not part of this slice.
- No idempotency key is implemented because the specification explicitly leaves backend idempotency optional and frontend duplicate-submit prevention is present.
- E2E requires a running PostgreSQL-backed API and web server; CI provisions those services.

## 14. Future improvements

- Replace the deterministic generator with a provider adapter without changing `StoryService` or API contracts.
- Continue from the persisted Story into the Story Outline slice.
- Add richer project/story navigation once later workflow slices exist.

## 15. Relevant architectural decisions

- ADR-001 — Composition Model is the source of truth.
- ADR-002 — Composition schema is versioned.
- ADR-003 — AI proposes suggestions/assets, not canonical project state.
- ADR-004 — AI providers stay behind abstractions.
- ADR-007 — Renderer remains separate from API.
- ADR-009 — V1 is web desktop first.
- ADR-011 — Avoid premature infrastructure.
- ADR-012 — SDD/TDD and durable feature implementation memory.

No new architecture ADR was required.

## FILE-BY-FILE IMPLEMENTATION MAP

| File | Layer | Responsibility | Important interactions |
|---|---|---|---|
| `apps/web/app/page.tsx` | Frontend | Dashboard and project list | Calls `listProjects`; links to Create Story |
| `apps/web/app/create/page.tsx` | Frontend | AI/Blank mode selection and project-shell creation | Calls `createProject`; routes to setup |
| `apps/web/app/create/setup/page.tsx` | Frontend | Story setup form, validation, submission and generation states | Calls `createStory`; reads project/mode from URL |
| `apps/web/lib/api.ts` | Frontend | Typed API client and error normalization | Used by all Story Creation pages |
| `apps/web/next.config.ts` | Frontend infrastructure | Same-origin rewrite for API calls | Forwards browser `/api/v1/*` requests to Spring API |
| `apps/web/package.json` | Frontend tooling | Adds minimal test/E2E scripts and dependencies | Used by CI and local feature tests |
| `apps/web/vitest.config.ts` | Frontend testing | Vitest/jsdom configuration | Loads React test environment |
| `apps/web/vitest.setup.ts` | Frontend testing | Testing Library DOM matchers | Loaded by Vitest |
| `apps/web/tests/story-creation.test.tsx` | Frontend tests | Component behavior proof for Story Creation | Mocks API boundary and Next navigation |
| `apps/web/playwright.config.ts` | E2E tooling | Playwright test configuration | Points tests at running web app |
| `apps/web/tests/e2e/story-creation.spec.ts` | E2E tests | Full creator-flow coverage | Exercises real browser flow and API; one failure test stubs controlled 422 |
| `apps/api/pom.xml` | Backend build | Adds validation and Testcontainers test dependencies | Enables request validation and PostgreSQL integration tests |
| `apps/api/src/main/java/com/storysprout/api/story/ProjectStatus.java` | Backend domain | Project lifecycle enum | Used by Project and persistence |
| `apps/api/src/main/java/com/storysprout/api/story/StoryCreationMode.java` | Backend domain | AI/Blank enum | Controls service branching |
| `apps/api/src/main/java/com/storysprout/api/story/StoryGenerationStatus.java` | Backend domain | Generation lifecycle enum | Persisted and returned by API |
| `apps/api/src/main/java/com/storysprout/api/story/Project.java` | Backend domain | Project record | Repository/service/controller |
| `apps/api/src/main/java/com/storysprout/api/story/Story.java` | Backend domain | Story record | Repository/service/controller |
| `apps/api/src/main/java/com/storysprout/api/story/ProjectCreateRequest.java` | API DTO | Project creation input validation | Consumed by StoryController |
| `apps/api/src/main/java/com/storysprout/api/story/StoryCreateRequest.java` | API DTO | Story setup input validation | Consumed by StoryController/StoryService |
| `apps/api/src/main/java/com/storysprout/api/story/ProjectResponse.java` | API DTO | Project response projection | Wraps persisted Project for API |
| `apps/api/src/main/java/com/storysprout/api/story/StoryResponse.java` | API DTO | Story response projection | Includes generation status and continuation path |
| `apps/api/src/main/java/com/storysprout/api/story/ApiResponse.java` | API contract | Success envelope | Used by StoryController |
| `apps/api/src/main/java/com/storysprout/api/story/ApiErrorResponse.java` | API contract | Error envelope | Used by exception handling and controlled failures |
| `apps/api/src/main/java/com/storysprout/api/story/ApiFieldError.java` | API contract | Field-level error detail | Nested in error envelope |
| `apps/api/src/main/java/com/storysprout/api/story/ApiExceptionHandler.java` | Backend error handling | Maps validation/not-found/unexpected failures | Provides consistent 400/404/500 responses |
| `apps/api/src/main/java/com/storysprout/api/story/ProjectRepository.java` | Persistence | Project insert/list/find/touch operations | Uses JdbcTemplate and `projects` table |
| `apps/api/src/main/java/com/storysprout/api/story/StoryRepository.java` | Persistence | Story insert/generation update/find operations | Uses JdbcTemplate and `stories` table |
| `apps/api/src/main/java/com/storysprout/api/story/StoryService.java` | Application | End-to-end creation orchestration | Coordinates repositories and StoryGenerator |
| `apps/api/src/main/java/com/storysprout/api/story/ProjectNotFoundException.java` | Backend error | Missing project domain error | Mapped to 404 |
| `apps/api/src/main/java/com/storysprout/api/story/StoryValidationException.java` | Backend error | Business validation error | Mapped to 400 |
| `apps/api/src/main/java/com/storysprout/api/story/StoryGenerationException.java` | AI boundary error | Controlled generator failure type | Caught by StoryService |
| `apps/api/src/main/java/com/storysprout/api/story/StoryGenerationRequest.java` | AI contract | Provider-neutral generator input | Created by StoryService |
| `apps/api/src/main/java/com/storysprout/api/story/StoryGenerationResult.java` | AI contract | Provider-neutral generator output | Persisted by StoryService |
| `apps/api/src/main/java/com/storysprout/api/story/StoryGenerator.java` | AI boundary | Provider-independent generation interface | Injected into StoryService |
| `apps/api/src/main/java/com/storysprout/api/story/DeterministicStoryGenerator.java` | AI implementation | Credential-free deterministic V1 generator | Implements StoryGenerator |
| `apps/api/src/main/java/com/storysprout/api/story/StoryCreationResult.java` | Application result | Carries persisted Story plus controlled generation failure | Consumed by controller |
| `apps/api/src/main/java/com/storysprout/api/story/StoryController.java` | API | Implements the three specified endpoints | Delegates to StoryService |
| `apps/api/src/main/resources/db/migration/V2__story_creation.sql` | Database | Creates projects/stories and constraints/indexes | Applied by Flyway before repository use |
| `apps/api/src/test/java/com/storysprout/api/story/StoryServiceTest.java` | Backend unit tests | Service behavior proof | Mocks repositories and StoryGenerator |
| `apps/api/src/test/java/com/storysprout/api/story/StoryControllerIntegrationTest.java` | Integration/API tests | HTTP + Flyway + real PostgreSQL proof | Uses Testcontainers and mocked StoryGenerator |
| `packages/shared-types/src/index.ts` | Shared contract | Adds Story Creation domain enums/types and richer Project metadata | Shared across future web/API consumers |
| `.github/workflows/ci.yml` | CI | Runs frontend tests, backend compile/tests, renderer build, and E2E environment | Validates Story Creation on pushes/PRs |
| `docs/api/api-conventions.md` | API documentation | Defines reusable success/error envelope | Governs Story Creation responses |
| `docs/features/FEATURE_INDEX.md` | Feature memory | Tracks Story Creation implementation status | Links specification/implementation |
| `docs/project/current-state.md` | Project memory | Records implementation and validation state | Keeps future agents aligned |
