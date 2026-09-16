# Feature Specification — Story Outline

**Specification ID:** SPEC-003  
**Status:** SPECIFIED — implementation not started

## 1. Problem

Story Creation currently produces a persisted Story and, in AI mode, a title and draft. The next creator task is to turn that narrative into an ordered, editable plan of scenes before any visual scene-building work begins.

The repository already separates AI suggestions from canonical project state and keeps the Editor/Composition Model as the source of truth for editable video semantics. Story Outline must therefore remain a planning/content layer and must not become Composition.

## 2. Goal

Allow a creator to open a valid Story, generate a structured outline with ordered scenes, edit that outline, save it, and continue toward the future Scene Setup flow without creating or modifying Composition state.

## 3. User story

As a StorySprout creator, I want to turn my story into an ordered list of scenes that I can review and edit, so that I have a clear narrative plan before setting up visual scenes.

## 4. User flow

### Existing entry flow

`Dashboard → Story → Story Outline`

The current Dashboard lists projects but does not yet provide a Story/Outline route. Story Outline implementation will need to add the smallest navigation/linking change required to open the persisted Story. The existing `StoryResponse.continuationPath` currently points to `/dashboard`, while the actual Dashboard route is `/`; this is an existing implementation inconsistency that must be corrected as part of the Story Outline integration, not treated as a new navigation system.

### Story Outline happy path

1. Creator opens a valid Story.
2. Story Outline loads the Story title, story setup metadata, and existing draft content when present.
3. If no outline exists, the screen shows an empty outline state and `Generate Outline`.
4. Creator requests generation.
5. The application sends the Story content and required setup context through `StoryOutlineGenerator`.
6. AI returns a typed structured outline.
7. The application validates the result and persists the generated scenes as outline content.
8. Scenes display in explicit order.
9. Creator edits scene content and/or duration, adds or deletes scenes, and reorders scenes.
10. Creator saves changes.
11. Refresh loads the persisted outline.
12. Creator can continue to the future Scene Setup flow. The destination may be a placeholder/navigation contract only; Scene Setup itself is out of scope.

### Alternate paths

- Existing saved outline loads without requiring AI.
- AI generation failure leaves existing persisted outline unchanged.
- Invalid generated output is rejected and is not partially persisted.
- Unsaved edits are not silently discarded by navigation or regeneration.
- Invalid story or scene identifiers return the existing API error envelope.

## 5. Functional requirements

- **FR-01:** A valid Story can be opened in Story Outline.
- **FR-02:** Story Outline displays the Story title and relevant story metadata.
- **FR-03:** Existing story draft content is displayed where useful for outline editing/reference.
- **FR-04:** A Story with no outline has an explicit empty state and `Generate Outline` action.
- **FR-05:** AI generation uses a capability-specific `StoryOutlineGenerator` boundary.
- **FR-06:** AI output is a typed structured outline, not arbitrary prose parsed by application code.
- **FR-07:** Generated scenes have unique identity, explicit order, title, summary, and planned duration.
- **FR-08:** Creator can edit a scene title, summary, and planned duration.
- **FR-09:** Creator can add a scene.
- **FR-10:** Creator can delete a scene.
- **FR-11:** Creator can reorder scenes.
- **FR-12:** Creator can save the current outline.
- **FR-13:** Saved changes survive page refresh.
- **FR-14:** AI suggestions never become canonical state until accepted/persisted into the Story Outline.
- **FR-15:** Regeneration never silently overwrites creator-authored outline content.
- **FR-16:** Story Outline does not create or modify Composition JSON, timeline clips, scene objects, assets, animation, camera, or renderer data.

## 6. UI requirements

V1 is desktop-web-first and should follow the existing StorySprout visual conventions rather than introducing a new design system.

### Header

Show:
- Story title
- Story setup metadata: target age, target duration, visual style, language
- Save state: `Saved`, `Unsaved changes`, `Saving…`, or save error
- Navigation back to the Story/Dashboard context where applicable

### Story reference

Show the existing story draft in a compact reference area. It should be readable without competing with the scene list. If the Story is blank and has no draft, show the story idea instead.

