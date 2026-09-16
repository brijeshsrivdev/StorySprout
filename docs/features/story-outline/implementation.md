# Story Outline — Implementation

**Feature:** Story Outline  
**Specification:** `docs/specifications/story-outline.md` (SPEC-003)  
**Status:** VALIDATED

## Feature purpose

Story Outline turns a persisted Story into an ordered, editable narrative planning layer before future Scene Setup work. It persists Outline Scene records containing title, summary, order and planned duration. It displays Story target duration, total planned duration and variance without forcing equality.

## Specification and acceptance criteria

The implementation follows SPEC-003 and its approved V1 duration decision. Key rules are:
- Story duration is a target of 60, 180 or 300 seconds.
- Outline scene durations are positive integer planning estimates.
- Planned total may be under, approximately on, or over target.
- UI exposes target, planned total and variance.
- Add, delete and reorder never silently alter unrelated scene durations.
- Exact timing belongs to future Editor/Timeline/Composition work.

## User flow

`Dashboard → Story → Story Outline → Scene Setup placeholder`

Creation flow now continues directly from Story Creation into `/stories/{storyId}/outline`. Existing project cards can open persisted stories through the project-story listing endpoint.

## End-to-end architecture flow

```text
Next.js Story Outline UI
        ↓
Story Outline API client
        ↓
Spring Boot StoryOutlineController
        ↓
StoryOutlineService
   ↙              ↘
PostgreSQL       StoryOutlineGenerator
                      ↓
              Fake or Gemini adapter
                      ↓
                Spring AI ChatClient
                      ↓
                    Gemini
```

The outline remains outside `packages/editor-model` and never creates Composition JSON.

## Frontend implementation

`apps/web/app/stories/[storyId]/outline/page.tsx` provides:
- Story title/setup metadata and reference draft/idea.
- Empty, loading and error states.
- Generate Outline.
- Add/edit/delete scenes.
- Move-up/move-down reorder controls.
- Save Changes.
- Target / Planned / Variance metrics.
- Under-target, approximately-on-target and over-target visual states with text labels.
- Field-level validation and save blocking for invalid title/summary/duration.
- Delete confirmation.
- Unsaved state handling.
- Continue to a Scene Setup placeholder only.

`apps/web/lib/api.ts` contains provider-neutral REST client functions for Story Outline.

`apps/web/lib/outline.ts` owns duration formatting, planned-total calculation, variance calculation and the V1 approximately-on-target threshold.

`apps/web/lib/outline.test.ts` covers under-target, approximately-on-target, over-target and duration formatting behavior.

## Backend implementation

The `com.storysprout.api.outline` package contains the planning domain, service, persistence repository, REST DTOs and error types.

`StoryOutlineService`:
- validates Story existence;
- validates scene title/summary bounds;
- validates positive integer duration;
- calculates target/planned/variance;
- prevents regeneration when an outline already exists;
- validates complete AI output before persistence;
- assigns canonical scene UUIDs and order positions;
- performs creator scene CRUD and reorder operations transactionally.

`StoryOutlineController` exposes the SPEC-003 REST surface plus the minimal project-story listing required for Dashboard navigation.

`StoryResponse.continuationPath` now points to the Story Outline route.

## Database / persistence

New migration:

`apps/api/src/main/resources/db/migration/V3__story_outline.sql`

Creates `story_outline_scenes` with:
- UUID primary key;
- Story foreign key;
- positive contiguous order managed by application operations;
- nonblank bounded title and summary;
- positive integer `duration_seconds`;
- timestamps;
- unique `(story_id, order_index)`;
- ordered Story index.

Existing migrations were not modified.

`OutlineSceneRepository` persists ordered scenes and performs atomic delete compaction and collision-safe reorder using valid temporary positive positions to respect the database's positive-order constraint.

## AI / external services

Application boundary:

```text
StoryOutlineGenerator.generate(StoryOutlineGenerationRequest)
    -> StoryOutlineGenerationResult
```

Normal CI uses `DeterministicStoryOutlineGenerator` and does not need credentials or network access.

`GeminiStoryOutlineGenerator` reuses the existing Spring AI `ChatClient`, `GeminiAiCallExecutor` and `GeminiAiProperties`. The approved `gemini-2.5-flash` model remains configuration-driven.

The Gemini adapter uses typed provider-native structured output and validates scene title, summary and positive integer duration. It does not require exact duration equality.

No new Maven dependency was introduced.

## Renderer impact

None. The renderer does not consume Story Outline data in this slice. No Composition, TimelineClip, SceneObject, animation, camera, asset or render-job data is created.

## API contracts

