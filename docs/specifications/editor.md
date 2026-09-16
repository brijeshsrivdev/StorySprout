# Feature Specification — Editor

**Specification ID:** SPEC-006  
**Status:** SPECIFIED — implementation not started

## 1. Purpose

The Editor is the first visual authoring layer after Story Outline, Characters, and Scene Setup. It turns one prepared Outline Scene into an editable visual Composition and gives a creator a deliberately small animation-authoring workspace.

The Editor is **not** a professional animation package and is not the Timeline feature. It owns the editable visual composition after initialization. The canonical flow is:

```text
Story → Story Outline → Characters → Scene Setup → Editor → Composition JSON → Timeline → Preview → Render
```

The governing lifecycle is:

```text
Scene Setup
    ↓
initial editor state
    ↓
Composition
    ↓
creator edits
    ↓
Composition is authoritative
    ↓
future Timeline / Renderer
```

After initial handoff, the Editor must not continuously reconstruct its state from Scene Setup.

## 2. Actual current-main inspection

This specification was prepared against the actual merged `main` branch. The repository confirms Story Creation, AI Provider Integration, Story Outline, Characters, and Scene Setup are validated. The current-state document says visual Editor/Timeline/Composition persistence/editing are not implemented, and Scene Setup still hands off only to an Editor placeholder.

### Existing Composition Model

`packages/editor-model/src/index.ts` currently defines:

```ts
export const COMPOSITION_SCHEMA_VERSION = "1.0" as const;

export interface Composition {
  schemaVersion: typeof COMPOSITION_SCHEMA_VERSION;
  projectId: string;
  width: number;
  height: number;
  fps: number;
  durationMs: number;
  scenes: Scene[];
}

export interface Scene {
  id: string;
  name: string;
  durationMs: number;
  background?: SceneBackground;
  objects: SceneObject[];
  timeline: TimelineClip[];
}

export interface SceneBackground { assetId: string; }

export interface SceneObject {
  id: string;
  assetId: string;
  x: number;
  y: number;
  scale: number;
  rotation: number;
  visible: boolean;
}

export interface TimelineClip {
  id: string;
  kind: "OBJECT" | "DIALOGUE" | "AUDIO" | "MUSIC" | "SFX";
  startMs: number;
  durationMs: number;
  sourceId?: string;
}
```

`createEmptyComposition(projectId)` currently creates schema `1.0`, `1920 × 1080`, `30` fps, `durationMs: 0`, and no scenes.

### Findings

The existing model already provides the minimum fields required for basic V1 visual editing:

- `Composition.schemaVersion` — explicit compatibility boundary.
- `Composition.projectId` — project ownership.
- `Composition.width/height/fps/durationMs` — composition-level settings.
- `Scene.id/name` — Composition scene identity and name.
- `Scene.background.assetId` — scene-level background reference.
- `Scene.objects[]` — ordered visual objects/layers.
- `Scene.timeline[]` — future timing structure.
- `SceneObject.id` — stable editable object identity.
- `SceneObject.assetId` — visual asset reference.
- `SceneObject.x/y` — position.
- `SceneObject.scale` — uniform scale.
- `SceneObject.rotation` — representable rotation.
- `SceneObject.visible` — visibility.
- `TimelineClip` already represents future object/dialogue/audio/music/SFX timing, but SPEC-006 does not create or edit clips.

### Schema decision

**No Composition schema change is required for basic V1 Editor behavior.** The existing model is sufficient for background, movable/scalable objects, visibility, rotation representation, ordering, and future timing compatibility.

SPEC-006 therefore does not change `packages/editor-model`, `createEmptyComposition()`, or add an `objectType` field. Character-versus-Prop context is supplied by the source-object mapping/editor presentation rather than by expanding canonical schema `1.0`.

A future requirement for anchors, dimensions, richer object semantics, animation state, or other canonical data must be separately specified and must include an explicit Composition compatibility decision if the model changes.

## 3. Existing feature boundaries

### Story Outline

Story Outline remains the narrative planning layer. Its planned duration is an initialization input only; exact timing belongs to future Editor/Timeline behavior.

### Characters

Characters remain the reusable project library and Story membership source. Editor does not duplicate Character CRUD.

### Scene Setup

