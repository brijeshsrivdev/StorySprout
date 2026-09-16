# Feature Specification — Story Creation

**Specification ID:** SPEC-001  
**Status:** SPECIFIED  
**Implementation:** Not started

## Problem

StorySprout has a runnable repository foundation, but a creator cannot yet create a project or story. The first product vertical slice must connect the dashboard, story setup, persistence, validation, and a provider-independent AI story-generation boundary without entering later V1 features.

## Goal

Enable a desktop-web creator to create a StorySprout project and Story either from AI-assisted generation or from a blank story, persist the setup, and continue to the next story workflow.

## User Story

As a StorySprout creator, I want to start a story with AI or from a blank story, provide basic story setup, and save it so that I can continue building the story later.

## User Flow

### Dashboard
1. Open Dashboard.
2. Load projects from the API.
3. Show an empty state with `Create Story` when there are no projects.
4. Otherwise show project cards with name/title, status, and last-updated metadata.
5. Select `Create Story`.
6. Create a draft Project shell and enter Create Story.

### Create Story
1. Choose `Start with AI` or `Blank Story`.
2. Continue to Story Setup.

### Story Setup
1. Enter story idea.
2. Select target age.
3. Select duration: `1`, `3`, or `5` minutes.
4. Select visual style: `2D`, `3D`, or `Hybrid`.
5. Select language: `English` or `Hindi`.
6. AI mode uses `Generate Story`; Blank mode uses `Continue`.
7. Validate the setup.
8. Persist the Story setup.
9. In AI mode, invoke the internal `StoryGenerator` and persist its generated title/draft.
10. Return the created project/story identifiers and continuation information.

### Alternate paths
- Abandoned draft projects remain drafts and may appear on Dashboard.
- Blank mode never invokes AI.
- AI failure preserves the submitted Story setup and exposes a recoverable failure.
- Invalid input is not persisted.
- Unknown project IDs return not-found.

## Functional Requirements

- **FR-01:** Dashboard lists persisted projects newest-updated-first.
- **FR-02:** Empty Dashboard provides `Create Story`.
- **FR-03:** Project cards show name/title, status, and updated metadata.
- **FR-04:** `Create Story` creates a `DRAFT` Project shell.
- **FR-05:** Creation mode is exactly `AI` or `BLANK`.
- **FR-06:** Story Setup captures idea, target age, duration, visual style, and language.
- **FR-07:** Duration is restricted to `1`, `3`, `5`; visual style to `2D`, `3D`, `HYBRID`; language to `ENGLISH`, `HINDI`.
- **FR-08:** Blank Story persists setup without invoking `StoryGenerator`.
- **FR-09:** AI Story persists setup and invokes `StoryGenerator` through the internal AI boundary.
- **FR-10:** No Story Creation domain/application code may depend directly on a provider SDK.
- **FR-11:** A successfully created Story is discoverable through Dashboard after refresh.

## UI Requirements

### Dashboard
- Desktop-web-first.
- Loading, ready, empty, and error states.
- Primary `Create Story` action.
- Basic project cards only; no editor controls.

### Create Story
- Mutually exclusive AI/Blank choices.
- Selected state.
- Navigation/loading/error states.

### Story Setup
- Story idea input.
- Target-age selector.
- Duration selector: 1/3/5 minutes.
- Visual-style selector: 2D/3D/Hybrid.
- Language selector: English/Hindi.
- AI action: `Generate Story`.
- Blank action: `Continue`.
- Inline validation.
- Submitting state prevents duplicate clicks.
- AI generating and recoverable generation-failed states.

The exact finite target-age option labels/range are intentionally not invented here. Before implementation, the target-age options must be agreed and represented by stable domain/API values rather than free-form text.

## Domain Requirements

### Project

Minimum persisted fields:
- `id`
- `name`
- `status`
- `createdAt`
- `updatedAt`

New projects are `DRAFT`.

### Story

Minimum persisted fields:
- `id`
- `projectId`
- `title`
- `idea`
- `targetAge`
- `durationMinutes`
- `visualStyle`
- `language`
- `creationMode`
- `generationStatus`
- `draftContent` nullable
- `createdAt`
- `updatedAt`

