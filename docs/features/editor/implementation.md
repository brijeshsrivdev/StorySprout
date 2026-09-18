# Editor — Implementation

**Feature:** Editor  
**Specification:** `docs/specifications/editor.md` — SPEC-006  
**Clarification:** `docs/specifications/editor-clarifications.md`  
**Decision:** ADR-014  
**Status:** VALIDATED — functional implementation complete; visual/UX polish milestone completed on `feature/storysprout-ui-foundation` (new UI validation pending)

## Feature purpose

The Editor is the first visual authoring layer after Scene Setup. It initializes one Outline Scene into a canonical Composition, lets the creator edit basic visual state, and explicitly saves that Composition for future Timeline/Renderer consumption.

## Visual/UX polish milestone

The existing Editor remains the functional SPEC-006 implementation. This milestone changes presentation only: compact creative-workstation shell, creator-oriented asset toolkit, dominant 1920 × 1080 stage, contextual inspector grouping, stronger selection/empty states, deliberate Timeline boundary styling, and clearer save/conflict feedback. No Composition, API, persistence, renderer or domain behavior changed.

## Acceptance criteria reference

All 31 SPEC-006 acceptance criteria remain authoritative. Implementation coverage is provided by Composition-model tests, backend service tests, the Editor UI, API persistence, and the Scene Setup → Editor Playwright flow. Final pass/fail status is recorded after CI validation.

## User flow

`Story → Story Outline → Characters → Scene Setup → Open Editor → initialize/load Composition → edit → Save → reload`

Editor identity is `storyId + outlineSceneId`.

## End-to-end architecture flow

```text
Next.js Editor UI
      ↓
Editor API client
      ↓
Spring Boot EditorController
      ↓
EditorService
   ↙        ↘
Scene Setup  CompositionRepository
 context          ↓
             PostgreSQL JSONB
      ↓
Composition schema 1.1
      ↓
Future Timeline / Renderer
```

Scene Setup is read for first initialization only. After a Composition exists, Composition JSON is authoritative.

## Composition schema 1.1 / migration

`packages/editor-model/src/index.ts` now defines Composition schema `1.1` and adds canonical `SceneObject.objectType` with `CHARACTER | PROP`.

`assetId` remains a visual reference and is never used to infer semantic type.

`migrateCompositionV1ToV11()` provides explicit legacy compatibility. A 1.0 document can be migrated only when the caller supplies an explicit object-id → semantic-type mapping for every object. Missing mappings fail. No asset/name/URL/ID inference is performed. New Editor-created documents are always 1.1.

## Frontend implementation

`apps/web/app/stories/[storyId]/editor/page.tsx` implements the desktop-first editor:
- fixed 1920×1080 logical stage;
- selection and accessible visibility controls;
- pointer-based move converted into logical coordinates;
- uniform scale from 0.25 through 3.0;
- Character/Prop add operations from Story membership and controlled presets;
- delete, visibility and four layer-order operations;
- X/Y/Scale properties and reset transforms;
- controlled background selection/removal;
- explicit Save, dirty state and conflict reload;
- Delete/Backspace and Escape keyboard handling;
- non-functional Timeline placeholder;
- browser unsaved-navigation warning.

## Backend implementation

`com.storysprout.api.editor` owns Editor context, initialization, validation, persistence and optimistic save behavior.

Initialization reads Story, Outline Scene, Scene Setup, Story Characters and controlled presets. It creates one Composition scene, explicit object types, deterministic placeholder visual references, zero Timeline clips, and the Outline planned duration as the initial scene/composition duration basis.

Existing Composition is returned without reconstruction. Save validates schema, project/scene linkage, fixed stage, object IDs/types/transforms, controlled references and empty Timeline before an optimistic versioned update.

## Database / persistence

`V6__editor_composition.sql` creates a single canonical `compositions` JSONB root table with Project/Outline Scene ownership, schema version, version, timestamps and unique `(project_id, outline_scene_id)`. The Outline Scene FK uses `ON DELETE CASCADE` so deleting an Outline Scene removes its subordinate Composition atomically.

No normalized Composition object/layer/timeline tables were introduced. No autosave exists.

## API contracts

- `GET /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/editor`
- `POST /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/editor/initialize`
- `GET /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/composition`
- `PUT /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/composition`

All use the existing API success/error envelope. Save conflicts are HTTP 409; invalid Composition/domain/reference content is HTTP 422; missing context is HTTP 404.

## Source-of-truth boundaries

`Scene Setup → initialize once → Composition → Editor edits → Composition authoritative`.

Scene Setup dialogue and action intent remain Scene Setup data. They are not copied to Composition, TimelineClip, audio state, animation state, or hidden metadata. Scene Setup changes after initialization do not silently change Composition.

## Renderer impact

Renderer implementation is unchanged. Editor produces versioned Composition 1.1 JSON with explicit object types, fixed stage dimensions and an empty Timeline. Future renderer work must consume the approved 1.1 contract.

