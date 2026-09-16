# StorySprout

StorySprout is a browser-based kids video creation studio.

## Start here for development

**Future coding agents must read [`AGENTS.md`](AGENTS.md) first.**

The current implementation snapshot is [`docs/project/current-state.md`](docs/project/current-state.md). The project context, roadmap, and architectural/product decisions are under [`docs/project/`](docs/project/).

## V1 foundation

The repository is structured as a small monorepo around one core rule:

**AI → suggestions/assets → Project → Editor → Composition JSON → Renderer**

The editor/composition model is the source of truth. AI providers are replaceable and are not part of the repository foundation yet.

## Product references

- [`docs/product/v1-scope.md`](docs/product/v1-scope.md) — existing V1 scope
- [`docs/product/v1-screen-spec.md`](docs/product/v1-screen-spec.md) — existing V1 screen definition
- [`docs/architecture/`](docs/architecture/) — existing architecture references
- [`docs/specifications/README.md`](docs/specifications/README.md) — SDD process
- [`docs/features/FEATURE_INDEX.md`](docs/features/FEATURE_INDEX.md) — feature implementation memory
- [`docs/testing/testing-strategy.md`](docs/testing/testing-strategy.md) — TDD/testing strategy

## Repository layout

- `apps/web` — Next.js web application
- `apps/api` — Java 21 / Spring Boot API
- `services/renderer` — renderer service skeleton
- `packages/shared-types` — shared cross-application types
- `packages/editor-model` — canonical composition model
- `packages/validation` — runtime validation contracts
- `infrastructure/docker` — local infrastructure notes/configuration
- `docs` — product, architecture, specification, testing, security, and project-memory documentation

## Local development

1. Start local infrastructure:

   ```bash
   docker compose up -d
   ```

2. Install web workspace dependencies:

   ```bash
   npm install
   ```

3. Start the web application:

   ```bash
   npm run dev
   ```

4. Start the API from `apps/api`:

   ```bash
   ./mvnw spring-boot:run
   ```

See [`docs/architecture/local-development.md`](docs/architecture/local-development.md) for the existing detailed local-development instructions.

## Scope of this foundation

This stage intentionally does not include authentication, AI providers, media generation, editor UI, timeline UI, FFmpeg rendering, payments, collaboration, cloud deployment, Kubernetes, Redis, direct YouTube publishing, or additional product microservices.