Values:
- `creationMode`: `AI`, `BLANK`
- `generationStatus`: `NOT_REQUESTED`, `GENERATING`, `COMPLETED`, `FAILED`
- `durationMinutes`: `1`, `3`, `5`
- `visualStyle`: `2D`, `3D`, `HYBRID`
- `language`: `ENGLISH`, `HINDI`

A Story belongs to an existing Project.

Story Creation does not create Scenes, TimelineClips, assets, or a populated Composition. Composition schema `1.0` remains unchanged.

## API Requirements

Follow the existing `/api/v1` base path and opaque identifier conventions documented in `docs/api/api-conventions.md`.

### List projects
`GET /api/v1/projects`

Returns projects ordered by `updatedAt DESC`.

### Create project shell
`POST /api/v1/projects`

Request:
```json
{"name":"Untitled Story"}
```

Creates only a DRAFT Project. It does not create a Story or invoke AI.

### Create Story
`POST /api/v1/projects/{projectId}/stories`

Request shape:
```json
{
  "creationMode": "AI",
  "idea": "A little rabbit learns to share.",
  "targetAge": "<approved-age-value>",
  "durationMinutes": 3,
  "visualStyle": "2D",
  "language": "ENGLISH"
}
```

The response contains the Story ID, Project ID, persisted setup, title, generation status, generated draft when available, and continuation information.

### API errors
At minimum distinguish:
- `400` validation/invalid request
- `404` project not found
- `409` conflicting/duplicate state where applicable
- `422` controlled AI generation failure
- `500` unexpected server failure

Use the repository's existing API error conventions when the implementation establishes the error envelope; do not invent a competing global format.

Frontend prevents duplicate submission. Backend idempotency is not required unless implementation exposes a concrete need.

## Persistence Requirements

Only two domain tables are required for this slice.

### `projects`

- `id UUID PRIMARY KEY`
- `name VARCHAR(...) NOT NULL`
- `status VARCHAR(...) NOT NULL`
- `created_at TIMESTAMPTZ NOT NULL`
- `updated_at TIMESTAMPTZ NOT NULL`

Constraints:
- name cannot be blank after trimming
- status must be supported

Index:
- `updated_at DESC` for Dashboard ordering

### `stories`

- `id UUID PRIMARY KEY`
- `project_id UUID NOT NULL REFERENCES projects(id)`
- `title VARCHAR(...) NOT NULL`
- `idea TEXT NOT NULL`
- `target_age VARCHAR(...) NOT NULL`
- `duration_minutes SMALLINT NOT NULL`
- `visual_style VARCHAR(...) NOT NULL`
- `language VARCHAR(...) NOT NULL`
- `creation_mode VARCHAR(...) NOT NULL`
- `generation_status VARCHAR(...) NOT NULL`
- `draft_content TEXT NULL`
- `created_at TIMESTAMPTZ NOT NULL`
- `updated_at TIMESTAMPTZ NOT NULL`

Constraints:
- Project foreign key
- duration limited to 1/3/5
- visual style limited to 2D/3D/HYBRID
- language limited to ENGLISH/HINDI
- creation mode limited to AI/BLANK
- generation status limited to defined states
- idea cannot be blank

Indexes:
- `project_id`
- `updated_at DESC` only if justified by actual query use; avoid speculative indexes

A new Flyway migration is required during implementation. The existing foundation migration must not be rewritten. No migration is created during this specification stage.

## AI Requirements

### Internal contract

Application code depends on an internal contract equivalent to:

```text
StoryGenerator.generate(StoryGenerationRequest) -> StoryGenerationResult
```

Request:
- idea
- target age
- duration
- visual style
- language

Result:
- generated title
- generated draft story text

Provider-specific request/response types must not cross this boundary.

