# Feature Specification — Story Creation

**Status:** SPECIFIED  
**Feature:** Story Creation  
**Specification ID:** SPEC-001  
**Implementation status:** Not implemented  
**Related decisions:** ADR-001 through ADR-012, especially ADR-003, ADR-004, ADR-009 and ADR-011

## 1. Problem

The repository foundation can run the web/API/renderer services, but a creator cannot yet create or persist a StorySprout project. The first product slice needs a small, complete vertical path from the dashboard through story setup to persisted project/story data, with an optional provider-independent AI generation path.

## 2. Goal

Enable a desktop-web creator to:

1. view existing projects,
2. start a new story,
3. choose AI-assisted or blank creation,
4. provide the required story setup,
5. persist the project and story setup,
6. optionally generate a draft story through the internal `StoryGenerator` boundary, and
7. continue to the next planned story workflow without entering the editor, timeline, renderer, or publishing flows.

The slice must be fully testable without a real AI provider.

## 3. User Story

**As a StorySprout creator, I want to start a new story either with AI or from a blank story, provide the basic story setup, and save it as a project so that I can continue building the story later.**

## 4. Exact User Flow

### 4.1 Dashboard → Create Story

1. Creator opens Dashboard.
2. Dashboard requests the project list.
3. If there are no projects, an empty state is shown with a primary `Create Story` action.
4. If projects exist, each project is shown as a basic card with title/name, status and last-updated metadata.
5. Creator selects `Create Story`.
6. The application creates a draft project shell and navigates to Create Story.

### 4.2 Create Story → Story Setup

1. Creator chooses `Start with AI` or `Blank Story`.
2. Creator proceeds to Story Setup.
3. Creator enters a story idea.
4. Creator selects target age.
5. Creator selects duration: `1`, `3`, or `5` minutes.
6. Creator selects visual style: `2D`, `3D`, or `Hybrid`.
7. Creator selects language: `English` or `Hindi`.
8. For `Start with AI`, the primary action is `Generate Story`.
9. For `Blank Story`, the primary action is `Continue`.
10. The setup is validated before submission.
11. The backend persists the Story and its setup.
12. For AI mode, the backend invokes the internal `StoryGenerator` and stores the returned draft/title on the Story.
13. On success, the creator receives the created Story/project identifiers and a continuation target for the next story workflow. This specification does not implement the Story Outline screen.

### 4.3 Important alternate paths

- Creator can leave Create Story without submitting; the draft project remains a normal draft project and can appear on Dashboard.
- Blank Story does not call an AI provider.
- AI generation failure does not delete the persisted project/story setup. The API returns a generation failure that the UI presents as recoverable.
- Validation failure does not persist an invalid Story setup.
- Project-not-found prevents Story creation and returns a stable not-found error.

## 5. Functional Requirements

### FR-01 — Dashboard project list
The Dashboard shall load persisted projects and display them in deterministic ordering by most recently updated first.

### FR-02 — Dashboard empty state
When no projects exist, the Dashboard shall display an empty state with a `Create Story` action.

### FR-03 — Project card
A project card shall display at minimum the project name/title, project status, and last-updated metadata.

### FR-04 — Create Story entry
`Create Story` shall create a draft Project shell and navigate to the creation flow.

### FR-05 — Creation mode
The creator shall choose exactly one creation mode: `AI` or `BLANK`.

### FR-06 — Story setup
The setup shall collect:
- story idea,
- target age,
- duration,
- visual style,
- language.

### FR-07 — Setup values
Duration is restricted to `1`, `3`, or `5` minutes. Visual style is restricted to `2D`, `3D`, or `HYBRID`. Language is restricted to `ENGLISH` or `HINDI`.

For this specification, target age is a required child-age selection represented as a stable domain value. The implementation must use a finite set of age options suitable for the product's child audience; the exact UI option labels/range must be finalized before implementation and represented by a stable API/domain value rather than free-form text.

### FR-08 — Blank story
Submitting a blank story persists the setup without invoking `StoryGenerator`.