### Main outline area

Each scene row/card contains:
- scene number/order
- scene title
- scene summary/description
- planned duration
- Edit action
- Delete action
- reorder control

`Add Scene` is available after the existing scenes and creates a new unsaved scene with no AI call.

### Primary actions

- `Generate Outline`
- `Add Scene`
- `Save Changes`
- `Continue to Scene Setup`

`Generate Outline` is the only AI generation action in this slice.

### Loading and empty states

- Story loading state
- Outline loading state
- Empty outline state
- AI generation state
- Save state
- Delete confirmation state when deletion is destructive

### Validation states

Inline validation should identify the affected scene/field. Save must remain blocked while invalid outline data exists.

### Unsaved changes

If the creator attempts to leave with unsaved changes, the UI must not silently discard them. The practical V1 behavior is a browser/navigation confirmation where supported, plus explicit `Save Changes` before continuing. No draft autosave is required.

### Accessibility

Interactive controls must have accessible names. Reorder controls must communicate the affected scene and direction. Status/error messages use appropriate live/alert semantics without relying on color alone.

## 7. Domain model

Story Outline is a conceptual planning structure belonging to a Story. For V1, its persisted scene records are the outline's ordered scene units.

### Story

Existing Story remains authoritative for:
- id
- projectId
- title
- idea
- targetAge
- durationMinutes
- visualStyle
- language
- creationMode
- generationStatus
- draftContent
- timestamps

### Outline Scene

Proposed persisted entity:
- `id` — UUID
- `storyId` — UUID foreign key to Story
- `orderIndex` — positive integer representing current sequence
- `title` — required, nonblank, bounded text
- `summary` — required, nonblank, bounded text
- `durationSeconds` — positive integer planned duration
- `createdAt`
- `updatedAt`

The specification deliberately calls this an **Outline Scene** in documentation to distinguish it from future visual/Composition scene semantics. It is a narrative planning record only.

No characters, dialogue records, backgrounds, props, assets, animation, camera, timeline or media references belong to this entity in V1.

## 8. Persistence requirements

A future implementation should add a new Flyway migration; existing Story Creation migrations must not be rewritten.

Proposed table: `story_outline_scenes`.

Required properties:
- UUID primary key
- `story_id` foreign key to `stories.id`
- `order_index` NOT NULL
- `title` NOT NULL and nonblank
- `summary` NOT NULL and nonblank
- `duration_seconds` NOT NULL and positive
- `created_at` NOT NULL
- `updated_at` NOT NULL

Constraints/indexes:
- Foreign key from scene to Story.
- Unique `(story_id, order_index)` so one Story cannot have duplicate scene positions.
- Index on `story_id, order_index` for ordered loading.
- No cascade deletion should be introduced unless the implementation explicitly proves that Story deletion semantics require it; current Story Creation does not expose Story deletion.

Persistence must save an outline atomically at the operation level. AI generation must not leave a partially generated outline if validation fails.

## 9. Scene duration decision

**Decision for V1:** scene duration is a **planned duration in integer seconds**, generated by AI and editable by the creator.

Story duration remains the target duration from Story Creation:
- 1 minute = 60 seconds
- 3 minutes = 180 seconds
- 5 minutes = 300 seconds

AI should allocate scene durations so their total equals the Story target duration. Individual durations are planning estimates, not frame-accurate render timing.

Save-time validation requires:
- every scene duration is a positive integer;
- total scene duration equals the Story target duration;
- an outline cannot be saved with zero scenes;
- a generated or edited scene cannot have duration greater than the Story target duration.

This gives the future Scene Setup a concrete timing budget without introducing a timeline system. Frame-level timing remains a future Composition concern.

## 10. API requirements

Base path: `/api/v1`.

The minimum proposed REST surface is:

### Get Story

`GET /api/v1/projects/{projectId}/stories/{storyId}`

Returns the existing Story representation needed by the outline screen. The implementation must verify that the Story belongs to the supplied Project.

### Get outline scenes

`GET /api/v1/stories/{storyId}/outline-scenes`

Returns scenes ordered by `orderIndex ASC`.

### Generate outline

