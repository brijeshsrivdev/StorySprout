# Feature Implementation Index

Every meaningful completed feature gets durable implementation memory at:

`docs/features/<feature>/implementation.md`

The implementation document must describe the feature end-to-end and include a **FILE-BY-FILE IMPLEMENTATION MAP**.

## Required implementation-document sections
- Feature purpose
- Specification reference
- Acceptance criteria reference
- User flow
- End-to-end architecture flow
- Frontend implementation
- Backend implementation
- Database/persistence implementation
- AI/external services
- Renderer impact
- API contracts
- Data model
- Error handling
- Security considerations
- Automated tests
- Known limitations
- Future improvements
- Related decisions
- File-by-file implementation map

## File-by-file map requirements
For every important file involved in a feature, document:
- exact file path
- responsibility
- important behavior
- dependencies/interactions
- relationship to the feature

Important test files should also be mapped to the behavior they prove.

## Status vocabulary
- `PLANNED` — intended but not implemented.
- `SPECIFIED` — specification and acceptance criteria exist.
- `IN_PROGRESS` — implementation underway.
- `IMPLEMENTED` — implementation exists; final validation may still be pending.
- `VALIDATED` — appropriate automated validation has passed.
- `DEPRECATED` — retained for historical reference but no longer active.

## Current feature inventory
| Feature | Status | Specification | Implementation | Tests | Major dependencies |
|---|---|---|---|---|---|
| Repository Foundation | IMPLEMENTED | Pre-SDD foundation | Foundation implementation is represented by repository history and current-state documentation | API health test + CI checks | Next.js, Spring Boot, PostgreSQL, MinIO |

Do not create implementation documentation for functionality that has not actually been implemented and validated.
