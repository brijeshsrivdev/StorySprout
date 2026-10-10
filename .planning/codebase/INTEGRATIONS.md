---
last_mapped_commit: 96fb0f7cf29f2ea11b9ebc7038d9e8a13d6c672f
last_mapped_at: 2026-10-11
---
# External & Internal Integrations

**Analysis Date:** 2026-10-11

## External AI Providers

**Google Gemini API:**
- **Adapter:** `GeminiStoryGenerator` in `apps/api/src/main/java/com/storysprout/api/ai/`
- **Infrastructure Framework:** Spring AI (`spring-ai-starter-model-google-genai`) using `ChatClient` adapter API
- **Approved Model:** `gemini-2.5-flash`
- **Domain Boundary:** Standardized behind application-level `StoryGenerator` interface. Application domain logic remains completely decoupled from provider-specific types or SDKs (ADR-013).
- **Execution Budget:** Strict two-attempt interactive generation limit with local structured output validation.

## Object Storage (Media Binaries)

**MinIO / S3 Storage Gateway:**
- **Local Dev Service:** MinIO running on port 9000 (Console port 9001) via Docker Compose
- **SDK:** `io.minio:minio:8.6.0` Java client in `apps/api`
- **Purpose:** Stores media binaries (character images, scene backgrounds, audio tracks, rendered MP4 video artifacts).
- **Architecture Invariant:** Binary media files strictly belong in object storage; application metadata belongs in PostgreSQL (ADR-006).

## Databases & Persistence

**PostgreSQL Relational Storage:**
- **Container:** `storysprout-postgres` (PostgreSQL 18) on port 5432
- **Schema Management:** Flyway migrations (`db/migration/V1__...sql`)
- **Key Entities:**
  - `projects` / `stories` - Creator project state & metadata
  - `story_outlines` / `scenes` - Structure and outline hierarchy
  - `characters` / `props` - Reusable project assets
  - `compositions` - Canonical versioned Editor visual state (Composition JSON schema 1.1)
  - `render_jobs` - Video rendering task queue and lifecycle history

## Internal Services & Boundaries

**Renderer Microservice (`services/renderer`):**
- **Endpoint:** Independent HTTP service listening on port 4000
- **Isolation:** Completely isolated from PostgreSQL database (ADR-007)
- **Input:** Consumes an immutable snapshot of Composition JSON (`packages/editor-model`) and referenced asset binary URIs
- **Output:** Deterministic 1920×1080, 30fps, 16:9 MP4 video file stored back to MinIO/S3 object storage
- **Status API:** Exposes endpoints for job trigger, status polling, and artifact retrieval back to `apps/api`

**Web Frontend to API (`apps/web` → `apps/api`):**
- **Protocol:** HTTP REST APIs (`/api/v1/...`)
- **Flow:** Next.js client component hooks request project creation, outline generation, character setup, scene setup, editor persistence, preview retrieval, and render triggers.

## Authentication & Authorization

**V1 Status:**
- V1 focuses on desktop web single-creator journey. Production authentication/authorization systems are out of scope for V1 (`docs/project/current-state.md`).

---
*Integrations analysis: 2026-10-11*
*Update after integration contract modifications*