- `GET /api/v1/projects/{projectId}/stories`
- `GET /api/v1/projects/{projectId}/stories/{storyId}`
- `GET /api/v1/stories/{storyId}/outline-scenes`
- `POST /api/v1/stories/{storyId}/outline/generate`
- `POST /api/v1/stories/{storyId}/outline-scenes`
- `PATCH /api/v1/stories/{storyId}/outline-scenes/{sceneId}`
- `DELETE /api/v1/stories/{storyId}/outline-scenes/{sceneId}`
- `PATCH /api/v1/stories/{storyId}/outline-scenes/reorder`

Existing API success/error envelope remains authoritative.

## Data model

### OutlineScene

- `id`
- `storyId`
- `orderIndex`
- `title`
- `summary`
- `durationSeconds`
- `createdAt`
- `updatedAt`

### Outline response

Includes:
- Story reference;
- ordered scenes;
- `targetDurationSeconds`;
- `plannedDurationSeconds`;
- `varianceSeconds`.

## Error handling

- 400 for invalid creator scene fields or reorder payloads.
- 404 for unknown/mismatched Story or Scene.
- 409 when generation is requested for an existing outline.
- 422 for controlled AI generation failure/malformed AI result.
- 500 for unexpected errors.

Existing persisted outline content is not replaced by a failed generation request.

## Security considerations

- No authentication/authorization added.
- Story and outline content is treated as untrusted creator input.
- Input lengths are bounded before persistence.
- AI provider credentials remain external configuration.
- Full prompts/generated content are not logged by the Story Outline adapter.

## Automated tests

### Backend
- `StoryOutlineServiceTest` covers variance, add preservation, delete preservation, reorder behavior, positive-duration validation and non-equal AI totals.
- `StoryOutlineControllerIntegrationTest` uses PostgreSQL Testcontainers and covers generation, CRUD, variance persistence, reorder duration preservation, invalid durations and regeneration conflict.

### Frontend
- `apps/web/lib/outline.test.ts` covers duration planning/variance states.
- Existing Story Creation frontend tests continue to pass.

### E2E
- `apps/web/tests/e2e/story-outline.spec.ts` covers creation → outline → add/edit/save/refresh and generation → reorder → duration preservation.
- Existing Story Creation E2E was updated for the approved Story → Outline continuation and passes.

### Regression
- Renderer build/regression passes unchanged.
- Full CI Web/API/Renderer/E2E validation passed in run `35085193451`.

## Validation result

| Validation | Status |
|---|---|
| Frontend lint | PASS |
| Frontend typecheck | PASS |
| Frontend unit tests | PASS |
| Frontend build | PASS |
| Backend compile | PASS |
| Backend tests | PASS |
| PostgreSQL/Testcontainers integration | PASS |
| Flyway/database validation | PASS |
| Story Outline Playwright E2E | PASS |
| Existing Story Creation E2E regression | PASS |
| Renderer build/regression | PASS |
| Real Gemini API smoke test | NOT RUN — intentionally excluded from normal CI |

## Known limitations

- The UI Save Changes action coordinates multiple existing CRUD endpoints; there is no separate Save Outline API by design.
- Add/delete/reorder are local edits until Save Changes is pressed.
- Approximately-on-target is defined as variance within ±10 seconds for V1 presentation.
- Scene Setup is intentionally a navigation placeholder only.
- Real Gemini smoke testing remains outside normal CI and requires runtime credentials.

## Future improvements

- Dedicated Scene Setup implementation.
- More sophisticated outline editing UX only if separately specified.
- Future Editor/Timeline will own exact animation timing and Composition semantics.
- Real-provider smoke test workflow may be added separately with secure runtime credentials.

## Related decisions

- Existing provider-independent AI boundary decision.
- Existing Spring AI/Gemini infrastructure decision.
- SPEC-003 approved duration decision: Story duration is a target and Outline durations are planning estimates.

## FILE-BY-FILE IMPLEMENTATION MAP