`POST /api/v1/stories/{storyId}/outline/generate`

Generates and persists an outline only when the Story currently has no persisted outline. Returns the saved ordered scenes.

If an outline already exists, this endpoint does not regenerate it in V1; it returns a conflict/controlled response instructing the creator to keep the existing outline. This prevents accidental destructive replacement.

### Create scene

`POST /api/v1/stories/{storyId}/outline-scenes`

Creates one creator-authored scene. The server assigns the next order position and validates the resulting total duration before saving.

### Update scene

`PATCH /api/v1/stories/{storyId}/outline-scenes/{sceneId}`

Updates editable outline fields: title, summary, and durationSeconds. The server verifies Story ownership and validates the resulting outline duration/order constraints.

### Delete scene

`DELETE /api/v1/stories/{storyId}/outline-scenes/{sceneId}`

Deletes the selected scene and compacts remaining `orderIndex` values in one transaction.

### Reorder scenes

`PATCH /api/v1/stories/{storyId}/outline-scenes/reorder`

Request contains the complete ordered list of scene IDs. The server validates that the IDs belong to the Story, are unique, and contain exactly the current scene set, then rewrites order positions atomically.

No separate generic `Save Outline` endpoint is required for V1 because scene creation/update/delete/reorder operations persist their changes. The UI `Save Changes` action is therefore a coordinated UX action that flushes pending edits; it does not introduce a second persistence model.

The existing repository-wide API success/error envelope remains authoritative. No Story Outline-specific error envelope is introduced.

## 11. AI requirements

### Capability boundary

Introduce the semantic application-level interface:

```text
StoryOutlineGenerator.generate(StoryOutlineGenerationRequest)
    -> StoryOutlineGenerationResult
```

The exact Java package should follow the existing `com.storysprout.api.ai` / capability boundary conventions after implementation inspection, but the contract must remain provider-independent.

### Request

The request should contain only information required to outline the Story:
- Story idea
- Story draft when available
- target age
- Story duration
- visual style
- language

### Result

The provider-neutral result should contain an ordered collection of:
- generated scene title
- generated scene summary
- generated durationSeconds

The persistence layer assigns database identity and authoritative order identity after validation. AI should not own database IDs.

### Gemini integration

Reuse the existing Spring AI/Google GenAI infrastructure pattern and approved `gemini-2.5-flash` model configuration. Do not duplicate provider configuration or create a generic AI service.

Conceptually:

```text
StoryOutlineGenerator
        ↓
GeminiStoryOutlineGenerator
        ↓
existing Spring AI ChatClient infrastructure
        ↓
Gemini
```

Spring AI/Gemini classes remain in infrastructure. Application/domain code must not import them.

### Structured output

Use typed provider structured output, equivalent in principle to the existing Gemini Story Generator approach. Do not parse arbitrary prose into scenes.

The AI output must be validated before persistence:
- at least one scene;
- title and summary nonblank;
- durations positive integers;
- no duplicate scene positions if the provider returns positions;
- duration total equals the Story target duration;
- reasonable field/request size limits.

The application assigns canonical ordering from the validated returned sequence rather than trusting arbitrary provider IDs.

### Deterministic fake

Normal CI and standard tests must use a deterministic `Fake/DeterministicStoryOutlineGenerator`. No Gemini credentials or network are required.

### Failure handling

Provider failure, timeout, malformed output, invalid durations, or invalid scene data must produce a controlled generation error. Existing persisted outline content must remain unchanged.

The AI operation is atomic from the Story Outline perspective: validate the complete result first, then persist all generated scenes together.

## 12. Editing model

The persisted outline is the source of truth for the planning layer after user changes are saved.

AI generation is a proposal. Creator edits replace AI-proposed values in canonical outline state.

Editable fields in V1:
- title
- summary
- durationSeconds

No inline AI rewrite, scene-level regeneration, dialogue generation, character generation, or visual generation is included.

### Regeneration behavior

**V1 decision: regeneration of an existing outline is out of scope.**

`Generate Outline` is available only when no outline exists. If scenes already exist, the UI does not offer destructive whole-outline regeneration. This guarantees that creator edits cannot be silently overwritten and avoids needing merge/diff semantics in V1.

