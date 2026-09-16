# StorySprout Current State

**Snapshot:** Repository foundation; no product functionality has been implemented yet.

## Existing repository foundation
- Monorepo containing web, API, renderer, and shared packages.
- Next.js web skeleton in `apps/web`.
- Java 21 / Spring Boot API skeleton in `apps/api`.
- `GET /api/v1/health` with an automated API health test.
- TypeScript renderer skeleton with `/health` and `/ready`.
- `packages/editor-model` defines Composition schema version `1.0`.
- `packages/shared-types` contains initial Project, Asset, and RenderJob types.
- `packages/validation` provides the runtime validation boundary.
- Docker Compose provides local PostgreSQL and MinIO.
- GitHub Actions CI is configured for web lint/typecheck/build, API compile/tests, and renderer build.

## Canonical model
The current Composition Model is in `packages/editor-model/src/index.ts`. It defines Composition, Scene, SceneBackground, SceneObject, TimelineClip, and `createEmptyComposition()`. The schema version is `1.0`.

## Existing documentation
The original foundation documents remain in place and are not replaced:
- `docs/product/v1-scope.md`
- `docs/product/v1-screen-spec.md`
- `docs/architecture/overview.md`
- `docs/architecture/editor-composition.md`
- `docs/architecture/ai-boundary.md`
- `docs/architecture/rendering.md`
- `docs/architecture/local-development.md`
- `docs/api/api-conventions.md`

The new project/specification/testing/feature-memory documents provide process and navigation around those existing references.

## Not implemented
- authentication/authorization
- project/story/scene/character CRUD
- production editor or timeline UI
- AI provider integrations or media generation
- asset upload/media pipeline
- actual renderer/FFmpeg pipeline
- render queue execution
- direct YouTube publishing
- production deployment
- payments/collaboration/unnecessary infrastructure

## Validation status
Foundation checks exist. Always verify the latest GitHub Actions result before stating that the complete repository suite is green.
