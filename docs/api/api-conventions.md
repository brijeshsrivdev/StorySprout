# API conventions

- Base path: `/api/v1`
- JSON responses for application endpoints.
- Health endpoint: `GET /api/v1/health`.
- Resource identifiers are opaque strings/UUIDs.
- Composition JSON is a versioned domain contract and should not be casually duplicated in endpoint DTOs.

Authentication, authorization and domain CRUD are outside this foundation.
