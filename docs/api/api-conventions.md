# API conventions

- Base path: `/api/v1`
- JSON responses for application endpoints.
- Health endpoint: `GET /api/v1/health`.
- Resource identifiers are opaque strings/UUIDs.
- Composition JSON is a versioned domain contract and should not be casually duplicated in endpoint DTOs.

## Success envelope
Application endpoints return `{ "data": ... }`.

## Error envelope
Domain/application errors return:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed",
    "fields": [
      { "field": "idea", "message": "Story idea is required" }
    ]
  }
}
```

`fields` may be empty for non-field errors. Standard feature mappings are `400` validation, `404` resource not found, `409` conflict, `422` controlled processing/generation/domain failure, and `500` unexpected server failure. Error responses must not expose stack traces, credentials, or provider internals.

## Editor Composition endpoints

Editor uses the existing envelope and the canonical versioned Composition Model:

- `GET /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/editor` — loads Editor context and initializes a Composition when none exists; returns initialization status `INITIALIZED` or `EXISTING`.
- `POST /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/editor/initialize` — explicit idempotent initialization entry.
- `GET /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/composition` — returns canonical Composition JSON plus persisted optimistic version.
- `PUT /api/v1/stories/{storyId}/outline-scenes/{outlineSceneId}/composition` — accepts `{ "version": <loadedVersion>, "composition": <complete Composition 1.1> }` and persists only when the version is current.

Editor-specific error behavior:
- `404` — Story, Outline Scene, Scene Setup or Composition context does not exist.
- `409` — optimistic Composition version is stale; newer state is never overwritten.
- `422` — Composition is structurally received but violates schema, fixed-stage, transform, reference or Editor boundary validation.

Editor Composition schema `1.1` requires explicit `SceneObject.objectType` values `CHARACTER` or `PROP`; `assetId` is a visual reference and is not a semantic discriminator. Editor V1 persists no Timeline clips, dialogue, action intent or hidden metadata.

Authentication, authorization and unrelated domain CRUD are outside this foundation.