## Testing

- `apps/web/tests/editor-model.test.ts` — schema 1.1, fixed stage, explicit 1.0 migration and no type inference.
- `apps/api/src/test/java/com/storysprout/api/editor/EditorServiceTest.java` — initialization typing, existing reopen, empty Timeline and stale-save conflict.
- `apps/web/tests/e2e/scene-setup.spec.ts` — full Story → Outline → Characters → Scene Setup → Editor → move → scale → Save → reload flow, plus Editor workstation/keyboard UI assertions.
- Existing Story Creation, Story Outline, Characters and Scene Setup suites remain as regression coverage in the repository CI workflow.

## Validation for this UI milestone

Local frontend execution was not available because the environment could not reach repository/build services. No new lint, typecheck, build or Playwright result is claimed for this visual milestone. The previous functional Editor CI validation remains the basis for unchanged model/backend behavior.

## Known limitations / deviations

- Local Maven/Node/Docker execution is unavailable in this environment; final validation is delegated to GitHub Actions.
- Character/Prop rendering currently uses deterministic UI placeholders because final media assets are not yet guaranteed persisted records. No fake generated image URL is stored.
- The 1.0 → 1.1 compatibility utility requires explicit semantic type mapping because legacy 1.0 has insufficient type information. Automatic inference is intentionally prohibited.
- Current placeholder/preset visual references use controlled reference strings, while semantic type always comes from explicit `objectType`.

## Future improvements

Timeline, animation, dialogue/audio timing, real media assets, undo/redo, autosave and richer canvas behavior require separately specified features.

## Related decisions

ADR-001, ADR-002, ADR-005, ADR-006, ADR-007, ADR-009, ADR-011, ADR-012 and ADR-014.

## FILE-BY-FILE IMPLEMENTATION MAP

| File | Responsibility | Important behavior / interactions |
|---|---|---|
| `packages/editor-model/src/index.ts` | Canonical Composition contract | Schema 1.1, explicit object type, fixed-stage constants and explicit legacy migration. |
| `apps/web/lib/editor.ts` | Editor API client | Typed context/composition contracts and explicit save/error handling. |
| `apps/web/app/stories/[storyId]/editor/page.tsx` | Editor UI | Visual composition editing, logical transforms, layers, background, Save/conflict UX. |
| `apps/api/src/main/java/com/storysprout/api/editor/Composition.java` | Persistence read model | Composition root and optimistic version. |
| `apps/api/src/main/java/com/storysprout/api/editor/CompositionRepository.java` | JDBC persistence | JSONB load/create/compare-and-update. |
| `apps/api/src/main/java/com/storysprout/api/editor/EditorContext.java` | API context | Story/scene/setup/source options plus Composition. |
| `apps/api/src/main/java/com/storysprout/api/editor/EditorController.java` | REST API | Context, initialize, get and save endpoints. |
| `apps/api/src/main/java/com/storysprout/api/editor/EditorService.java` | Application service | Initialization, validation, source-of-truth boundaries and optimistic save. |
| `apps/api/src/main/java/com/storysprout/api/editor/SaveCompositionRequest.java` | Save DTO | Expected version plus complete Composition. |
| `apps/api/src/main/java/com/storysprout/api/editor/EditorNotFoundException.java` | 404 error | Missing Story/scene/setup/Composition context. |
| `apps/api/src/main/java/com/storysprout/api/editor/EditorConflictException.java` | 409 error | Stale optimistic version. |
| `apps/api/src/main/java/com/storysprout/api/editor/EditorValidationException.java` | 422 error | Invalid Composition/reference/domain content. |
| `apps/api/src/main/java/com/storysprout/api/story/ApiExceptionHandler.java` | Shared API mapping | Editor 404/409/422 mappings. |
| `apps/api/src/main/resources/db/migration/V6__editor_composition.sql` | Persistence schema | Canonical JSONB root, version and Outline Scene cascade. |
| `apps/api/src/test/java/com/storysprout/api/editor/EditorServiceTest.java` | Backend tests | Initialization, semantic types, reopen and conflict behavior. |
| `apps/web/tests/editor-model.test.ts` | Model regression | Schema and migration compatibility behavior. |
| `apps/web/tests/e2e/scene-setup.spec.ts` | E2E | Full creator path into persisted Editor state. |
| `docs/specifications/editor.md` | Specification | 31 acceptance criteria and V1 boundaries. |
| `docs/specifications/editor-clarifications.md` | Clarification | ADR-014 object identity, migration, boundaries and coordinate semantics. |
| `docs/project/decisions.md` | ADR record | ADR-014 explicit Character/Prop semantic identity. |
| `docs/api/api-conventions.md` | API contract | Shared response/error envelope. |
| `docs/features/FEATURE_INDEX.md` | Feature index | Editor implementation status. |
| `docs/project/current-state.md` | Project memory | Editor implementation/validation state. |