### FR-09 — AI story
Submitting an AI story persists the setup and invokes `StoryGenerator` through the internal AI boundary. A successful result stores a generated title/draft on the Story.

### FR-10 — No provider coupling
No application/domain code in the Story Creation feature may depend directly on an external AI provider SDK.

### FR-11 — Dashboard refresh
After successful Story creation, the project shall be retrievable from Dashboard with the persisted metadata.

## 6. UI Requirements

### Dashboard
- Desktop-web-first layout.
- Project list state.
- Empty state.
- Loading state.
- Error state.
- Create Story primary action.
- Project cards show basic metadata only; no editor controls.

### Create Story
- Two mutually exclusive choices: `Start with AI`, `Blank Story`.
- Clear selected state.
- Continue action into Story Setup.
- Loading/error states for project-shell creation if the project is created at entry.

### Story Setup
- Story idea input.
- Target-age selector.
- Duration selector with exactly `1`, `3`, `5` minutes.
- Visual-style selector with exactly `2D`, `3D`, `Hybrid`.
- Language selector with exactly `English`, `Hindi`.
- AI mode action: `Generate Story`.
- Blank mode action: `Continue`.
- Inline validation for missing/invalid fields.
- Saving/submitting state prevents duplicate submission.
- AI mode additionally exposes a generating state and recoverable generation-failed state.
- No editor, timeline, character, scene, asset or render controls are included.

## 7. Domain Requirements

### Project
The existing shared type `Project` is extended only as needed for this feature. The domain concept represents the creator's top-level workspace.

Minimum persisted fields:
- `id`
- `name`
- `status`
- `createdAt`
- `updatedAt`

For this slice, newly created projects use `DRAFT` status.

### Story
A Story belongs to a Project and stores the setup required to create it.

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
- `draftContent` (nullable for blank stories and before successful AI generation)
- `createdAt`
- `updatedAt`

Recommended enum values:
- `creationMode`: `AI`, `BLANK`
- `generationStatus`: `NOT_REQUESTED`, `GENERATING`, `COMPLETED`, `FAILED`
- `durationMinutes`: `1`, `3`, `5`
- `visualStyle`: `2D`, `3D`, `HYBRID`
- `language`: `ENGLISH`, `HINDI`

A Story must reference an existing Project.

### Composition boundary
Story Creation does not create Scenes, TimelineClips, assets, or a populated Composition. The existing Composition schema `1.0` remains unchanged by this feature.

## 8. API Requirements

The existing API convention uses `/api/v1` and opaque identifiers/UUIDs. citeturn153file0

### 8.1 List projects

`GET /api/v1/projects`

Response:
```json
{
  "data": [
    {
      "id": "...",
      "name": "...",
      "status": "DRAFT",
      "updatedAt": "..."
    }
  ]
}
```

Default ordering: `updatedAt DESC`.

### 8.2 Create project shell

`POST /api/v1/projects`

Request:
```json
{
  "name": "Untitled Story"
}
```

Response: created Project metadata.

The endpoint creates only a draft Project. It does not create a Story and does not invoke AI.

### 8.3 Create Story

`POST /api/v1/projects/{projectId}/stories`

Request:
```json
{
  "creationMode": "AI",
  "idea": "A little rabbit learns to share.",
  "targetAge": "AGE_BAND",
  "durationMinutes": 3,
  "visualStyle": "2D",
  "language": "ENGLISH"
}
```

For `BLANK`, the same request shape is used with `creationMode: "BLANK"`; AI is not invoked.

Response contains:
- Story identifier,
- Project identifier,
- persisted setup,
- title,
- generation status,
- generated draft when available,
- continuation information for the next workflow.

### 8.4 Error contract
Use the existing `/api/v1` JSON conventions. Errors must distinguish at least:
- `400` invalid request/validation,
- `404` project not found,
- `409` duplicate/invalid state transition where applicable,
- `422` AI generation could not produce an acceptable result,
- `500` unexpected server failure.

