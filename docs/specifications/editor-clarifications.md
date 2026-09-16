# SPEC-006 Editor — Final Clarification Pass

**Specification ID:** SPEC-006
**Status:** SPECIFIED — implementation not started
**Purpose:** Final clarification pass before Editor implementation authorization.

## 1. Current-main inspection

This clarification pass was performed against the actual current `main` branch and the current `packages/editor-model`.

Current Composition schema remains `1.0` and currently contains `Composition`, `Scene`, `SceneBackground`, `SceneObject`, and `TimelineClip`. `SceneObject` currently contains only:

- `id`
- `assetId`
- `x`
- `y`
- `scale`
- `rotation`
- `visible`

The existing shared asset model distinguishes `CHARACTER`, `BACKGROUND`, and `PROP` at the asset-type level, but the current Character/Scene Setup implementation does not establish that every Character/Prop editor object has a persisted Asset record that can be resolved from `SceneObject.assetId`. Character and Prop V1 visuals may use deterministic placeholders. Therefore `assetId` alone is **not a reliable Character-vs-Prop identity mechanism for Editor V1**.

## 2. Character vs Prop identity — resolved

### Decision

**A Composition schema change is genuinely required.**

V1 `SceneObject` must explicitly carry:

```ts
objectType: "CHARACTER" | "PROP"
```

The field is part of the canonical Composition model. It is not inferred from asset names, IDs, URL patterns, placeholder IDs, catalog names, or other string conventions.

### Versioning

Because the current Composition schema is `1.0`, adding a required semantic field changes the canonical schema. Editor implementation must therefore introduce **Composition schema `1.1`** deliberately rather than silently adding the field to schema `1.0`.

The implementation specification must define the `1.0 → 1.1` compatibility/migration behavior before code is written. Existing `1.0` documents cannot be treated as `1.1` without an explicit migration/defaulting rule.

### Initialization semantics

During Scene Setup → Composition initialization:

- a selected Story Character becomes a `SceneObject` with `objectType: "CHARACTER"`;
- a Scene Setup Prop instance becomes a `SceneObject` with `objectType: "PROP"`;
- newly added Editor Characters use `objectType: "CHARACTER"`;
- newly added Editor Props use `objectType: "PROP"`.

`assetId` remains the visual asset reference. It does **not** determine semantic object type.

### Why not use AssetType

The repository's shared asset type union includes `CHARACTER` and `PROP`, but V1 Character/Prop records can be represented by deterministic placeholders and are not guaranteed to have a persisted Asset record. Consequently, relying on an Asset lookup would make Editor object identity conditional on an asset record that does not currently exist as a guaranteed invariant.

### No hidden mapping

There must be no external Composition-side map such as:

```text
sceneObjectId -> Character/Prop
```

The semantic type belongs directly in canonical Composition state.

## 3. Dialogue handoff — resolved

V1 Editor does **not** copy Dialogue into Composition.

The exact rule is:

```text
Scene Setup dialogue → remains Scene Setup data
Composition V1 → does not contain dialogue
```

Dialogue is not converted into `TimelineClip`, audio state, voice assets, or hidden Composition metadata by Editor V1.

The Editor may display a read-only contextual summary only if useful to the creator. Such a display does not make Dialogue part of Composition state.

### Future consumption

A future Dialogue/Audio/Timeline feature may read the persisted Scene Setup dialogue for the same `storyId + outlineSceneId`. That feature must define its own explicit conversion into the canonical Composition/Timeline representation. Once converted, the resulting Composition/Timeline representation becomes authoritative for that capability.

No automatic synchronization is implied. Changes to Scene Setup dialogue after Composition exists do not silently rewrite Composition or Timeline state.

## 4. Action intent handoff — resolved

V1 Editor does **not** copy Action Intent into Composition.

The exact rule is:

```text
Scene Setup action intent → remains Scene Setup data
Composition V1 → does not contain action intent
```

The fixed V1 intents remain:

```text
IDLE, TALK, WALK, RUN, WAVE, SIT, JUMP
```

They are not converted into `TimelineClip` entries because `TimelineClip` currently has no approved animation/action semantic and SPEC-006 does not define animation timing.

The Editor may show a read-only action-intent summary as context. It must not imply that an action intent is already animated.

### Future consumption

A future Animation/Timeline feature may read the persisted Scene Setup action intent for the same `storyId + outlineSceneId` and define the canonical animation/timeline representation. That conversion must be explicit and separately specified.

There is no hidden Composition-side action metadata.

## 5. Scene Setup synchronization remains one-way initialization

The authoritative lifecycle remains:

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

After Composition exists:

