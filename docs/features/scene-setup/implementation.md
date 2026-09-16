# Scene Setup — Implementation

**Feature:** Scene Setup
**Specification:** `docs/specifications/scene-setup.md` (SPEC-005)
**Status:** VALIDATED

## Feature purpose
Scene Setup is the preparation layer between Story Outline/Characters and the future Editor. It stores one Outline Scene's background preset, Story Character selection, scene-local prop instances, ordered manual dialogue and preset action intents. It does not create Composition.

## Acceptance criteria reference
Implements the approved SPEC-005 behavior: one scene at a time, read-only Outline identity/duration, empty setup allowed, controlled background/prop presets, Story Character validation, manual Character/Narrator dialogue, seven fixed actions, persistence, lifecycle protection and Editor placeholder handoff.

## User flow
`Story Outline → Characters → Scene Setup → configure → Save Changes → Open Editor placeholder`

Scene selection uses `?scene=<outlineSceneId>`. Previous/Next moves between existing Outline Scenes without changing their identity.

## End-to-end architecture flow
```text
Next.js Scene Setup UI
  ↓
Scene Setup REST client
  ↓
Spring Boot SceneSetupController
  ↓
SceneSetupService
  ↓
SceneSetupRepository / PostgreSQL
```
No AI, Composition, Timeline or Renderer path is introduced.

## Frontend implementation
`apps/web/app/stories/[storyId]/scene-setup/page.tsx` loads the Story Outline, Story Characters and one persisted Scene Setup. Missing setup is initialized through the approved POST endpoint. The page exposes Background, Characters, Props & Objects, Dialogue, Actions, explicit Save Changes, Previous/Next navigation and Open Editor. Dialogue and action lists support ordering controls.

`apps/web/lib/api.ts` contains typed Scene Setup API models and request functions.

`apps/web/app/stories/[storyId]/editor/page.tsx` is a minimal navigation placeholder only. It explicitly states that no Composition was created.

## Backend implementation
`com.storysprout.api.scene` contains domain records, request contracts, catalog, repository, service, controller and controlled error types.

`SceneSetupService` validates Story/Outline Scene ownership, controlled preset keys, Story Character membership, duplicate Character selection, dialogue speaker rules and fixed action vocabulary. It never writes Outline duration or Composition data.

## Database / persistence
`apps/api/src/main/resources/db/migration/V5__scene_setup.sql` creates `scene_setups`, `scene_setup_characters`, `scene_setup_props`, `scene_setup_dialogue`, and `scene_setup_actions`. Child rows cascade from their setup. Outline Scene deletion explicitly removes the setup before deleting the Outline Scene inside the existing transactional Outline service, without modifying prior migrations.

## Background and prop catalog
`SceneSetupCatalog` supplies deterministic application presets. No generated media URL, upload pipeline, marketplace or AI generation is used. The API validates every key against the catalog.

## Characters
Characters are reused from SPEC-004. Scene Setup never creates or edits Character records. The API accepts only Characters already attached to the Story and rejects duplicate selection.

## Dialogue
Dialogue is manual text with `CHARACTER` or `NARRATOR` speaker types. Character speakers must reference Story Characters; Narrator references must be null. No voice, audio binary or timing is persisted.

## Actions
Only `IDLE`, `TALK`, `WALK`, `RUN`, `WAVE`, `SIT`, `JUMP` are accepted. Each action references a selected Story Character. No timing/keyframe/pose data exists.

## API contracts
- `GET /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/scene-setup`
- `POST /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/scene-setup`
- `PATCH /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/scene-setup/background`
- `PUT /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/scene-setup/characters`
- `POST /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/scene-setup/props`
- `DELETE /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/scene-setup/props/{propId}`
- `PUT /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/scene-setup/dialogue`
- `PUT /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/scene-setup/actions`
- `GET /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/scene-setup/editor-handoff`

## Automated tests
- `SceneSetupServiceTest` — backend validation.
- `SceneSetupControllerIntegrationTest` — PostgreSQL/Testcontainers, Flyway, persistence, invalid references, duplicate setup and Outline deletion cleanup.
- `apps/web/tests/e2e/scene-setup.spec.ts` — full creator path through refresh and Editor placeholder.
- Existing Story Outline service regression updated for setup lifecycle cleanup.

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
| Scene Setup Playwright E2E | PASS |
| Story Creation E2E regression | PASS as part of full E2E suite |
| Story Outline E2E regression | PASS as part of full E2E suite |
| Characters E2E regression | PASS as part of full E2E suite |
| Renderer build/regression | PASS |
| Real Gemini API smoke test | NOT RUN — not applicable; Scene Setup has no AI |

Validation source: GitHub Actions CI run `35101048667`; local execution was not claimed.