If future product requirements need regeneration, it must be specified separately with an explicit replacement/merge UX.

## 13. Reordering model

The canonical persisted order is `orderIndex` per Story, starting at `1` and remaining contiguous after successful save.

UI reordering may be implemented with simple move-up/move-down controls in V1; drag-and-drop is optional and should not be added solely to support the feature.

Rules:
- no duplicate order values;
- order values are contiguous after persistence;
- moving a scene updates affected positions atomically;
- deleting a scene compacts remaining positions;
- adding a scene appends it to the end;
- reordering does not change scene content or duration.

## 14. Validation

### Story
- Story must exist.
- Story must be associated with the requested Project when a Project-scoped route is used.
- Story must have valid existing Story Creation setup.

### Scene
- Scene must exist for update/delete.
- Scene must belong to the requested Story.
- Title is required and nonblank.
- Summary is required and nonblank.
- Title/summary lengths must have explicit bounded limits in implementation/API validation.
- Duration is a positive integer and cannot exceed the Story target duration.

### Outline
- Empty outline is valid only before generation/creation; it cannot be saved as a completed outline.
- At least one scene is required for a saved outline.
- Scene order must be unique and contiguous after persistence.
- Total scene duration must equal the Story target duration.
- Reorder request must contain exactly the current scene IDs, once each.
- Duplicate create/update/reorder submissions must not produce duplicate scene positions or corrupt ordering. Frontend disables active actions; backend transactions/constraints remain authoritative.

### IDs
- Invalid UUID syntax is rejected by the API boundary.
- Unknown Story/Scene IDs return the repository's normal not-found error behavior.

Authentication/authorization is not introduced; the current V1 architecture has no authentication layer. Ownership validation means relational correctness, not user authorization.

## 15. Error handling

Use the existing API error envelope.

Expected cases:
- `400` — invalid scene fields, invalid duration, invalid reorder payload.
- `404` — Story or Scene does not exist, or Scene does not belong to the requested Story.
- `409` — generation requested when an outline already exists, or a conflicting persistence operation where appropriate.
- `422` — controlled AI generation failure/malformed AI result.
- `500` — unexpected server failure.

AI failure must preserve all previously persisted outline scenes. If generation is the first outline and fails, no scenes are persisted.

Save failures leave the editor's unsaved state visible so the creator can retry; the UI must not falsely display `Saved`.

Delete should require confirmation when the scene contains saved content because it is destructive. If deletion is cancelled, no API call occurs.

## 16. Security and child-safety

- Do not introduce authentication or permissions in this slice.
- Treat story ideas, drafts, scene titles and summaries as untrusted creator content.
- Bound input sizes at API and persistence boundaries.
- Do not log full creator prompts, drafts, or generated outline content by default.
- Do not expose provider credentials or raw provider payloads.
- AI output must be validated before persistence.
- Content safety policy remains a platform concern; Story Outline must not bypass the existing content-safety boundary when one is introduced.

## 17. Acceptance criteria

- **AC-01:** Given a valid Story, when the creator opens Story Outline, then the Story title and relevant Story metadata are displayed.
- **AC-02:** Given a Story with no outline, when Story Outline loads, then an explicit empty state and `Generate Outline` action are displayed.
- **AC-03:** Given a valid Story and no existing outline, when the creator generates an outline, then `StoryOutlineGenerator` receives the provider-neutral Story outline request and a complete structured outline is validated and persisted.
- **AC-04:** Generated scenes appear in deterministic sequence order and each has title, summary, and planned duration.
- **AC-05:** Given generated or saved scenes, when the creator edits a scene and saves, then the edited values persist after refresh.
- **AC-06:** When the creator adds a scene, the new scene is appended with the next order position and persists after save/refresh.
- **AC-07:** When the creator deletes a scene and confirms, remaining scenes are compacted into contiguous order and persist after refresh.
- **AC-08:** When the creator reorders scenes, the new order is persisted without changing scene content.
- **AC-09:** Invalid title, summary, duration, order, or Story/Scene identifier is rejected using existing validation/error conventions.
- **AC-10:** A saved outline contains at least one scene and total planned scene duration equals the Story target duration.
- **AC-11:** When AI generation fails, times out, or returns invalid structured data, existing persisted outline content is unchanged; a first-generation failure persists no partial scenes.
- **AC-12:** When an outline already exists, `Generate Outline` does not silently replace it; existing creator content remains intact.
- **AC-13:** Refreshing Story Outline reloads the persisted Story and scene list.
- **AC-14:** Story Outline creates no Composition JSON and does not create/modify TimelineClip, SceneObject, animation, camera, asset-placement, renderer, or rendering state.
- **AC-15:** Normal CI uses the deterministic fake outline generator and does not require live Gemini credentials or network access.
- **AC-16:** Story Outline remains a planning/content layer between Story and future Scene Setup.

