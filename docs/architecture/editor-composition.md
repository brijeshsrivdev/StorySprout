# Editor and Composition Model

`packages/editor-model` defines the canonical serialized project composition.

## Rules

- `schemaVersion` starts at `"1.0"` and must be treated as a compatibility boundary.
- React state is an editing implementation detail, not the canonical model.
- API DTOs should map to the model rather than redefine its semantics.
- Renderer input is Composition JSON plus referenced assets.
- AI results are suggestions/assets and must not bypass the project/editor flow.

The model should evolve deliberately with explicit schema changes and migration/versioning when compatibility requires it.
