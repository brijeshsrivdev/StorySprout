---
last_mapped_commit: 96fb0f7cf29f2ea11b9ebc7038d9e8a13d6c672f
last_mapped_at: 2026-10-11
---
# Coding & Architectural Conventions

**Analysis Date:** 2026-10-11

## Software Development Methodology

StorySprout follows **Specification-Driven Development (SDD)** and **Test-Driven Development (TDD)** (`AGENTS.md` & `ADR-012`):

```
Specification → Acceptance Criteria → Test Cases → Failing Tests → Implementation → Passing Tests → Refactor → Validation → Feature Documentation → Current-State Update
```

- **Specification First:** Every meaningful feature begins with a clear specification defining goals and constraints before writing code.
- **Durable Implementation Memory:** Every completed feature must document its exact file-by-file implementation map in `docs/features/<feature>/implementation.md` and update `docs/features/FEATURE_INDEX.md`.
- **Validation Standard:** Never claim task completion without running and passing all relevant build, lint, unit, integration, and E2E test suites.

## Code Style & Formatting

### TypeScript / Web (`apps/web`, `services/renderer`, `packages/*`)

- Strict TypeScript configuration (`"strict": true` in `tsconfig.json`).
- ESLint with Next.js & TypeScript rules (`npm run lint`).
- Component file naming: PascalCase for components (`SceneEditor.tsx`), camelCase for utilities/hooks (`useComposition.ts`).
- Avoid ad-hoc utility classes or arbitrary pixel inline styles; use predefined StorySprout design tokens and Tailwind utility classes (`@storysprout/web`).
- All interactive UI elements must have unique, descriptive `id` attributes to facilitate automated E2E browser testing.

### Java Backend (`apps/api`)

- Standard Java 21 conventions using Spring Boot idioms.
- CamelCase for class names (`GeminiStoryGenerator`), camelCase for method/variable names.
- Clean separation of layers: `Controller` → `Service` → `Repository` / `Gateway`.
- AI providers remain behind clean application interfaces (e.g. `StoryGenerator`). No Spring AI or Google GenAI provider-specific types leak into domain/application services.
- Database entity IDs: standard UUIDs or auto-incrementing numeric IDs mapped via Flyway migrations.

## Error Handling & Exception Discipline

- **No Masking or Symptom Patching:** Never catch exceptions silently, swallow errors, return dummy fallbacks, or delete failing assertions to pass tests (`AGENTS.md` guidelines).
- **Log Inspection:** Base diagnostics on empirical log output and complete stack traces.
- **Fail Fast & Explicitly:** API endpoints return appropriate HTTP status codes (`400 Bad Request`, `404 Not Found`, `422 Unprocessable Entity`, `500 Internal Server Error`) with explicit error messages.
- **Validation Boundaries:** Use `@storysprout/validation` and Spring Boot `@Valid` / `jakarta.validation` annotations at input boundaries.

## Architecture Boundaries & State Invariants

1. **AI Output vs Canonical State:** AI generates suggestions (text, outlines, initial asset drafts). The Editor/Composition Model owns canonical project state (`ADR-001`, `ADR-003`).
2. **Schema Versioning:** Composition schema modifications require explicit schema version increments (`1.0 -> 1.1`) and backwards-compatible migration handlers (`packages/editor-model/src/migration.ts`).
3. **Explicit Semantic Types:** All scene objects carry an explicit `objectType` discriminator (`CHARACTER` or `PROP`) in Composition schema `1.1`.
4. **Renderer Isolation:** The renderer service is stateless and reads binary media assets exclusively via object storage URLs (`ADR-006`, `ADR-007`).

---
*Conventions analysis: 2026-10-11*
*Update after team convention or guideline changes*