## 18. Test scenarios

### Frontend

- **T-UI-01:** Valid Story Outline loads Story title/metadata and existing scenes.
- **T-UI-02:** Empty outline state shows `Generate Outline`.
- **T-UI-03:** Generated scenes render in order with title, summary, and duration.
- **T-UI-04:** Scene title/summary/duration editing updates unsaved state.
- **T-UI-05:** Invalid scene fields block save and show field-level errors.
- **T-UI-06:** Add Scene appends a new scene.
- **T-UI-07:** Delete confirmation prevents accidental deletion; confirmed deletion removes the scene.
- **T-UI-08:** Reorder changes visible sequence and marks outline unsaved.
- **T-UI-09:** Save state transitions `Unsaved changes → Saving… → Saved` on success.
- **T-UI-10:** Save failure keeps unsaved state and exposes retryable error.
- **T-UI-11:** AI loading state disables duplicate generation.
- **T-UI-12:** AI failure shows a useful error without clearing existing scenes.
- **T-UI-13:** Existing outline does not expose destructive regeneration.
- **T-UI-14:** Navigation with unsaved changes does not silently discard edits.
- **T-UI-15:** Continue action is blocked while invalid or unsaved state would cause data loss.

### Backend/domain/service

- **T-BE-01:** Existing Story can be retrieved for outline context.
- **T-BE-02:** Scene creation assigns next order and validates duration.
- **T-BE-03:** Scene update validates ownership and resulting total duration.
- **T-BE-04:** Scene deletion compacts order atomically.
- **T-BE-05:** Reorder validates exact current scene ID set and persists contiguous order.
- **T-BE-06:** Invalid Story ID returns not-found.
- **T-BE-07:** Invalid Scene ID returns not-found.
- **T-BE-08:** Scene from another Story cannot be updated/deleted/reordered through the requested Story.
- **T-BE-09:** Empty saved outline is rejected.
- **T-BE-10:** Duplicate order cannot persist.
- **T-BE-11:** Total duration must equal Story target duration.
- **T-BE-12:** Outline generation persists all validated scenes atomically.
- **T-BE-13:** Generation failure leaves an existing outline unchanged.
- **T-BE-14:** First-generation failure leaves no partial scenes.
- **T-BE-15:** Existing outline blocks regeneration in V1.

### Persistence

- **T-DB-01:** Story-to-outline-scene foreign key is enforced.
- **T-DB-02:** `(story_id, order_index)` uniqueness is enforced.
- **T-DB-03:** Ordered lookup returns ascending order.
- **T-DB-04:** Positive duration and nonblank constraints reject invalid records.
- **T-DB-05:** Deletion/reordering does not leave duplicate or missing order positions after successful transaction.
- **T-DB-06:** Existing Story Creation tables/migration remain unchanged.

### AI