Exact error envelope should follow the existing API error conventions when they are implemented; this feature must not invent a competing global error format.

### 8.5 Duplicate submission
The frontend must prevent accidental duplicate submission while a request is active. Backend idempotency is not required for this first slice unless the implementation introduces a concrete need.

## 9. Persistence Requirements

Only two domain tables are genuinely required for this slice:

### `projects`
Fields:
- `id UUID PRIMARY KEY`
- `name VARCHAR(...) NOT NULL`
- `status VARCHAR(...) NOT NULL`
- `created_at TIMESTAMPTZ NOT NULL`
- `updated_at TIMESTAMPTZ NOT NULL`

Constraints:
- non-empty name after trimming,
- status restricted to supported Project status values.

Indexes:
- index on `updated_at DESC` for Dashboard ordering.

### `stories`
Fields:
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
- foreign key to `projects`.
- duration limited to `1`, `3`, `5`.
- visual style limited to `2D`, `3D`, `HYBRID`.
- language limited to `ENGLISH`, `HINDI`.
- creation mode limited to `AI`, `BLANK`.
- generation status limited to defined states.
- idea must be non-empty after trimming.

Indexes:
- index on `project_id`.
- index on `updated_at DESC` if query plans/use justify it; avoid speculative indexes.

### Migration
A new Flyway migration must be created during implementation, after this specification is approved. The existing foundation migration must not be rewritten. No migration is created in this specification stage.

## 10. AI Requirements

### Internal interface
The API/application layer shall depend on an internal interface equivalent to:

```text
StoryGenerator.generate(StoryGenerationRequest) -> StoryGenerationResult
```

Request contains:
- idea,
- target age,
- duration,
- visual style,
- language.

Result contains at minimum:
- generated title,
- generated draft story text.

The interface must not expose provider-specific request/response types.

### Behavior
- `BLANK` mode never calls `StoryGenerator`.
- `AI` mode calls it once for the creation request.
- Successful generation changes `generationStatus` to `COMPLETED`.
- Failure changes `generationStatus` to `FAILED` and preserves the submitted setup.
- AI output is stored as editable story content, not canonical Composition state.
- The generated story is not automatically converted into scenes, characters, assets, animation, or timeline data.

### Test provider
Tests shall use an in-memory fake/mock `StoryGenerator` that:
- returns deterministic title/draft output for success tests,
- can be configured to throw/return a controlled failure for failure tests,
- records invocation count and request values so tests can prove AI is not called for blank stories and is called with the persisted setup for AI stories.

### Real provider
No real AI provider integration is part of this implementation slice. Provider selection, credentials, SDKs, prompts, production moderation integration and provider-specific retry strategy remain future work.

## 11. Validation / Error Requirements

Frontend and backend must validate the same business constraints at their respective boundaries.

Required validation:
- story idea present and non-blank,
- target age selected,
- duration in `1/3/5`,
- visual style in `2D/3D/Hybrid`,
- language in `English/Hindi`,
- creation mode in `AI/BLANK`,
- project exists before Story creation.

Error behavior:
- invalid input is rejected without persistence,
- project-not-found is reported as a not-found error,
- AI failure preserves the Story setup and exposes a retryable failure state,
- unexpected failures do not expose provider credentials or internal stack traces to the browser.

## 12. Security / Content Safety

StorySprout creates content intended for children. This feature must follow the existing content-safety baseline without prematurely implementing a moderation platform. fileciteturn160file0L2-L2

Requirements for this slice:
- treat story idea and generated draft as untrusted content,
- validate request sizes and reject excessively large inputs,
- do not send unnecessary personal data to an AI provider,
- do not store provider credentials in project/story records,
- do not silently publish or distribute generated content,
- generation failures must be visible to the creator.

Authentication/authorization is explicitly outside this slice because it is not currently implemented in the foundation.

## 13. Renderer Requirements

None beyond preserving architectural compatibility.

