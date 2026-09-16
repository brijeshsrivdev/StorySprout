# StorySprout

StorySprout is a browser-based kids video creation studio.

## V1 foundation

The repository is structured as a small monorepo around one core rule:

**AI → suggestions/assets → Project → Editor → Composition JSON → Renderer**

The editor/composition model is the source of truth. AI providers are replaceable and are not part of the repository foundation yet.

## Repository layout

- `apps/web` — Next.js web application
- `apps/api` — Java 21 / Spring Boot API
- `services/renderer` — renderer service skeleton
- `packages/shared-types` — shared cross-application types
- `packages/editor-model` — canonical composition model
- `packages/validation` — runtime validation contracts
- `infrastructure/docker` — local infrastructure notes/configuration
- `docs` — product and architecture documentation

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

The local PostgreSQL database and MinIO object storage are provided by Docker Compose.

## Scope of this foundation

This stage intentionally does not include authentication, AI providers, media generation, editor UI, timeline UI, FFmpeg rendering, payments, collaboration, cloud deployment, Kubernetes, Redis, or additional product microservices.