### Behavior
- Blank mode never calls `StoryGenerator`.
- AI mode calls it once for the creation request.
- Success stores the generated title/draft and sets `generationStatus=COMPLETED`.
- Failure preserves setup and sets `generationStatus=FAILED`.
- Generated content is Story content, not Composition state.
- AI does not create scenes, characters, assets, animation, or timeline data in this slice.

### Test provider
Use an in-memory fake/mock `StoryGenerator` that:
- returns deterministic output for success tests,
- can be configured to fail,
- records invocation count and request values.

### Real provider
No real AI provider, SDK, credentials, production prompts, provider-specific retries, or production moderation integration is included.

## Validation / Error Requirements

Validate at both UI and API boundaries:
- idea present and non-blank
- target age selected
- duration valid
- visual style valid
- language valid
- creation mode valid
- project exists

Invalid requests must not create invalid Stories. AI failures preserve setup and are retryable. Unexpected errors must not expose credentials or internal stack traces.

## Security / Content Safety

Apply the baseline in `docs/security/content-safety.md`:
- treat creator ideas and generated drafts as untrusted content
- enforce reasonable request-size limits
- do not send unnecessary personal data to providers
- never store provider credentials in project/story records
- do not publish/distribute content from this slice
- expose safety/generation failures instead of silently succeeding

Authentication/authorization is outside this slice because it is not part of the current foundation.

## Renderer Requirements

None beyond compatibility. Story Creation must not modify the Composition Model or renderer semantics. No renderer code, FFmpeg, render jobs, scene composition, audio composition, or media generation is included.

## Acceptance Criteria

### AC-01 — Empty Dashboard
Given no projects exist, when Dashboard loads, then an empty state and `Create Story` action are displayed.

### AC-02 — Project List
Given projects exist, when Dashboard loads, then projects appear newest-updated-first with name/title, status, and updated metadata.

### AC-03 — Create Project
Given the creator selects `Create Story`, when project creation succeeds, then a DRAFT Project exists and the creator enters the Story Creation flow.

### AC-04 — Creation Mode
Given the creator is on Create Story, when AI or Blank is selected, then exactly one mode is active and its appropriate action is available.

### AC-05 — Story Setup
Given a creation mode is selected, when Story Setup loads, then idea, target age, duration, visual style, and language are available with the specified allowed values.

### AC-06 — Validation
Given invalid or incomplete setup, when submitted, then the UI shows validation errors and the API does not persist an invalid Story.

### AC-07 — Blank Story
Given valid setup with BLANK mode, when `Continue` is selected, then the Story is persisted and `StoryGenerator` is not invoked.

### AC-08 — AI Story
Given valid setup with AI mode, when `Generate Story` is selected, then the internal StoryGenerator is invoked, generated title/draft are persisted, and a successful continuation response is returned.

### AC-09 — AI Failure
Given valid AI setup and a StoryGenerator failure, when generation fails, then setup remains persisted, status is FAILED, and the UI shows a recoverable failure state without publishing or deleting the Story.

### AC-10 — Persistence Across Refresh
Given a Story has been created, when Dashboard is refreshed, then the associated project is loaded from persisted API data.

### AC-11 — Invalid Project
Given a nonexistent project ID, when Story creation is requested, then the API returns 404 and does not create a Story.

## Test Scenarios

### Frontend
- **T-UI-01:** Empty Dashboard renders empty state. → AC-01
- **T-UI-02:** Project cards render required metadata. → AC-02
- **T-UI-03:** Create Story starts creation flow. → AC-03
- **T-UI-04:** AI/Blank choices are mutually exclusive. → AC-04
- **T-UI-05:** Story Setup renders all required fields/options. → AC-05
- **T-UI-06:** Invalid setup blocks submission and shows errors. → AC-06
- **T-UI-07:** AI submit shows generating and success states. → AC-08
- **T-UI-08:** AI failure shows recoverable error. → AC-09
- **T-UI-09:** Blank submit continues without AI. → AC-07
- **T-UI-10:** Active submission prevents duplicate clicks. → AC-08/AC-07

