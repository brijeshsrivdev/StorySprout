# StorySprout Current State

**Snapshot:** Repository foundation complete; Story Creation is the first specified product vertical slice and has not yet been implemented.

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

Story Creation does not modify the Composition Model. It creates/persists project and story setup only; scenes, timeline content, assets and rendered composition remain future slices.

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

The SDD/TDD project-memory documents provide process and navigation around those existing references.

## Specified next slice
**Story Creation — SPECIFIED**

Specification: `docs/specifications/story-creation.md`

Scope:
- Dashboard project list, empty state, create action and basic project cards.
- Create Story with AI or Blank mode.
- Story Setup with idea, target age, duration, visual style and language.
- Project creation and Story persistence APIs.
- Validation and error handling.
- Provider-independent `StoryGenerator` boundary with fake/mock test provider.
- Concrete frontend, backend, persistence, API and end-to-end test scenarios.

No implementation has started for this feature.

## Not implemented
- Story Creation application code.
- Authentication/authorization.
- Production project/story CRUD.
- Production editor or timeline UI.
- Real AI provider integrations or media generation.
- Asset upload/media pipeline.
- Actual renderer/FFmpeg pipeline.
- Render queue execution.
- Direct YouTube publishing.
- Production deployment.
- Payments/collaboration/unnecessary infrastructure.

## Foundation conflict/blocker review
No architectural conflict was discovered for Story Creation. The slice fits the existing web/API/PostgreSQL structure and the established AI boundary.

One product detail remains intentionally open before implementation: the exact finite target-age option set. The specification requires a stable domain/API value but does not silently invent the final labels/range. This must be resolved before implementation begins.

The existing foundation database migration contains no domain tables; Story Creation therefore requires a new Flyway migration during implementation. The existing foundation migration must remain unchanged.

## Validation status
This documentation change does not implement or validate Story Creation. Foundation checks remain the current executable validation. Always verify the latest GitHub Actions result before stating that the complete repository suite is green.