Story Creation must not modify the Composition Model or renderer semantics. The existing model is versioned at schema `1.0`; renderer input remains Composition JSON plus referenced assets. fileciteturn151file0L2-L2

No renderer code, FFmpeg pipeline, render job execution, scene composition, audio composition, or media generation is included.

## 14. Test Scenarios

Tests are defined before implementation and must map to acceptance criteria.

### Frontend unit/component tests

- **T-UI-01:** Dashboard renders empty state when project list is empty. → AC-01
- **T-UI-02:** Dashboard renders project cards with required metadata. → AC-02
- **T-UI-03:** Create Story action starts the creation flow. → AC-03
- **T-UI-04:** AI/Blank choices are mutually exclusive. → AC-04
- **T-UI-05:** Story Setup renders all required fields and allowed options. → AC-05
- **T-UI-06:** Invalid/missing setup prevents submission and shows validation. → AC-06
- **T-UI-07:** AI submission shows generating state and successful continuation. → AC-08
- **T-UI-08:** AI failure shows recoverable generation-failed state. → AC-09
- **T-UI-09:** Blank submission continues without AI generation. → AC-07
- **T-UI-10:** Active submission prevents duplicate clicks. → AC-10

### Backend unit/service tests

- **T-BE-01:** Project creation creates a DRAFT project with timestamps. → AC-03
- **T-BE-02:** Story setup validation rejects invalid duration/style/language/missing idea/target age. → AC-06
- **T-BE-03:** Blank Story creation persists setup and never calls StoryGenerator. → AC-07
- **T-BE-04:** AI Story creation calls StoryGenerator with exact setup values. → AC-08
- **T-BE-05:** Successful Story generation persists generated title/draft and COMPLETED status. → AC-08
- **T-BE-06:** AI failure preserves setup and records FAILED status. → AC-09
- **T-BE-07:** Missing project produces a not-found domain/API error. → AC-11

### Persistence/repository tests

- **T-DB-01:** Project persists and can be listed ordered by updated timestamp. → AC-01, AC-02
- **T-DB-02:** Story persists with project foreign-key relationship and setup fields. → AC-07, AC-08
- **T-DB-03:** Invalid enum/duration values are rejected at the application/database boundary. → AC-06
- **T-DB-04:** Project deletion behavior is not introduced unless explicitly specified; the implementation must preserve the intended foreign-key policy. → Constraint check

### API integration tests

- **T-API-01:** `GET /api/v1/projects` returns an empty list for no projects. → AC-01
- **T-API-02:** `POST /api/v1/projects` creates a DRAFT project. → AC-03
- **T-API-03:** `POST /api/v1/projects/{id}/stories` creates a blank Story. → AC-07
- **T-API-04:** Same endpoint creates an AI Story using the fake StoryGenerator. → AC-08
- **T-API-05:** Invalid request returns validation error without creating a Story. → AC-06
- **T-API-06:** Unknown project returns 404. → AC-11
- **T-API-07:** Controlled AI failure returns a recoverable generation error and preserves Story setup. → AC-09

### End-to-end acceptance scenarios

- **T-E2E-01:** Empty Dashboard → Create Story → Blank Story → Setup → Continue → Story is persisted and visible on Dashboard. → AC-01 through AC-07
- **T-E2E-02:** Dashboard → Create Story → Start with AI → Setup → Generate Story → generated draft is returned and persisted. → AC-03 through AC-05, AC-08
- **T-E2E-03:** AI generation failure leaves persisted setup and shows a recoverable failure state. → AC-09
- **T-E2E-04:** Refresh Dashboard after successful creation and verify persisted project metadata is loaded from the API. → AC-10

## 15. Acceptance Criteria

### AC-01 — Empty Dashboard
**Given** no projects exist, **when** the creator opens Dashboard, **then** an empty state and `Create Story` action are displayed.

### AC-02 — Project List
**Given** projects exist, **when** Dashboard loads, **then** projects are displayed newest-updated-first with basic name/title, status and updated metadata.

