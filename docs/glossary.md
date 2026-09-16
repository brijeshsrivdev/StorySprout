# StorySprout Glossary

## V1 concepts

**Project** — The creator's top-level video/story workspace and persisted unit.

**Story** — The narrative content associated with a project; includes story setup and, after the Story Outline slice, its planning outline.

**Story Outline** — An editable narrative planning structure that turns a Story into an ordered sequence of Outline Scenes. It is not the Editor/Composition model and does not contain visual placement, animation, timeline or renderer semantics.

**Outline Scene** — A planning-level section of a Story Outline with an order, title, summary and planned duration. It describes what happens in the story and guides future Scene Setup. It is not a Composition Scene.

**Scene** — In future visual/editor context, a bounded section of the story with visual/background/object and timeline content. When referring specifically to the planning layer, use **Outline Scene** to avoid ambiguity.

**Composition Scene** — A future/editor-level scene represented within the canonical Composition Model. It contains editable video semantics and is distinct from an Outline Scene.

**Character** — A reusable story character represented through one or more character assets.

**Asset** — A reusable media resource such as a character image, background, prop, audio, music or SFX.

**Prop** — A non-character visual asset used in a scene.

**SceneObject** — An instance of an asset placed in a scene with spatial/display properties.

**Composition** — The canonical, versioned serialized representation of the editable video semantics. It is the source of truth consumed by the renderer.

**Timeline** — The temporal arrangement of scene content, represented by timeline clips in the Composition Model.

**TimelineClip** — A time-bounded item in a scene timeline, such as an object, dialogue, audio, music or SFX clip.

**Dialogue** — Spoken narrative or character speech associated with story/video timing.

**Voice** — A voice resource or generated speech used for dialogue.

**Music** — Background musical content used in the video.

**SFX** — Sound effects used at specific points in the composition.

**Preview** — Playback/visualization of the composition before final rendering.

**Render** — The process of converting Composition JSON and referenced assets into a final video artifact.

**RenderJob** — The tracked unit of work representing a requested render and its status/output.

## Future concepts / not yet implemented

**Episode** — A publishable or serial story unit within a larger content collection. Future concept; not currently modeled as a separate V1 entity.

**World** — A reusable story universe/environment containing shared characters, visual rules or locations. Future concept.

**Style** — A reusable visual/style definition controlling the look of generated or selected content. V1 may capture visual style as story setup input, but a full reusable Style domain concept is future scope.

## Terminology rule
Use these terms consistently in specifications and implementation documentation. If a new domain term is introduced, update this glossary and record an architectural/product decision when the terminology changes existing semantics.
