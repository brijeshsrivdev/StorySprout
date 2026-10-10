---
last_mapped_commit: 96fb0f7cf29f2ea11b9ebc7038d9e8a13d6c672f
last_mapped_at: 2026-10-11
---
# Testing Strategy & Patterns

**Analysis Date:** 2026-10-11

## Testing Framework Overview

StorySprout enforces multi-tiered automated testing across all monorepo components:

| Component | Test Level | Framework / Runner | Target Location |
|---|---|---|---|
| `apps/web` | Unit & Component | Vitest + React Testing Library | `apps/web/**/*.test.ts(x)` |
| `apps/web` | End-to-End Browser | Playwright | `apps/web/tests/e2e/*.spec.ts` |
| `apps/api` | Unit & Integration | JUnit Jupiter + Spring Boot Test | `apps/api/src/test/java/...` |
| `apps/api` | DB Integration | Testcontainers (PostgreSQL) | `apps/api/src/test/java/...` |
| `services/renderer` | Unit & Rendering | Node Test Runner (`node --test`) | `services/renderer/src/*.test.ts` |

## Execution Commands

**Root Workspace Workflows:**
- `npm run test` - Executes unit/component tests for `apps/web` and `services/renderer`
- `npm run typecheck` - Runs static TypeScript verification across all TS projects
- `npm run lint` - Runs ESLint code quality checks
- `npm run build` - Validates web and renderer production builds

**Frontend Web Testing (`apps/web`):**

```bash

# Run unit & component tests

npm --workspace apps/web run test

# Watch mode for unit tests

npm --workspace apps/web run test:watch

# Run Playwright E2E browser tests (requires API, PostgreSQL, MinIO, Renderer running)

npm --workspace apps/web run test:e2e
```

**Backend API Testing (`apps/api`):**

```bash

# Run backend compilation & unit/integration tests

cd apps/api && ./mvnw test
```

**Renderer Testing (`services/renderer`):**

```bash

# Run renderer unit tests

npm --workspace services/renderer run test
```

## Testing Guidelines & Practices

### 1. Test-Driven Development (TDD)

- Behavior should be test-first/test-driven wherever practical.
- Acceptance criteria defined in specifications serve as the baseline for test cases.

### 2. Isolation & Determinism

- **AI Generator Isolation:** Default backend tests use deterministic mock/stub generators (`DeterministicStoryGenerator`) that require no external API keys or network access (`ADR-013`).
- **Media Binary Isolation:** E2E and storage tests run against local MinIO containers or mock storage services.
- **Database Isolation:** Java integration tests use Testcontainers to spin up ephemeral PostgreSQL instances ensuring clean test state.

### 3. Rendering Verification

- `services/renderer/src/render.test.ts` validates composition loading, asset retrieval, and error handling for non-empty timeline content.
- Rendering output tests ensure deterministic 1920×1080 MP4 file generation.

### 4. CI Validation Baseline

- GitHub Actions CI pipeline runs full multi-suite validation on every pull request and commit:
  - Web linting, typechecking, unit tests, and Next.js production build.
  - Java API Maven compile and test execution.
  - Renderer typechecking, unit tests, and Docker build.
  - Full E2E browser verification with live PostgreSQL, MinIO, API, and Renderer.

---
*Testing analysis: 2026-10-11*
*Update after test suite or framework changes*
