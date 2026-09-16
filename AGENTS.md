# StorySprout Agent Instructions

This file is the primary engineering instruction set for future coding agents.

## Before changing anything
1. Read `AGENTS.md` first.
2. Read `docs/project/project-context.md`.
3. Read `docs/project/current-state.md`.
4. Read relevant decisions in `docs/project/decisions.md` and the applicable architecture documents.
5. For meaningful features, read the relevant specification before implementation.
6. Inspect the existing repository, code, tests, and documentation before changing anything.
7. Never assume a feature is missing without checking the existing implementation.

## Development method
StorySprout uses Specification-Driven Development (SDD) and Test-Driven Development (TDD).

For a meaningful feature, follow:

**Specification → Acceptance Criteria → Test Cases → Failing Tests → Implementation → Passing Tests → Refactor → Validation → Feature Documentation → Current-State Update**

Tests do not have to literally be written before every line of implementation. Behavior should be test-first/test-driven wherever practical. If an exploratory spike makes strict red-green sequencing impractical, define the behavior and tests as early as possible and document the exception.

SDD defines what should be built. Acceptance criteria make behavior explicit and testable. TDD provides executable proof that the implementation satisfies that behavior.

## Scope discipline
- Do not implement roadmap items unless explicitly requested.
- Do not introduce unnecessary dependencies, frameworks, services, or infrastructure.
- Do not silently expand feature scope.
- Prefer the established repository structure and technology choices.

## Architecture invariants
- The Editor/Composition Model is the source of truth for editable video semantics.
- The composition schema is versioned; schema changes require deliberate compatibility/versioning decisions.
- AI produces suggestions/assets. AI does not own canonical project state.
- AI providers stay behind internal abstractions/adapters.
- The renderer consumes Composition JSON and referenced assets; renderer semantics must remain aligned with the canonical Composition Model.
- Media binaries belong in object storage; application metadata belongs in PostgreSQL.
- Renderer remains separate from the API.
- V1 is web desktop first.
- V1 does not include direct YouTube publishing.
- V1 prefers preset animation rather than unrestricted AI motion.
- Do not introduce unnecessary microservices, Kubernetes, queues, or similar infrastructure without a concrete approved need.

## Documentation rules
Every meaningful completed feature must have/update `docs/features/<feature>/implementation.md` and `docs/features/FEATURE_INDEX.md`.

The implementation document must preserve end-to-end implementation memory, including a file-by-file implementation map with exact paths, responsibilities, important behavior, dependencies/interactions, and relationship to the feature.

Update `docs/project/current-state.md` after meaningful milestones. Update `docs/project/decisions.md` when an architectural/product decision changes. Keep existing product and architecture documents authoritative for their established subject areas; add references rather than duplicating or replacing them.

Documentation describes the current system but does not override executable code, approved specifications, tests, or recorded decisions. Never create implementation documentation for functionality that has not actually been implemented and validated.

## Completion requirements
Before finishing a task, report:
- files changed
- tests added/changed
- tests/builds/checks actually executed
- documentation updated
- known limitations
- remaining work

Never claim completion without running the appropriate validation. Never hide failed or skipped checks.
