# StorySprout Testing Strategy

Testing proves that implementation satisfies specifications and acceptance criteria. Meaningful behavior follows SDD + TDD wherever practical.

## TDD workflow
**Specification → Acceptance Criteria → Test Cases → Failing Tests → Implementation → Passing Tests → Refactor → Validation**

Tests do not need to precede every line of code, but acceptance-criterion behavior should be defined and test-driven before or alongside implementation wherever practical.

## Frontend
- **Unit tests:** pure functions, state transformations, utilities and validation.
- **Component tests:** screen/component behavior, states, interactions and validation.
- **Editor-model tests:** serialization, schema invariants, transformations and version compatibility.
- **Integration tests:** important interactions between editor components, model, API clients and persistence-facing boundaries.
- **E2E:** critical creator workflows through the browser.

## Backend
- **Unit tests:** domain logic and isolated services.
- **Service tests:** application workflows and state transitions.
- **Repository tests:** persistence mappings, queries and constraints where needed.
- **API integration tests:** HTTP contracts, validation, errors and persistence interactions.

## Renderer
- **Composition tests:** valid/invalid Composition JSON and semantic interpretation.
- **Rendering tests:** scene composition, object placement, timing and deterministic rendering behavior.
- **Audio composition tests:** dialogue, music, SFX timing/mixing rules.
- **Output validation:** file existence, container/codec expectations, duration and other agreed output properties.

## End-to-end validation
Critical flows should eventually cover:
1. project creation
2. composition persistence
3. rendering
4. final MP4 validation

## Traceability
Use acceptance-criterion IDs in test names/comments where useful. A feature is not complete merely because code compiles; appropriate automated tests and validation must pass.

## Current foundation
The repository currently has an API health test and CI build/type/lint checks. Product-level test suites will be added with the corresponding specifications and features.
