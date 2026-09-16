# Feature Specification — Story Creation

**Specification ID:** SPEC-001  
**Status:** IMPLEMENTED

## Goal
Enable a desktop-web creator to create a StorySprout project and Story either from AI-assisted generation or from a blank story, persist the setup, and continue the story workflow.

## Approved V1 target ages
The V1 target-age set is fixed and closed:
- `3_5` → Ages 3–5
- `6_8` → Ages 6–8
- `9_12` → Ages 9–12

V1 does not support individual ages, custom ranges, ages 13+, or any other target-age category.

## User flow

### Dashboard
1. Load projects from the API.
2. Show an empty state and `Create Story` when no projects exist.
3. Otherwise show project cards with name/title, status, and last-updated metadata.
4. `Create Story` creates a DRAFT project shell and enters creation.

### Create Story
1. Choose exactly `Start with AI` or `Blank Story`.
2. Continue to Story Setup.

### Story Setup
1. Enter story idea.
2. Select target age from the approved V1 set.
3. Select duration: `1`, `3`, or `5` minutes.
4. Select visual style: `2D`, `3D`, or `Hybrid`.
5. Select language: `English` or `Hindi`.
6. AI mode uses `Generate Story`; Blank mode uses `Continue`.
7. Validate and persist the setup.
8. AI mode invokes the internal `StoryGenerator` and persists generated title/draft.

Alternate paths:
- Abandoned draft projects remain drafts.
- Blank mode never invokes AI.
- AI failure preserves setup, marks the story `FAILED`, and is recoverable.
- Invalid input is not persisted.
- Unknown project IDs return not-found.

## Functional requirements
- **FR-01:** Dashboard lists persisted projects newest-updated-first.
- **FR-02:** Empty Dashboard provides `Create Story`.
- **FR-03:** Project cards show name/title, status, and updated metadata.
- **FR-04:** `Create Story` creates a `DRAFT` Project shell.
- **FR-05:** Creation mode is exactly `AI` or `BLANK`.
- **FR-06:** Story Setup captures idea, target age, duration, visual style, and language.
- **FR-07:** Target age is exactly `3_5`, `6_8`, or `9_12`; duration is `1`, `3`, or `5`; visual style is `2D`, `3D`, or `HYBRID`; language is `ENGLISH` or `HINDI`.
- **FR-08:** Blank Story persists setup without invoking `StoryGenerator`.
- **FR-09:** AI Story persists setup and invokes `StoryGenerator` through the internal boundary.
- **FR-10:** Story Creation application code has no direct provider SDK dependency.
- **FR-11:** A created Story's project is discoverable through Dashboard after refresh.

## UI requirements
Dashboard supports loading, ready, empty and error states. Create Story supports mutually exclusive AI/Blank choices and submission/loading/error states. Story Setup supports inline validation, all approved options, duplicate-submit prevention, AI generating state, success state, and recoverable generation failure.

## Domain requirements

### Project
- `id`
- `name`
- `status`
- `createdAt`
- `updatedAt`

New projects are `DRAFT`.

### Story
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
- `targetAge`: `3_5`, `6_8`, `9_12`
- `creationMode`: `AI`, `BLANK`
- `generationStatus`: `NOT_REQUESTED`, `GENERATING`, `COMPLETED`, `FAILED`
- `durationMinutes`: `1`, `3`, `5`
- `visualStyle`: `2D`, `3D`, `HYBRID`
- `language`: `ENGLISH`, `HINDI`

Story Creation does not create scenes, timeline clips, assets, dialogue tables, or populated Composition state. Composition schema `1.0` remains unchanged.

## API requirements
Base path: `/api/v1`.

### List projects
`GET /api/v1/projects` — returns projects ordered by `updatedAt DESC`.

### Create project shell
`POST /api/v1/projects`

Request:
```json
{"name":"Untitled Story"}
```

Creates only a DRAFT project and does not invoke AI.

### Create story
`POST /api/v1/projects/{projectId}/stories`

Request:
```json
{
  "creationMode": "AI",
  "idea": "A little rabbit learns to share.",
  "targetAge": "6_8",
  "durationMinutes": 3,
  "visualStyle": "2D",
  "language": "ENGLISH"
}
```

Response contains Story ID, Project ID, persisted setup, title, generation status, generated draft when available, and continuation information.

Errors use the repository-wide envelope and distinguish 400 validation, 404 project not found, 409 conflict where applicable, 422 controlled generation failure, and 500 unexpected server failure.

## Persistence requirements
Only `projects` and `stories` are created by this slice.

`projects` has UUID primary key, nonblank name, supported status, created/updated timestamps, and an `updated_at DESC` index.