Scene Setup is the preparation layer. It owns one Outline Scene's controlled background, Story Character selection, scene-local props, manual dialogue, and fixed action intents. It stores no transforms, exact timing, keyframes, camera state, audio timing, Composition data, or AI-generated content.

The current Scene Setup Editor link is a placeholder that explicitly does not create Composition. SPEC-006 defines the replacement initialization contract; implementation remains deferred.

## 4. Editor entry and scene identity

The Editor operates on **one Outline Scene at a time**.

Identity is:

```text
storyId + outlineSceneId
```

The existing Scene Setup continuation path supplies the stable Outline Scene ID. No separate editor-scene identity is introduced before Composition exists.

### Opening behavior

1. Verify Story exists.
2. Verify Outline Scene belongs to the Story.
3. Load Scene Setup for that Outline Scene.
4. Determine whether a Composition exists for the Story/Outline Scene.
5. If none exists, initialize it once from Scene Setup and persist it before presenting the editable state.
6. If one exists, load that Composition and do not reconstruct it from Scene Setup.

### Empty Scene Setup

Empty Scene Setup is valid. Opening Editor must therefore produce a valid Composition containing the scene identity/name and no background/objects when none were prepared. The creator can then add available Characters/Props.

### Existing Composition

Existing Composition is authoritative on reopen. It wins over current Scene Setup for visual state, including objects, transforms, visibility, ordering, and background.

## 5. Initial Composition creation

Initialization occurs **once per Outline Scene**, only when no Composition exists.

| Scene Setup data | Initial Composition treatment |
|---|---|
| Outline Scene ID | Stable linkage to the Composition Scene/root record |
| Outline Scene title | `Scene.name` |
| Outline planned duration | Initial `Scene.durationMs`; composition duration basis for this scene, while exact timing remains future Timeline-owned |
| Background preset | `Scene.background.assetId` when selected |
| Selected Story Characters | One `SceneObject` per selected Character |
| Scene-local Prop instances | One `SceneObject` per persisted Prop instance |
| Dialogue lines | Not converted to `TimelineClip`; retained as Scene Setup preparation data for future Dialogue/Audio/Timeline work |
| Action intents | Not converted to animation/timeline clips; retained as preparation intent for future animation behavior |

### Character/Prop object creation

Each initialized Character or Prop creates a Composition `SceneObject` with:

- new stable Composition object UUID;
- source visual `assetId` when one exists;
- deterministic initial `x`, `y`, and `scale`;
- `rotation: 0`;
- `visible: true`.

Character/Prop records currently use deterministic placeholders rather than guaranteed final image assets. Editor must render an approved placeholder when a final visual asset is unavailable and must not invent a fake generated image URL.

Initial placement must be deterministic and should avoid obvious overlap where practical. Exact default coordinates are implementation detail, not a new model field.

### Identity semantics

- Outline Scene ID remains the stable preparation-to-Composition linkage.
- Composition `SceneObject.id` is the canonical editable object identity.
- Character ID / Prop instance ID is source context used during initialization and UI mapping; it is not added to Composition schema `1.0`.

The initializer must ensure one source item produces one Composition object during first initialization. Reopen always loads existing Composition and never creates another object.

Initialization validates the complete result and persists atomically; no partial Composition may become canonical state.

## 6. Source of truth

The authoritative lifecycle is:

```text
Scene Setup
    ↓
initialization
    ↓
Composition
    ↓
Editor edits
    ↓
Composition is authoritative
```

After initialization:

- Editor state is derived from Composition.
- Save writes Composition.
- Reopen reads Composition.
- Scene Setup is not a live synchronization source.
- Scene Setup changes do not silently mutate Composition.

This directly applies ADR-001 and the existing editor-composition architecture.

## 7. Canvas

### Stage

V1 uses a fixed **16:9** stage matching the foundation's `1920 × 1080` Composition dimensions.

Canonical transforms use logical 1920×1080 stage coordinates, not DOM pixels. The browser may scale the stage to fit the viewport, but viewport resizing never rewrites Composition coordinates.

Logical ranges for V1:

- `x: 0..1920`
- `y: 0..1080`