## Renderer / Composition boundary
**No renderer changes. No Composition changes.** Scene Setup records contain no transforms, exact timing, keyframes, camera state, audio timing or renderer fields. Open Editor is only a placeholder navigation/handoff.

## Known limitations
- Prop add/remove uses the approved section endpoints and persists immediately; other editable sections use explicit Save Changes.
- Preset visuals are labelled placeholders rather than generated media.
- Full Editor/Timeline implementation is intentionally excluded.

## Future improvements
- Implement the separately specified Editor/Timeline.
- Resolve Scene Setup orphan references explicitly if a Character is removed from Story membership.

## Related decisions
- ADR-001 Composition source of truth.
- ADR-008 preset animation semantics.
- ADR-009 desktop-first web.
- ADR-012 SDD/TDD and feature memory.
- SPEC-005 Scene Setup preparation boundary.

## FILE-BY-FILE IMPLEMENTATION MAP
| File | Responsibility | Interaction |
|---|---|---|
| `apps/api/src/main/resources/db/migration/V5__scene_setup.sql` | Scene Setup schema | Root/child persistence tables and constraints. |
| `apps/api/src/main/java/com/storysprout/api/scene/SceneSetup.java` | Aggregate domain record | Holds one setup and ordered children. |
| `apps/api/src/main/java/com/storysprout/api/scene/SceneSetupCharacter.java` | Character selection model | Stable Story Character reference/order. |
| `apps/api/src/main/java/com/storysprout/api/scene/SceneSetupProp.java` | Prop instance model | Scene-local preset instance/order. |
| `apps/api/src/main/java/com/storysprout/api/scene/SceneSetupDialogue.java` | Dialogue model | Ordered manual speaker/text record. |
| `apps/api/src/main/java/com/storysprout/api/scene/SceneSetupAction.java` | Action intent model | Ordered fixed action reference. |
| `apps/api/src/main/java/com/storysprout/api/scene/SceneSetupRequests.java` | API request contracts | Background, Characters, Props, Dialogue and Actions payloads. |
| `apps/api/src/main/java/com/storysprout/api/scene/SceneSetupCatalog.java` | Controlled preset catalog | Validates background/prop keys and action vocabulary. |
| `apps/api/src/main/java/com/storysprout/api/scene/SceneSetupRepository.java` | JDBC persistence | Root retrieval, child replacement, prop mutation and lifecycle deletion. |
| `apps/api/src/main/java/com/storysprout/api/scene/SceneSetupService.java` | Application rules | Ownership, validation, persistence orchestration and handoff. |
| `apps/api/src/main/java/com/storysprout/api/scene/SceneSetupResponse.java` | API response | Combines setup with read-only Story/Outline context and catalog options. |
| `apps/api/src/main/java/com/storysprout/api/scene/SceneSetupController.java` | REST API | Exposes SPEC-005 endpoints. |
| `apps/api/src/main/java/com/storysprout/api/scene/SceneSetupNotFoundException.java` | 404 error | Controlled missing-resource mapping. |
| `apps/api/src/main/java/com/storysprout/api/scene/SceneSetupConflictException.java` | 409 error | Controlled relationship/lifecycle conflicts. |
| `apps/api/src/main/java/com/storysprout/api/story/StoryOutlineService.java` | Cross-feature lifecycle | Deletes Scene Setup before deleting an Outline Scene in the same transaction. |
| `apps/api/src/main/java/com/storysprout/api/story/ApiExceptionHandler.java` | Shared API errors | Maps Scene Setup 404/409. |
| `apps/api/src/test/java/com/storysprout/api/scene/SceneSetupServiceTest.java` | Unit tests | Service validation/rules. |
| `apps/api/src/test/java/com/storysprout/api/scene/SceneSetupControllerIntegrationTest.java` | Integration tests | PostgreSQL/Flyway/API persistence behavior. |
| `apps/api/src/test/java/com/storysprout/api/outline/StoryOutlineServiceTest.java` | Regression test | Proves setup cleanup during Outline deletion. |
| `apps/web/lib/api.ts` | Frontend API client | Typed Scene Setup operations. |
| `apps/web/app/stories/[storyId]/scene-setup/page.tsx` | Scene Setup UI | One-scene configuration, ordering, save and navigation. |
| `apps/web/app/stories/[storyId]/editor/page.tsx` | Editor placeholder | Navigation only; creates no Composition. |
| `apps/web/tests/e2e/scene-setup.spec.ts` | E2E | Full creator path and refresh persistence. |
| `docs/specifications/scene-setup.md` | Specification | SPEC-005 product boundary. |
| `docs/features/scene-setup/implementation.md` | Feature memory | End-to-end implementation record. |
| `docs/features/FEATURE_INDEX.md` | Feature index | Scene Setup status tracking. |
| `docs/project/current-state.md` | Project memory | Current feature/validation status. |
