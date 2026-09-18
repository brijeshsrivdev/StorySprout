# Preview Implementation

## Scope
Preview establishes a static visual read of the saved canonical Composition 1.1.

## Implementation
- Added `/stories/{storyId}/preview?scene={sceneId}`.
- Preview loads the existing Editor context and reads `composition.compositionJson`.
- No new Preview backend representation was introduced.
- Preview uses `@storysprout/editor-model` `interpretScene` for the same background/object semantics used by the renderer.
- Visible objects are positioned using their canonical 1920×1080 coordinates and scaled dimensions.
- Object order is preserved by rendering the interpreted object list in Composition order.
- Hidden objects are omitted from the preview.
- Background presets map to deterministic visual colors.
- Preview explicitly communicates that it represents the current saved Composition and is static in V1.

## States
- loading
- empty Composition
- ready
- recoverable error
- return to Editor

## Editor integration
The frozen Editor gained only:
- Preview action
- Render action
- render status strip

Preview is disabled while Composition edits are unsaved, preventing an implicit mismatch between creator-visible working state and persisted preview state.

## Composition impact
None. Composition 1.1 remains canonical and unchanged.

## Tests
The shared renderer interpretation is covered by renderer tests. Full repository test execution was unavailable in the implementation environment.