Origin is top-left. The object anchor is kept within stage bounds during direct dragging. Because schema `1.0` has no object width/height, full bounding-box containment is deferred; a rendered object may partially extend beyond the stage.

### Supported canvas behavior

- render background;
- render Character and Prop objects;
- select objects;
- show selection boundary/handles;
- drag to move;
- resize/scale;
- toggle visibility;
- delete permitted Composition objects;
- simple layer ordering.

Rotation is represented by the existing model but **not interactively edited in V1**. V1 initialized objects use `rotation: 0`; the Properties panel may display the value read-only. This avoids expanding the first interaction model while preserving the existing field.

## 8. Editor layout

Desktop-first V1:

```text
┌─────────────────────────────────────────────────────────────┐
│ Story / Scene name     Save state       Save      Back      │
├──────────────┬──────────────────────────────┬──────────────┤
│ Scene Objects│                              │ Properties   │
│ / Add        │            Canvas            │              │
│              │          16:9 stage          │              │
│              │                              │              │
├──────────────┴──────────────────────────────┴──────────────┤
│ Timeline — Coming next                                      │
└─────────────────────────────────────────────────────────────┘
```

### Left panel

Object browser containing current Composition Character/Prop objects, selection state, visibility toggles, and `Add Character` / `Add Prop` actions. It is not asset-management CRUD.

### Center

Visual 16:9 stage.

### Right panel

Contextual Properties panel for the selected object or scene background.

### Bottom

A clearly labelled non-functional `Timeline — Coming next` placeholder may be shown. It must not create clips or expose timing controls. It may also be omitted if it adds no usability value.

## 9. Scene objects

V1 editable object types:

- **Character** — a Story Character already belonging to the Story.
- **Prop** — a scene-local controlled Prop preset instance or a newly added controlled preset.

Background remains scene-level `Scene.background`.

Because schema `1.0` has no `objectType`, the Editor maintains source/type context outside the canonical Composition document. No object-type field is added in SPEC-006.

Canonical object fields edited by the Editor:

- `id`
- `assetId`
- `x`
- `y`
- `scale`
- `visible`
- `rotation` preserved but not interactively changed.

### Layer order

`Scene.objects[]` is canonical back-to-front order. First item is backmost; later items render above earlier items.

V1 supports:

- Bring Forward
- Send Backward
- Bring to Front
- Send to Back

These operations only reorder the array.

## 10. Transform model

### Position

Logical 1920×1080 coordinates, top-left origin. `x/y` represent the object's visual center anchor for consistent dragging.

### Scale

Uniform scale uses existing `scale`. V1 valid range is **0.25 through 3.0**, inclusive. Reject/clamp zero, negative, NaN, infinite, or unbounded values at the appropriate UI/API validation boundaries.

### Resize

Selected object exposes a resize handle or equivalent direct-manipulation control. Resize updates `scale` while preserving the anchor.

### Browser resizing

The stage can scale visually; canonical logical coordinates remain unchanged.

### Out-of-bounds

Dragging clamps the anchor to logical stage bounds. Full object containment is not required because the current model has no canonical width/height.

## 11. Properties panel

### Character

Show Character name, role/category when source context is available, `x`, `y`, scale, visibility, layer controls, reset transform, and read-only action-intent summary when available. Do not expose Character CRUD.

### Prop

Show preset/label, `x`, `y`, scale, visibility, layer controls, and reset transform. Do not expose prop catalog CRUD.

### Background

Show current background preset/name and replace/remove controls from the existing controlled Scene Setup background catalog. Changing background in Editor changes `Scene.background` in Composition only; it does not rewrite Scene Setup. No arbitrary image upload or background generation is included.

## 12. Adding and removing objects

### Initial objects

Scene Setup Characters and Prop instances are initialized into Composition once.

### Adding in Editor

V1 allows adding objects directly because empty Scene Setup must remain usable and basic visual authoring needs recovery/addition capability.

Sources are strictly:

- Characters from the Story's existing Character membership.
- Props from the existing controlled Scene Setup prop preset catalog.

Adding an object creates a new Composition `SceneObject` with a new object ID and deterministic default transform. It does not create Character records, Story membership, Prop catalog records, or Scene Setup rows.

For a newly added Prop, the editor object is canonical Composition state; it does not create a persisted Scene Setup Prop instance.

