---
last_mapped_commit: 96fb0f7cf29f2ea11b9ebc7038d9e8a13d6c672f
last_mapped_at: 2026-10-11
---
# Codebase Structure

**Analysis Date:** 2026-10-11

## Repository Root Directory Layout

```
.
├── AGENTS.md                  # Primary engineering & development instructions
├── PROJECT_BOOTSTRAP.md       # Initial project bootstrap details
├── README.md                  # Monorepo overview & quick start guide
├── docker-compose.yml         # Local dev containers (PostgreSQL 18, MinIO, Renderer)
├── package.json               # Root npm workspace configuration
├── package-lock.json          # Root lockfile
├── apps/                      # Application binaries & web interfaces
│   ├── api/                   # Java 21 / Spring Boot backend service
│   └── web/                   # Next.js 16 / React 19 web creator studio
├── services/                  # Standalone background microservices
│   └── renderer/              # TypeScript video rendering microservice
├── packages/                  # Shared monorepo packages
│   ├── editor-model/          # Canonical versioned Composition Model & migrations
│   ├── shared-types/          # Shared TypeScript type definitions
│   └── validation/            # Runtime validation schemas
├── docs/                      # Comprehensive project specification & design memory
│   ├── architecture/          # ADRs & subsystem architecture documents
│   ├── features/              # Feature implementation memory (FEATURE_INDEX.md)
│   ├── product/               # V1 product specifications & screen specs
│   └── project/               # project-context.md, current-state.md, decisions.md
└── infrastructure/            # Environment & deployment scripts
```

## Detailed Directory Organization

### 1. `apps/web/` (Next.js Application)

- `src/app/` - Next.js App Router pages (`dashboard`, `create`, `story/[id]`, `editor`, `preview`, `render`)
- `src/components/` - Shared StorySprout UI primitives and design components
  - `ui/` - Buttons, cards, modals, sliders, design token controls
  - `editor/` - Canvas stage, object layer controls, toolbar, timeline controls
  - `preview/` - Composition preview player component
- `src/lib/` - API client utilities, editor state hooks, and composition helpers
- `tests/` - Vitest component tests and Playwright E2E test suites (`tests/e2e/`)

### 2. `apps/api/` (Java Spring Boot API)

- `src/main/java/com/storysprout/api/`
  - `ai/` - AI provider abstractions (`StoryGenerator`) & Gemini implementation
  - `editor/` - Composition model persistence & configuration
  - `project/` - Project, story, character, outline, scene controllers & services
  - `render/` - RenderJob entity, status workflow, queue trigger & service
  - `storage/` - MinIO object storage service wrapper
- `src/main/resources/`
  - `db/migration/` - Flyway SQL migration scripts (`V1__...`, `V2__...`)
  - `application.properties` - Spring configuration parameters
- `src/test/java/com/storysprout/api/` - JUnit integration tests with Testcontainers

### 3. `services/renderer/` (Rendering Service)

- `src/`
  - `server.ts` - Fastify / HTTP server entry point listening on port 4000
  - `render.ts` - Core video rendering engine consuming Composition JSON snapshots
  - `storage.ts` - S3/MinIO upload client for rendered MP4 artifacts
  - `render.test.ts` - Unit tests for renderer logic

### 4. `packages/editor-model/` (Canonical Composition Model)

- `src/`
  - `types.ts` - Schema `1.1` interfaces (`Composition`, `Scene`, `SceneObject`, `ObjectType`)
  - `migration.ts` - Version migration utilities (`migrate10To11`)
  - `defaults.ts` - Default scene object factories & coordinate math (1920×1080 stage)
  - `index.ts` - Package exports

### 5. `docs/` (Project Memory)

- `docs/project/project-context.md` - High-level vision & canonical journey
- `docs/project/current-state.md` - Live implementation snapshot & CI status
- `docs/project/decisions.md` - Recorded ADR-001 through ADR-014 architectural decisions
- `docs/features/FEATURE_INDEX.md` - Index of feature implementation documents

---
*Structure analysis: 2026-10-11*
*Update after file organization or module structural changes*
