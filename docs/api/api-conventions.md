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

`fields` may be empty for non-field errors. Standard feature mappings are `400` validation, `404` resource not found, `409` conflict, `422` controlled processing/generation failure, and `500` unexpected server failure. Error responses must not expose stack traces, credentials, or provider internals.

Authentication, authorization and unrelated domain CRUD are outside this foundation.