### Removing

Delete removes only the Composition object. It never deletes the reusable Character, Story membership, Scene Setup source row, controlled Prop preset, or project asset.

## 13. Scene Setup synchronization

There is **no silent bidirectional synchronization**.

After Composition exists, Scene Setup changes to Character selection, Props, background, dialogue, or action intent do not automatically modify Composition.

When Editor reopens, the persisted Composition remains authoritative.

### Reinitialization

V1 does not expose automatic reinitialization, merge, or reconciliation. A future `Reinitialize from Scene Setup`/merge workflow must be separately specified and clearly warn that Composition edits may be replaced.

## 14. Save / autosave

V1 uses **explicit Save**, not autosave.

The UI marks dirty state when the working Composition differs from the last successfully persisted version.

Save:

1. Validate complete Composition locally.
2. Send complete Composition plus loaded version to API.
3. Server validates schema, ownership, references, transforms, and feature boundaries.
4. Server atomically persists and increments version.
5. UI reports `Saved` and clears dirty state.

States:

- `Saved`
- `Unsaved changes`
- `Saving…`
- `Save failed`
- `Conflict — reload required`

Leaving with unsaved changes warns the creator where browser/navigation support permits it. No draft/autosave persistence is included.

## 15. Undo / redo

**Deferred from V1.** Do not implement partial history. A future Undo/Redo feature must define command/history semantics separately.

## 16. Keyboard / mouse interaction

V1 desktop interactions:

- click object → select;
- drag selected object → move;
- resize handle → scale;
- Delete/Backspace → delete selected Composition object;
- Escape → deselect;
- layer controls → reorder;
- property changes → update selected Composition object;
- Save → persist canonical Composition.

Controls must have accessible names and must not depend on hover-only discoverability.

## 17. Timeline boundary

SPEC-006 is not Timeline.

The existing `Scene.timeline: TimelineClip[]` remains part of schema `1.0`, but is **empty and inactive** for this feature.

SPEC-006 does not create/edit playheads, clips, timing, clip resizing, exact dialogue timing, animation keyframes, audio timing, music timing, or SFX timing.

The Editor prepares visual Composition state for the future Timeline.

## 18. Animation boundary

Scene Setup action intents remain preparation metadata:

```text
IDLE, TALK, WALK, RUN, WAVE, SIT, JUMP
```

SPEC-006 does not generate, play, or edit animation. Action intents are not converted into `TimelineClip` records because schema `1.0` does not define an approved animation/action clip semantic and animation timing is out of scope.

The Editor may show a read-only action-intent summary but must not imply the intent is already animated.

## 19. Dialogue boundary

Scene Setup dialogue remains preparation data. It is not converted into Timeline clips, voice assets, or audio state.

Editor does not edit dialogue text and does not generate voice. A read-only dialogue summary may be shown as context, but canonical editable dialogue remains Scene Setup-owned until a separately specified Dialogue/Audio/Timeline feature defines its Composition representation.

## 20. API / persistence contract

Base path remains `/api/v1`.

### Get Editor context

`GET /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/editor`

Returns Story/scene identity, Scene Setup context sufficient for initialization, existing Composition when present, and initialization status (`INITIALIZED` or `EXISTING`).

### Initialize Composition

`POST /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/editor/initialize`

Creates Composition only when absent. Repeated initialization is idempotent: existing Composition is returned and no duplicate objects are created.

### Get Composition

`GET /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/composition`

Returns canonical Composition JSON and persisted version.

### Save Composition

`PUT /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/composition`

Request contains `version` and the complete `composition` document.

Server validates Story/Outline Scene ownership, schema version, scene linkage, object references, transforms, and unsupported content before atomic persistence.

### Errors

Use the repository-wide API envelope:

- `400` malformed request / invalid field or transform;
- `404` Story, Outline Scene, or required source reference not found/belonging to requested context;
- `409` optimistic version conflict or actual conflicting create semantics;
- `422` structurally valid JSON that fails domain/reference/feature validation;
- `500` unexpected service/persistence failure.

Repeated initialization should return the existing Composition rather than use 409 for the normal idempotent case.

No Editor-specific response envelope is introduced.

## 21. Database