| File | Responsibility | Important behavior / interaction |
|---|---|---|
| `apps/api/src/main/resources/db/migration/V3__story_outline.sql` | Story Outline schema | Creates outline scene table, constraints and ordered index. |
| `apps/api/src/main/java/com/storysprout/api/outline/OutlineScene.java` | Domain record | Planning-only scene representation. |
| `apps/api/src/main/java/com/storysprout/api/outline/OutlineSceneRepository.java` | JDBC persistence | Ordered load, create/update/delete compaction and collision-safe reorder. |
| `apps/api/src/main/java/com/storysprout/api/outline/StoryOutlineService.java` | Application service | Story ownership, validation, duration variance, generation and scene operations. |
| `apps/api/src/main/java/com/storysprout/api/outline/StoryOutlineController.java` | REST API | Exposes Story and Outline endpoints. |
| `apps/api/src/main/java/com/storysprout/api/outline/StoryOutlineGenerator.java` | AI capability boundary | Keeps application code provider-independent. |
| `apps/api/src/main/java/com/storysprout/api/outline/DeterministicStoryOutlineGenerator.java` | CI/local AI fake | Stable credential-free outline generation. |
| `apps/api/src/main/java/com/storysprout/api/outline/StoryOutlineGenerationRequest.java` | AI request contract | Carries Story idea/draft and setup context. |
| `apps/api/src/main/java/com/storysprout/api/outline/StoryOutlineGenerationResult.java` | AI result contract | Contains ordered generated scenes. |
| `apps/api/src/main/java/com/storysprout/api/outline/GeneratedOutlineScene.java` | AI scene DTO | Provider-neutral title/summary/duration result. |
| `apps/api/src/main/java/com/storysprout/api/outline/OutlineSnapshot.java` | Service read model | Combines Story, scenes and duration metrics. |
| `apps/api/src/main/java/com/storysprout/api/outline/OutlineResponse.java` | REST response | Serializes Story Outline and duration metrics. |
| `apps/api/src/main/java/com/storysprout/api/outline/OutlineSceneResponse.java` | REST scene response | Serializes persisted planning scene. |
| `apps/api/src/main/java/com/storysprout/api/outline/StoryOutlineStoryResponse.java` | REST Story reference | Prevents API coupling to internal Story response routing. |
| `apps/api/src/main/java/com/storysprout/api/outline/CreateOutlineSceneRequest.java` | Create payload | Creator scene fields. |
| `apps/api/src/main/java/com/storysprout/api/outline/UpdateOutlineSceneRequest.java` | Update payload | Editable title/summary/planned duration. |
| `apps/api/src/main/java/com/storysprout/api/outline/ReorderOutlineScenesRequest.java` | Reorder payload | Complete ordered scene ID set. |
| `apps/api/src/main/java/com/storysprout/api/outline/OutlineNotFoundException.java` | 404 error | Controlled Story/scene not-found handling. |
| `apps/api/src/main/java/com/storysprout/api/outline/OutlineConflictException.java` | 409 error | Blocks destructive regeneration. |
| `apps/api/src/main/java/com/storysprout/api/outline/OutlineGenerationException.java` | 422 error | Controlled AI generation failures. |
| `apps/api/src/main/java/com/storysprout/api/story/StoryRepository.java` | Story lookup extension | Adds project Story listing used for Dashboard navigation. |
| `apps/api/src/main/java/com/storysprout/api/story/StoryResponse.java` | Story navigation response | Routes persisted Story to Story Outline. |
| `apps/api/src/main/java/com/storysprout/api/story/ApiExceptionHandler.java` | API error mapping | Adds Story Outline 404/409/422 mappings. |
| `apps/api/src/main/java/com/storysprout/api/ai/infrastructure/google/GeminiStoryOutlinePromptBuilder.java` | Gemini prompt | Includes Story context and approximate duration instructions. |
| `apps/api/src/main/java/com/storysprout/api/ai/infrastructure/google/GeminiStoryOutlineResponse.java` | Gemini DTO | Infrastructure-only structured response type. |
| `apps/api/src/main/java/com/storysprout/api/ai/infrastructure/google/GeminiStoryOutlineGenerator.java` | Gemini adapter | Reuses ChatClient/executor/config and validates positive durations. |
| `apps/web/lib/api.ts` | Frontend API client | Story Outline REST calls and response types. |
| `apps/web/lib/outline.ts` | Duration presentation logic | Planned total, variance, formatting and approximate-state calculation. |
| `apps/web/lib/outline.test.ts` | Frontend unit tests | Duration/variance behavior. |
| `apps/web/app/page.tsx` | Dashboard navigation | Lists project Stories and links to their Outline. |
| `apps/web/app/create/setup/page.tsx` | Creation continuation | Sends newly created Story directly to its Outline. |
| `apps/web/app/stories/[storyId]/outline/page.tsx` | Story Outline UI | Full V1 planning editor and duration visibility. |
| `apps/web/app/stories/[storyId]/scene-setup/page.tsx` | Navigation placeholder | Future Scene Setup destination only; no Scene Setup feature implementation. |
| `apps/web/tests/e2e/story-outline.spec.ts` | Playwright E2E | End-to-end Story → Outline, persistence, variance and reorder coverage. |
| `apps/web/tests/e2e/story-creation.spec.ts` | Story Creation regression | Verifies approved Story → Outline continuation remains compatible with existing Story Creation flow. |
| `apps/api/src/test/java/com/storysprout/api/outline/StoryOutlineServiceTest.java` | Backend unit tests | Domain/service duration and mutation rules. |
| `apps/api/src/test/java/com/storysprout/api/outline/StoryOutlineControllerIntegrationTest.java` | Backend integration tests | PostgreSQL-backed API/persistence coverage. |