`stories` has UUID primary key, project foreign key, required setup fields, nullable draft, created/updated timestamps, nonblank idea, and database CHECK constraints for target age, duration, visual style, language, creation mode, and generation status. It has an index on `project_id`.

A new Flyway migration is required. Existing foundation migrations are not rewritten.

## AI requirements

Internal contract:
```text
StoryGenerator.generate(StoryGenerationRequest) -> StoryGenerationResult
```

Request: idea, target age, duration, visual style, language.  
Result: generated title and generated draft story text.

Blank mode never calls the generator. AI mode calls it once. Success persists `COMPLETED`; controlled failure persists `FAILED` while retaining setup. AI output is Story content, not Composition state.

V1 uses a deterministic provider-free implementation. No real provider SDK, credentials, production prompt strategy, or provider-specific retry system is included.

## Validation and security
Validate at UI and API boundaries. Database constraints are the final persistence guard. Treat creator ideas and generated drafts as untrusted content, enforce reasonable request size, do not store credentials in story records, and do not publish content from this slice. Unexpected errors do not expose stack traces or credentials.

Authentication/authorization is outside this slice.

## Renderer requirements
None beyond compatibility. No Composition or renderer changes are permitted for Story Creation.

## Acceptance criteria
- **AC-01:** Empty Dashboard shows empty state and `Create Story`.
- **AC-02:** Existing projects render newest-updated-first with required metadata.
- **AC-03:** `Create Story` creates a DRAFT project and enters creation.
- **AC-04:** Exactly one AI/Blank mode is active.
- **AC-05:** Setup exposes idea, approved target ages, duration, style, and language.
- **AC-06:** Invalid/incomplete setup is rejected and not persisted.
- **AC-07:** Blank setup persists without AI.
- **AC-08:** AI setup invokes StoryGenerator and persists generated title/draft.
- **AC-09:** AI failure preserves setup, marks FAILED, and remains recoverable.
- **AC-10:** Refresh loads persisted project metadata.
- **AC-11:** Unknown project returns 404 and creates no Story.

## Test scenarios

### Frontend
- **T-UI-01:** Empty Dashboard.
- **T-UI-02:** Project cards.
- **T-UI-03:** Create Story action.
- **T-UI-04:** AI/Blank mutual exclusion.
- **T-UI-05:** Story Setup fields and allowed values.
- **T-UI-06:** Invalid setup blocks submission.
- **T-UI-07:** AI generating/success.
- **T-UI-08:** AI failure recovery.
- **T-UI-09:** Blank submission without AI.
- **T-UI-10:** Active submission prevents duplicate clicks.

### Backend unit/service
- **T-BE-01:** Project creation creates DRAFT.
- **T-BE-02:** Invalid setup is rejected.
- **T-BE-03:** Blank Story persists and does not call generator.
- **T-BE-04:** AI generator receives exact setup.
- **T-BE-05:** Successful generation persists title/draft/COMPLETED.
- **T-BE-06:** AI failure preserves setup and records FAILED.
- **T-BE-07:** Missing project is rejected.

### Persistence
- **T-DB-01:** Projects persist and list in updated-desc order.
- **T-DB-02:** Story/project foreign-key relationship.
- **T-DB-03:** Database constraints reject invalid values.
- **T-DB-04:** No unintended cascade/deletion behavior.

### API integration
- **T-API-01:** Empty project list.
- **T-API-02:** Project creation.
- **T-API-03:** Blank Story.
- **T-API-04:** AI Story with fake generator.
- **T-API-05:** Validation failure.
- **T-API-06:** Unknown project.
- **T-API-07:** Controlled AI failure and persistence.

### End-to-end
- **T-E2E-01:** Dashboard → Create Story → Blank → Setup → Continue → persisted Story.
- **T-E2E-02:** Dashboard → Create Story → AI → Setup → Generate Story → generated draft persisted.
- **T-E2E-03:** AI failure leaves setup and exposes recovery.
- **T-E2E-04:** Dashboard refresh loads persisted project.

## Out of scope
Authentication/authorization, collaboration, direct YouTube publishing, real AI providers, provider-specific prompt/retry systems, media generation, Story Outline implementation, scene/character/asset management, editor/canvas/timeline, Composition editing, animation, preview/render/FFmpeg, payments/subscriptions/quotas, notifications, cloud deployment, Kubernetes, Redis, queues, and additional microservices.

## Relevant architectural decisions
ADR-001, ADR-002, ADR-003, ADR-004, ADR-005, ADR-006, ADR-007, ADR-009, ADR-010, ADR-011, and ADR-012 remain applicable. No new architecture ADR is required for this slice.