Composition persistence does not currently exist, so implementation requires a new Flyway migration.

Proposed table:

```text
compositions
-------------
id UUID PRIMARY KEY
project_id UUID NOT NULL REFERENCES projects(id)
outline_scene_id UUID NOT NULL REFERENCES story_outline_scenes(id)
schema_version VARCHAR NOT NULL
composition_json JSONB NOT NULL
version BIGINT NOT NULL
created_at TIMESTAMPTZ NOT NULL
updated_at TIMESTAMPTZ NOT NULL
```

Required rules:

- unique `(project_id, outline_scene_id)`;
- `version >= 1`;
- supported schema-version validation;
- index `project_id`;
- add a dedicated `outline_scene_id` index only if implementation access patterns need scene-only lookup, since the unique composite index begins with `project_id`.

The JSON document is canonical. Do not create normalized composition-object/layer/timeline tables.

Lifecycle:

- Composition is subordinate to its Outline Scene and Project.
- Once implemented, Outline Scene deletion must remove its Composition atomically.
- No independent Composition deletion UI is required.

## 22. Concurrency / versioning

V1 uses **optimistic concurrency with an integer `version`** on the Composition root.

Save must include the last loaded version. If stored version differs, return `409 Conflict` and do not overwrite newer Composition state.

No last-write-wins behavior is allowed.

V1 does not implement multi-user merge. Conflict UX keeps local unsaved state where possible and offers reload/reopen from the latest persisted Composition.

## 23. Validation

The server must not blindly persist arbitrary JSON.

### Composition

- `schemaVersion === "1.0"`;
- `projectId` matches request context;
- supported `width/height` and 16:9 foundation;
- V1-created composition uses `30` fps;
- `durationMs >= 0`;
- unique scene IDs;
- valid scene objects;
- unique object IDs within each scene;
- `Scene.timeline` remains empty for SPEC-006-created/edited state.

### Scene

- Composition scene corresponds to requested Outline Scene;
- background reference resolves to an allowed controlled background when present;
- object sources resolve to allowed Story Characters or controlled Props.

### SceneObject

- ID present and unique;
- asset/source resolves to permitted Character/Prop context;
- `x/y` finite and within logical stage bounds;
- `scale` finite and `0.25..3.0`;
- `rotation` finite and `0` for V1 interactive edits;
- `visible` boolean.

### Missing/deleted references

If a source Character/Prop no longer resolves, the API must not silently substitute another asset. Return a controlled missing-reference state/error so the UI can identify the affected object. Canonical data remains untouched until explicit creator resolution.

Add operations reject cross-Story Characters and Props outside the approved controlled catalog.

## 24. Testing strategy

### Composition / initialization

- **T-COMP-01:** Empty Scene Setup initializes a valid empty Composition Scene.
- **T-COMP-02:** Background maps to `Scene.background.assetId`.
- **T-COMP-03:** Each selected Story Character creates exactly one Composition object.
- **T-COMP-04:** Each Scene Setup Prop instance creates exactly one Composition object.
- **T-COMP-05:** Repeated initialization is idempotent and never duplicates objects.
- **T-COMP-06:** Stable Outline Scene ID links preparation to Composition.
- **T-COMP-07:** Initialization persists atomically.
- **T-COMP-08:** Existing Composition reopens unchanged.
- **T-COMP-09:** Schema `1.0` is accepted; unsupported schema versions are rejected.

### Editor UI

- **T-UI-01:** Editor loads context and Composition.
- **T-UI-02:** Background renders.
- **T-UI-03:** Character/Prop objects render and can be selected.
- **T-UI-04:** Selection indication is visible and accessible.
- **T-UI-05:** Dragging updates logical position.
- **T-UI-06:** Scaling updates `0.25..3.0`.
- **T-UI-07:** Visibility toggle updates state.
- **T-UI-08:** Delete removes only the Composition object.
- **T-UI-09:** Add Character uses Story Character source.
- **T-UI-10:** Add Prop uses controlled preset source.
- **T-UI-11:** Layer operations preserve identity and change only array order.
- **T-UI-12:** Properties panel edits supported values without Character/asset CRUD.
- **T-UI-13:** Empty scene is usable.
- **T-UI-14:** Missing-reference state is visible and does not silently substitute.
- **T-UI-15:** Save states and unsaved-navigation warning behave correctly.
- **T-UI-16:** Timeline placeholder, if present, is clearly non-functional.