- **T-AI-01:** `StoryOutlineGenerationRequest` contains idea, draft, target age, duration, style, and language exactly as specified.
- **T-AI-02:** Prompt construction includes all required Story context and clear scene-output instructions.
- **T-AI-03:** Valid structured result maps to provider-neutral outline result.
- **T-AI-04:** Missing scene list is rejected.
- **T-AI-05:** Blank scene title/summary is rejected.
- **T-AI-06:** Zero/negative/non-integer duration is rejected.
- **T-AI-07:** Duplicate provider order values are rejected if provider order is represented.
- **T-AI-08:** Generated duration total must equal Story target duration.
- **T-AI-09:** Provider failure is translated to controlled generation failure.
- **T-AI-10:** Timeout is translated to controlled generation failure.
- **T-AI-11:** Transient retry remains bounded using the existing Gemini reliability pattern.
- **T-AI-12:** Deterministic fake generator produces stable test output without network/credentials.
- **T-AI-13:** Provider-specific Spring AI/Gemini types remain outside application/domain contracts.
- **T-AI-14:** Existing StoryGenerator/Gemini integration is reused rather than duplicated as a generic AI service.

### API integration

- **T-API-01:** Get valid Story for outline context.
- **T-API-02:** Get empty outline.
- **T-API-03:** Generate outline with deterministic fake generator.
- **T-API-04:** Generate outline when an outline already exists returns controlled conflict/no replacement.
- **T-API-05:** Create scene.
- **T-API-06:** Update scene.
- **T-API-07:** Delete scene.
- **T-API-08:** Reorder scene IDs.
- **T-API-09:** Invalid Story/Scene ID.
- **T-API-10:** Invalid duration/title/summary/order.
- **T-API-11:** AI generation failure returns controlled error and preserves persisted content.

### E2E

- **T-E2E-01:** Dashboard → open Story → Story Outline → Generate Outline → scenes appear → refresh → scenes remain.
- **T-E2E-02:** Story Outline → edit scene → save → refresh → edited scene remains.
- **T-E2E-03:** Story Outline → add scene → reorder → delete → save → refresh → final ordering persists.
- **T-E2E-04:** AI failure → useful error → existing outline content preserved.
- **T-E2E-05:** Existing outline → refresh → no destructive regeneration action is exposed.
- **T-E2E-06:** Invalid scene edit → save blocked → creator can correct and save.

## 19. Out of scope

- Scene Setup implementation
- Editor/Composition implementation
- Timeline
- Character placement
- Background placement
- Props
- Assets/media generation
- Dialogue generation or dialogue persistence
- Animations
- Camera instructions
- Rendering
- FFmpeg
- Voice
- Music
- SFX
- YouTube publishing
- Collaboration
- Authentication/authorization
- Advanced AI editing
- Scene-level AI regeneration
- Whole-outline regeneration after creator edits
- Marketplace
- Payments/subscriptions/quotas
- New queues, Redis, microservices, or infrastructure
- Automatic draft autosave
- Frame-accurate timing

## 20. Dependencies

- Existing Story Creation Project/Story persistence and API conventions.
- Existing `StoryService`, `StoryRepository`, Story domain and Flyway baseline.
- Existing Spring AI 2.0.1 + Google GenAI infrastructure.
- Existing provider-independent AI boundary conventions.
- Approved Gemini model `gemini-2.5-flash` and external configuration.
- Existing Next.js/Tailwind web application and test setup.
- Existing PostgreSQL persistence.
- Existing deterministic CI pattern.

No new Maven/frontend dependency is required by this specification.

## 21. Architectural decisions

- Story Outline is a planning/content layer, not Composition.
- AI remains provider-independent at the application boundary.
- Existing Spring AI/Gemini infrastructure is reused rather than duplicated.
- Outline generation is atomic and non-destructive.
- Existing outline regeneration is explicitly out of scope for V1.
- Scene order is persisted as contiguous `orderIndex` values per Story.
- Scene duration is planned integer seconds; the sum must equal Story target duration, while individual values remain estimates rather than frame timing.
- No new architecture ADR is required at specification time; these choices follow ADR-001, ADR-003, ADR-004, ADR-005, ADR-009, ADR-011, and ADR-012. If implementation introduces a materially different persistence, AI, or navigation architecture, record a new ADR before/alongside that change.

## 22. Open decisions

No blocker remains for implementation at the product-specification level. The implementation phase must still choose exact field-length limits, concrete Java package placement for the new capability contract, and the final minimal navigation route structure while preserving the boundaries above. These are implementation details, not product-scope changes.
