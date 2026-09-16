# Feature Specification — Scene Setup

**Specification ID:** SPEC-005
**Status:** SPECIFIED — implementation approved

See the approved SPEC-005 specification supplied for this implementation. Scene Setup is the preparation layer between Story Outline/Characters and the future Editor. It owns one Outline Scene's background preset, selected Story Characters, scene-local prop instances, ordered manual dialogue, and fixed action intents. It stores no duration, transforms, exact timing, keyframes, camera state, audio timing, Composition data, or AI-generated content.

Canonical flow:

```text
Story → Story Outline → Characters → Scene Setup → Editor / Timeline → Composition JSON → Renderer
```

V1 supports controlled background and prop presets, Story Character membership, manual Character/Narrator dialogue, and actions `IDLE`, `TALK`, `WALK`, `RUN`, `WAVE`, `SIT`, `JUMP`. A valid setup may be empty. Outline title, summary, order and planned duration remain authoritative. Outline deletion removes subordinate Scene Setup data; reorder preserves setup by stable Outline Scene ID. Open Editor is a preparation-data handoff only and must not create Composition.
