---
last_mapped_commit: 96fb0f7cf29f2ea11b9ebc7038d9e8a13d6c672f
last_mapped_at: 2026-10-11
---
# Architecture Overview

**Analysis Date:** 2026-10-11

## Architecture Pattern

StorySprout is a monorepo application structured around Specification-Driven Development (SDD) and clean layer separation:

```
[ Next.js Web Studio (apps/web) ]
         │
         ▼ REST API
[ Java 21 / Spring Boot API (apps/api) ] ◄── AI Adapters ──► [ Google Gemini (Spring AI) ]
         │                                                            │
         ├─────────────────────────────────────────┐                  │ Suggestions / Assets
         ▼ Metadata                                ▼ Binaries         ▼
[ PostgreSQL Database ]                   [ MinIO / Object Storage ] ◄┘
                                                   ▲
                                                   │ Rendered Artifacts
                                          [ Renderer (services/renderer) ]
                                                   ▲ Consumes Composition JSON
                                                   │
                                     [@storysprout/editor-model]
```

## Creator Journey & Data Flow

**Canonical Creator Journey:**
`Create Story → Story Setup → Story Outline → Characters → Scene Setup → Editor → Timeline → Preview → Render`

1. **AI Output as Suggestion:** AI generates story outlines, character descriptions, and initial asset concepts. AI suggestions NEVER own canonical project state (ADR-003).
2. **Creator Control:** The creator reviews, shapes, and commits character/scene data in the studio interface.
3. **Editor / Composition Model:** Visual layout in the Editor builds the canonical Composition JSON (`packages/editor-model`, schema `1.1`).
4. **Preview:** Preview loads `composition.compositionJson`, applying the shared `@storysprout/editor-model` visual rules (fixed 1920×1080 stage) without duplicating state or logic.
5. **Render Job Execution:** Rendering creates an immutable JSON snapshot of the Composition. The API sends the job request to `services/renderer`, which renders a deterministic MP4 video to MinIO object storage.

## Core Architectural Invariants

- **ADR-001 (Source of Truth):** The Editor/Composition Model is the single canonical source of truth for editable video semantics.
- **ADR-002 (Schema Versioning):** Composition schema is explicitly versioned (currently `1.1` with explicit `SceneObject.objectType` discriminate: `CHARACTER` or `PROP`).
- **ADR-006 (Storage Split):** Application metadata belongs in PostgreSQL; media binaries belong in object storage.
- **ADR-007 (Renderer Isolation):** The renderer service is strictly isolated from PostgreSQL and accesses assets only via object storage URLs or passed payload data.
- **ADR-014 (Explicit Object Type):** SceneObject explicitly specifies `objectType` (`CHARACTER` vs `PROP`) rather than inferring from asset IDs or URLs.

## Layer Boundaries & Modules

- `apps/web`: Creator UX, page routing, design system components, visual canvas editor, timeline view, render triggers.
- `apps/api`: Business logic, domain models, JPA/JDBC persistence, AI provider gateways (`com.storysprout.api.ai`), object storage managers (`com.storysprout.api.storage`), and render job coordinator.
- `services/renderer`: Independent Node.js service using `@storysprout/editor-model` to translate Composition JSON snapshots into video frames and MP4 exports.
- `packages/editor-model`: Shared TypeScript library defining canonical Composition JSON interfaces, default scene object factories, schema migration utilities (`1.0 -> 1.1`), and layout calculations.
- `packages/shared-types`: Standard TypeScript types shared across web and services.
- `packages/validation`: Runtime boundary validators for API contracts and user input payload verification.

---
*Architecture analysis: 2026-10-11*
*Update after system design or ADR modifications*