### Scene Setup boundary

- **T-BOUNDARY-01:** Initialization reads Scene Setup when Composition does not exist.
- **T-BOUNDARY-02:** Reopen with existing Composition does not reconstruct from Scene Setup.
- **T-BOUNDARY-03:** Character/Prop Scene Setup changes do not automatically mutate Composition.
- **T-BOUNDARY-04:** Background Scene Setup changes do not automatically mutate Composition.
- **T-BOUNDARY-05:** Dialogue/action changes do not create Composition Timeline data.

### API

- **T-API-01:** Valid Editor context returns existing Composition when present.
- **T-API-02:** Initialize creates Composition exactly once.
- **T-API-03:** Repeated initialize is idempotent.
- **T-API-04:** Get Composition returns canonical JSON and version.
- **T-API-05:** Save valid Composition succeeds and increments version.
- **T-API-06:** Stale version returns 409 and preserves newer state.
- **T-API-07:** Invalid schema version returns controlled validation error.
- **T-API-08:** Invalid transform returns controlled validation error.
- **T-API-09:** Cross-Story Character/Prop reference is rejected.
- **T-API-10:** Invalid Outline Scene returns 404.
- **T-API-11:** Malformed Composition is rejected without partial persistence.
- **T-API-12:** Persistence failure does not report false success.

### Database / integration

- **T-DB-01:** Composition persists with Project and Outline Scene ownership.
- **T-DB-02:** Unique `(project_id, outline_scene_id)` prevents duplicate roots.
- **T-DB-03:** Version starts at 1 and increments after successful save.
- **T-DB-04:** Stale save does not overwrite stored JSON.
- **T-DB-05:** Flyway migration creates table and constraints.
- **T-DB-06:** Outline Scene deletion removes subordinate Composition atomically once implemented.
- **T-DB-07:** PostgreSQL/Testcontainers verifies JSON persistence and versioning.

### E2E

- **T-E2E-01:** Story → Outline → Characters → Scene Setup → configure scene → Open Editor → verify initial Composition.
- **T-E2E-02:** Move Character → save → reload → position persists.
- **T-E2E-03:** Resize Character → save → reload → scale persists.
- **T-E2E-04:** Add Prop → save → reload → object persists.
- **T-E2E-05:** Delete object → save → reload → only Composition object is removed.
- **T-E2E-06:** Change layer order → save → reload → order persists.
- **T-E2E-07:** Change Scene Setup after Composition exists → reopen Editor → Composition remains unchanged.
- **T-E2E-08:** Empty Scene Setup → Editor → add Character/Prop → save → reload.
- **T-E2E-09:** Stale-version conflict produces recoverable conflict state.

### Regression

- **T-REG-01:** Story Creation remains green.
- **T-REG-02:** Story Outline remains green.
- **T-REG-03:** Characters remains green.
- **T-REG-04:** Scene Setup remains green.
- **T-REG-05:** Renderer build/health remains green and its input semantics remain Composition JSON.

## 25. Acceptance criteria

### Entry / initialization

- **AC-01:** Valid Outline Scene opens Editor using `storyId + outlineSceneId`.
- **AC-02:** Empty Scene Setup creates a valid empty Composition Scene.
- **AC-03:** Background maps to scene-level Composition background.
- **AC-04:** Selected Story Characters map to one Composition object each.
- **AC-05:** Scene Setup Prop instances map to one Composition object each.
- **AC-06:** Initialization occurs only when Composition is absent.
- **AC-07:** Reopen never duplicates objects.
- **AC-08:** Outline Scene identity remains stable.

### Canvas / editing

- **AC-09:** Editor renders fixed 16:9 stage.
- **AC-10:** Character/Prop can be selected.
- **AC-11:** Dragging changes logical `x/y`.
- **AC-12:** Resizing changes `scale` within `0.25..3.0`.
- **AC-13:** Visibility can be toggled.
- **AC-14:** Delete removes only Composition object.
- **AC-15:** Available Story Characters and controlled Props can be added without creating library records.
- **AC-16:** Simple layer ordering works through array reorder.
- **AC-17:** Properties panel exposes supported object properties without duplicating CRUD.
- **AC-18:** Background remains a scene-level element.