- Scene Setup changes do not silently mutate Composition.
- Editor edits do not silently mutate Scene Setup.
- Reopening Editor loads Composition.
- No automatic reinitialization or reconciliation exists in V1.

The future Dialogue/Animation/Timeline features may explicitly read Scene Setup preparation data, but that is a feature-level handoff, not a hidden Composition metadata mechanism.

## 6. Coordinate-system definition — resolved

The canonical Editor coordinate system is a **fixed logical 1920 × 1080 stage**.

```text
Stage width  = 1920 logical units
Stage height = 1080 logical units
Origin       = top-left
x range      = 0..1920
 y range     = 0..1080
```

`SceneObject.x` and `SceneObject.y` are stored in this logical stage coordinate system.

The browser may scale the rendered canvas to fit the available viewport. That visual CSS/device scaling does not rewrite Composition coordinates.

The specification does **not** call these coordinates "resolution-independent". They are logical coordinates in the fixed 1920 × 1080 canonical stage.

Because schema `1.1` still has no canonical object width/height, V1 constrains the object's anchor to the logical stage bounds rather than guaranteeing complete visual bounding-box containment.

## 7. Renderer compatibility

The Renderer remains a consumer of versioned Composition JSON plus referenced assets.

Editor V1 does not introduce Timeline clips, dialogue data, action data, or renderer-specific metadata.

The required future Renderer behavior is:

1. understand the approved Composition schema version;
2. understand `SceneObject.objectType` in Composition schema `1.1`;
3. continue using `assetId` for visual asset lookup;
4. ignore no required semantic fields;
5. preserve compatibility through the explicit Composition schema/version contract.

Renderer implementation is not part of this clarification pass.

## 8. Acceptance-criteria consistency check

The existing SPEC-006 acceptance-criteria set remains **31 criteria**. No scope is added or removed. The following clarifications apply:

| Area | Status after clarification |
|---|---|
| Editor entry / Story + Outline Scene identity | Consistent |
| Scene Setup → Composition initialization | Consistent; Character/Prop initialization now explicitly writes `objectType` in Composition `1.1` |
| Empty Scene Setup | Consistent; produces a valid Composition scene with no objects/background when none are prepared |
| Existing Composition reopening | Consistent; existing Composition remains authoritative |
| No duplicate initialization | Consistent; existing Composition is loaded rather than reconstructed |
| Character identity | Clarified: `objectType: "CHARACTER"` |
| Prop identity | Clarified: `objectType: "PROP"` |
| `assetId` | Remains visual asset reference; does not determine semantic type |
| Character/Prop deletion | Consistent; deletion removes only the Composition object |
| Layer ordering | Consistent; `Scene.objects[]` remains back-to-front canonical order |
| Add Character / Add Prop | Consistent; new objects receive explicit `objectType` |
| Save | Consistent; complete Composition is persisted explicitly |
| Versioning / conflict | Consistent; stale version must not overwrite newer Composition |
| Scene Setup changes after Composition exists | Consistent; no silent synchronization |
| Dialogue boundary | Clarified: remains Scene Setup data; not in Composition V1 |
| Action boundary | Clarified: remains Scene Setup data; not in Composition V1 |
| Timeline boundary | Consistent; `Scene.timeline` remains empty/inactive in SPEC-006 |
| Animation boundary | Consistent; action intents are not animation clips |
| Renderer compatibility | Clarified: Renderer must support versioned Composition `1.1` semantics when Editor implementation lands |
| Browser scaling | Consistent; visual scaling does not change logical coordinates |
| Coordinate model | Clarified: fixed logical 1920 × 1080 stage |
| Rotation | Consistent; preserved at `0` and not interactively edited in V1 |
| Scale range | Consistent: `0.25..3.0` |
| Visibility | Consistent |
| Properties panel | Consistent |
| Background behavior | Consistent; background remains scene-level Composition state |
| No hidden Composition metadata | Explicitly required |
| No Scene Setup live reconstruction | Explicitly required |
| Explicit Save / no autosave | Consistent |
| Undo/redo | Deferred, unchanged |
| No Editor implementation in SPEC-006 | Unchanged |

**Result:** all 31 acceptance criteria remain internally consistent after this clarification pass. The only canonical-model change required by the clarified semantics is the explicitly versioned `SceneObject.objectType` addition for Composition schema `1.1`.

## 9. Scope guard

This clarification pass does not authorize implementation and does not change:

- Scene Setup application behavior;
- Timeline implementation;
- Dialogue generation or voice generation;
- Animation generation/playback;
- Renderer implementation;
- asset generation/upload;
- YouTube publishing;
- authentication;
- collaboration;
- production deployment.

No implementation work is included in SPEC-006 authorization by this document.
