---
last_mapped_commit: 96fb0f7cf29f2ea11b9ebc7038d9e8a13d6c672f
last_mapped_at: 2026-10-11
---
# Technology Stack

**Analysis Date:** 2026-10-11

## Languages

**Primary:**
- TypeScript 5.9 - All web frontend application code (`apps/web`), rendering service (`services/renderer`), and shared packages (`packages/editor-model`, `packages/shared-types`, `packages/validation`)
- Java 21 - Backend API microservice (`apps/api`)

**Secondary:**
- SQL / Flyway migrations - Database schema definition (`apps/api/src/main/resources/db/migration/`)
- HTML / CSS (Tailwind v4 PostCSS) - Web application styling and UI layout

## Runtime

**Environment:**
- Node.js 24.x (`>=24 <25` specified in root `package.json`)
- Java OpenJDK 21 (Spring Boot 4.1.1 runtime)
- Docker / Docker Compose - Infrastructure services runtime (PostgreSQL 18, MinIO, Renderer container)

**Package Manager:**
- npm 11 (`packageManager: "npm@11"` in `package.json`)
- Lockfile: `package-lock.json` present at workspace root
- Maven (`mvnw`) for Java API build and dependency resolution

## Frameworks

**Core:**
- Next.js 16.3 (React 19.1) - Desktop web creator studio interface (`apps/web`)
- Spring Boot 4.1.1 - REST API, orchestration, persistence, and external service gateway (`apps/api`)
- Vanilla Node.js + `tsx` / HTTP - Standalone rendering service (`services/renderer`)

**Testing:**
- Vitest 3.2 - Unit and component testing for web (`apps/web`)
- Playwright 1.55 - End-to-end browser integration tests (`apps/web/tests/e2e/`)
- JUnit Jupiter 5 + Spring Boot Starter Test + WebMvcTest - Backend Java unit/integration testing (`apps/api`)
- Testcontainers 1.21 - PostgreSQL integration testing for backend API (`apps/api`)
- Node Built-in Test Runner (`node --test`) - Renderer unit testing (`services/renderer`)

**Build/Dev:**
- TailwindCSS 4.1 (`@tailwindcss/postcss`) - Web UI design tokens and styling
- TypeScript Compiler (`tsc`) - Static typechecking and package building
- Spring Boot Maven Plugin - Java executable JAR packaging

## Key Dependencies

**Critical:**
- Spring AI 2.0.1 (`spring-ai-starter-model-google-genai`) - Infrastructure adapter for Google Gemini LLM provider (`apps/api`)
- MinIO Java SDK 8.6 - S3-compatible object storage SDK for media binaries (`apps/api`)
- Flyway Core - Database migration management (`apps/api`)
- PostgreSQL JDBC Driver - Relational database connector (`apps/api`)
- `@storysprout/editor-model` - Canonical versioned Composition Model package (monorepo internal)
- `@storysprout/shared-types` - Shared cross-application TypeScript definitions (monorepo internal)
- `@storysprout/validation` - Runtime validation schema boundary (monorepo internal)

**Infrastructure:**
- PostgreSQL 18 - Relational store for stories, characters, outlines, scenes, compositions, render jobs
- MinIO (`quay.io/minio/minio`) - Local S3-compatible object storage for binary media assets

## Configuration

**Environment:**
- Environment variables via `.env` / system env
  - API: `SPRING_PROFILES_ACTIVE`, `SPRING_AI_GOOGLE_GENAI_API_KEY`, `MINIO_URL`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`
  - Web: `NEXT_PUBLIC_API_URL`
  - Renderer: `PORT`, `MINIO_ENDPOINT`, `MINIO_BUCKET`

**Build:**
- Root `package.json` with npm workspaces (`apps/*`, `services/*`, `packages/*`)
- Root `docker-compose.yml` for local development services
- `apps/api/pom.xml` - Java Maven configuration
- `apps/web/next.config.ts`, `postcss.config.mjs`, `tsconfig.json`
- `services/renderer/tsconfig.json`

## Platform Requirements

**Development:**
- macOS / Linux / Windows with Docker Desktop or Docker engine
- Node.js 24.x and Java JDK 21 installed locally

**Production:**
- Desktop-web-first creator interface (V1 target)
- Containerized Java REST API service and TypeScript rendering service
- S3-compatible object storage for video rendering artifacts and media assets

---
*Stack analysis: 2026-10-11*
*Update after major dependency changes*