### Source of truth / synchronization

- **AC-19:** After initialization, Editor loads from Composition rather than reconstructing from Scene Setup.
- **AC-20:** Scene Setup changes do not silently overwrite existing Composition.
- **AC-21:** No bidirectional synchronization exists.
- **AC-22:** No automatic reinitialization/merge exists.

### Save / persistence

- **AC-23:** Explicit Save persists complete Composition.
- **AC-24:** UI distinguishes saved, dirty, saving, failure, and conflict states.
- **AC-25:** Reload restores saved transforms, visibility, ordering, background, and objects.
- **AC-26:** Stale version save returns 409 without overwriting newer state.
- **AC-27:** Invalid Composition is rejected without partial persistence.

### Boundaries

- **AC-28:** No Timeline clips are created/edited.
- **AC-29:** No animation playback/generation is implemented.
- **AC-30:** Dialogue is not converted to audio/timeline state.
- **AC-31:** Composition schema remains `1.0` in the implementation of this specification.

## 26. V1 usability target

A non-technical creator should understand what scene is open, what objects are present, what is selected, how to move/resize/hide/delete/add, how to change simple layer order, how to save, and that Timeline/animation come later.

The experience should feel like a simple visual scene composer, not a professional animation suite.

## 27. Explicit V1 scope

Included:

- one Outline Scene per Editor session;
- one-time Scene Setup → Composition initialization;
- persisted canonical Composition JSON;
- desktop 16:9 canvas;
- Character and Prop objects;
- scene-level background;
- selection;
- move;
- uniform scale;
- visibility;
- delete Composition objects;
- add Story Characters and controlled Props;
- simple layer ordering;
- contextual Properties panel;
- explicit Save;
- dirty/save/error/conflict states;
- optimistic versioning;
- reload/reopen persistence;
- stable scene/object identity handling;
- usable empty scene;
- controlled missing-reference behavior.

## 28. Explicitly out of scope

- Timeline implementation;
- playhead;
- exact timing controls;
- Timeline clip creation/editing;
- keyframes;
- advanced animation;
- animation playback;
- rigging;
- facial animation;
- lip-sync;
- mocap;
- generated body motion;
- camera keyframes;
- interactive rotation;
- audio mixing;
- voice generation;
- dialogue editing in Composition;
- music;
- SFX;
- AI scene/object generation;
- arbitrary Editor image upload;
- character image generation;
- advanced asset management;
- Character CRUD;
- Prop catalog CRUD;
- collaboration;
- marketplace;
- YouTube publishing;
- 4K;
- mobile editor;
- multiplayer;
- multi-user Composition merge;
- autosave;
- undo/redo;
- automatic Scene Setup ↔ Composition synchronization;
- automatic reinitialization/merge;
- production deployment infrastructure.

Any additional Editor functionality discovered during implementation must be separately specified rather than silently added to SPEC-006.

## 29. Unresolved questions

These are implementation-level details that do not change the approved boundary:

1. Exact deterministic default placement algorithm for multiple initialized objects.
2. Exact placeholder rendering for Characters/Props without final visual assets.
3. Exact canvas interaction library/implementation, provided it preserves the logical transform model.
4. Exact API DTO names around the existing response envelope.
5. Whether the non-functional Timeline placeholder is shown.
6. Exact missing-reference recovery UI, provided there is no silent substitution.

No unresolved question permits a Composition schema change, Timeline behavior, or automatic Scene Setup synchronization without a new decision/specification.

## 30. Relevant architectural decisions

Existing ADRs remain authoritative:

- ADR-001 — Editor/Composition Model is the source of truth.
- ADR-002 — Composition schema is versioned.
- ADR-005 — Renderer consumes Composition JSON.
- ADR-006 — Media binaries use object storage.
- ADR-007 — Renderer is separate from API.
- ADR-009 — V1 is web desktop first.
- ADR-011 — Avoid premature infrastructure.
- ADR-012 — SDD + TDD + feature implementation memory.

SPEC-006 does not require a new ADR because it applies existing decisions without changing the Composition schema or renderer contract.