### Backend unit/service
- **T-BE-01:** Project creation creates DRAFT project. → AC-03
- **T-BE-02:** Invalid Story setup is rejected. → AC-06
- **T-BE-03:** Blank Story persists and does not call generator. → AC-07
- **T-BE-04:** AI Story calls generator with exact setup. → AC-08
- **T-BE-05:** Successful generation persists title/draft and COMPLETED status. → AC-08
- **T-BE-06:** AI failure preserves setup and records FAILED. → AC-09
- **T-BE-07:** Missing Project produces not-found error. → AC-11

### Persistence
- **T-DB-01:** Projects persist and list in updated-desc order. → AC-01/AC-02
- **T-DB-02:** Story persists with Project foreign key and setup. → AC-07/AC-08
- **T-DB-03:** Invalid constrained values are rejected. → AC-06
- **T-DB-04:** No unintended cascade/deletion behavior is introduced.

### API integration
- **T-API-01:** GET projects returns empty list when empty. → AC-01
- **T-API-02:** POST projects creates DRAFT project. → AC-03
- **T-API-03:** POST stories creates Blank Story. → AC-07
- **T-API-04:** POST stories creates AI Story using fake generator. → AC-08
- **T-API-05:** Invalid request returns validation error and no Story. → AC-06
- **T-API-06:** Unknown project returns 404. → AC-11
- **T-API-07:** Controlled AI failure returns recoverable error and preserves setup. → AC-09

### End-to-end
- **T-E2E-01:** Empty Dashboard → Create Story → Blank → Setup → Continue → persisted Story appears on Dashboard. → AC-01–AC-07
- **T-E2E-02:** Dashboard → Create Story → AI → Setup → Generate Story → generated draft is persisted. → AC-03–AC-05, AC-08
- **T-E2E-03:** AI failure leaves setup persisted and shows recoverable failure. → AC-09
- **T-E2E-04:** Dashboard refresh loads persisted project metadata. → AC-10

## Out of Scope

- Authentication/authorization.
- Collaboration/multi-user editing.
- Direct YouTube publishing.
- Real AI provider integration.
- Provider-specific prompt engineering or retry strategy.
- Image/character/background/voice/music/SFX generation.
- Story Outline implementation.
- Scene/character/asset management.
- Editor/canvas/timeline implementation.
- Composition editing.
- Preset animation implementation.
- Preview/render/FFmpeg/render jobs.
- Payments/subscriptions/quotas.
- Notifications.
- Cloud deployment.
- Kubernetes, Redis, queues, or additional microservices.

## Dependencies

- `apps/web` Next.js + TypeScript + Tailwind foundation.
- `apps/api` Java 21 + Spring Boot foundation.
- PostgreSQL + Flyway.
- `packages/shared-types`.
- `packages/validation`.
- Existing API conventions in `docs/api/api-conventions.md`.
- Existing AI boundary in `docs/architecture/ai-boundary.md`.
- Existing V1 scope in `docs/product/v1-scope.md`.
- Existing V1 screen definition in `docs/product/v1-screen-spec.md`.
- Existing Composition Model in `packages/editor-model`.

## Relevant Architectural Decisions

- ADR-001 — Editor/Composition Model is the source of truth.
- ADR-002 — Composition schema is versioned.
- ADR-003 — AI proposes suggestions/assets, not project state.
- ADR-004 — AI providers are abstracted.
- ADR-005 — Renderer consumes Composition JSON.
- ADR-006 — Media binaries use object storage.
- ADR-007 — Renderer is separate from API.
- ADR-009 — V1 is web desktop first.
- ADR-010 — No direct YouTube publishing in V1.
- ADR-011 — Avoid premature infrastructure.
- ADR-012 — SDD + TDD + feature implementation memory.

No architecture conflict requires a new ADR for this slice. The only unresolved product detail is the exact target-age option set, which must be agreed before implementation.

## Implementation Workflow

**Specification → Acceptance Criteria → Test Cases → Failing Tests → Implementation → Passing Tests → Refactor → Validation → Feature Implementation Documentation → Current-State Update**

Tests do not need to precede every line of code, but behavior must be test-first/test-driven wherever practical.

No application code or database migration is part of this specification-stage change.
