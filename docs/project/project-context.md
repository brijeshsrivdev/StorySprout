# StorySprout Project Context

## Product
StorySprout is a browser-based AI-powered kids video creation studio. Creators can select/create characters, scenes, backgrounds, props, voices, music and animations, arrange content on a timeline, preview it, and eventually render complete children’s videos suitable for publishing.

## V1 creator journey
`Create Story → Story Setup → Story Outline → Characters → Scene Setup → Editor → Timeline → Preview → Render`

The existing detailed V1 scope and screen documents remain the product references:
- `docs/product/v1-scope.md`
- `docs/product/v1-screen-spec.md`

## Current architecture
- `apps/web` — Next.js + TypeScript + Tailwind browser application/editor.
- `apps/api` — Java 21 + Spring Boot API for persistence/orchestration.
- `services/renderer` — separate rendering service.
- `packages/editor-model` — canonical versioned Composition Model.
- `packages/shared-types` — cross-application types.
- `packages/validation` — runtime validation boundary.
- PostgreSQL — application metadata.
- MinIO locally / object storage direction for media binaries.

Canonical flow:
`AI → suggestions/assets → Project → Editor → Composition JSON → Renderer`

## Product boundaries
V1 is desktop-web-first and creator-controlled. AI assists rather than owning project state. V1 does not include direct YouTube publishing, unrestricted AI motion, or premature infrastructure complexity.

## Engineering method
Meaningful work follows SDD + TDD and must leave durable feature implementation memory in `docs/features/`.