### AC-03 — Create Project
**Given** the creator selects `Create Story`, **when** project creation succeeds, **then** a DRAFT Project exists and the creator enters the Story Creation flow.

### AC-04 — Creation Mode
**Given** the creator is on Create Story, **when** they select AI or Blank, **then** exactly one creation mode is active and the appropriate next action is available.

### AC-05 — Story Setup
**Given** a creation mode is selected, **when** the creator reaches Story Setup, **then** the required idea, target age, duration, visual style and language fields are available with the specified allowed values.

### AC-06 — Validation
**Given** invalid or incomplete setup, **when** the creator submits, **then** the UI shows validation errors and the API does not persist an invalid Story.

### AC-07 — Blank Story
**Given** valid setup with `BLANK` mode, **when** the creator selects `Continue`, **then** a Story is persisted with the setup and no StoryGenerator invocation occurs.

### AC-08 — AI Story
**Given** valid setup with `AI` mode, **when** the creator selects `Generate Story`, **then** the backend invokes the internal StoryGenerator, persists a successful generated title/draft, and returns a successful continuation response.

### AC-09 — AI Failure
**Given** valid AI setup and a StoryGenerator failure, **when** generation fails, **then** the setup remains persisted, generation status is `FAILED`, and the UI exposes a recoverable failure state without publishing or deleting the Story.

### AC-10 — Persistence Across Refresh
**Given** a Story has been successfully created, **when** the creator returns to or refreshes Dashboard, **then** the associated project appears using persisted API data rather than transient frontend state.

### AC-11 — Invalid Project
**Given** a nonexistent project ID, **when** Story creation is requested, **then** the API returns a stable not-found response and does not create a Story.

## 16. Out of Scope

- Authentication and authorization.
- Collaboration or multi-user editing.
- Direct YouTube publishing.
- Real AI provider integration.
- Advanced prompt engineering/provider-specific prompts.
- Image, character, background, voice, music or SFX generation.
- Story Outline implementation.
- Scene creation.
- Character management.
- Asset management/upload.
- Editor/canvas/timeline UI.
- Composition editing.
- Preset animation implementation.
- Preview implementation.
- Rendering/FFmpeg/render jobs.
- Payments, subscriptions or quotas.
- Notifications.
- Cloud deployment.
- Kubernetes, Redis, queues or additional microservices.

## 17. Dependencies

Existing repository dependencies:
- `apps/web` Next.js + TypeScript + Tailwind foundation.
- `apps/api` Java 21 + Spring Boot foundation.
- PostgreSQL + Flyway foundation.
- `packages/shared-types` for cross-application types.
- `packages/validation` for runtime validation.
- Existing `/api/v1` API conventions. fileciteturn153file0L2-L2
- Existing AI boundary architecture. fileciteturn149file0L2-L2
- Existing V1 product scope and screen definition. fileciteturn154file0L2-L2 fileciteturn155file0L2-L2

## 18. Relevant Architectural Decisions

- **ADR-001:** Editor/Composition Model is the source of truth.
- **ADR-002:** Composition schema is versioned.
- **ADR-003:** AI proposes suggestions/assets, not project state.
- **ADR-004:** AI providers are abstracted.
- **ADR-005:** Renderer consumes Composition JSON.
- **ADR-006:** Media binaries use object storage.
- **ADR-007:** Renderer is separate from API.
- **ADR-009:** V1 is web desktop first.
- **ADR-010:** No direct YouTube publishing in V1.
- **ADR-011:** Avoid premature infrastructure.
- **ADR-012:** SDD + TDD + feature implementation memory.

No new architectural decision is required for this specification because Story Creation fits the existing architecture. The only implementation-level product detail intentionally left open is the exact finite target-age option set; that must be resolved before implementation rather than silently invented in code.

## 19. Implementation Workflow

When implementation is explicitly requested, follow:

**Specification → Acceptance Criteria → Test Cases → Failing Tests → Implementation → Passing Tests → Refactor → Validation → Feature Implementation Documentation → Current-State Update**

No application code or database migration is part of this specification-stage change.
