# StorySprout UI Foundation — Implementation Notes

## Scope

This milestone restores the shared web visual foundation and performs a focused UI QA/remediation pass across the existing V1 creator journey: Dashboard → Create Story → Story Setup → Outline → Characters → Scene Setup → Editor → Preview → Render status.

No product flow, backend/domain/render architecture, or Timeline behavior was changed.

## Root cause: unstyled localhost UI

The web application already contained extensive Tailwind utility classes and StorySprout design tokens in apps/web/app/globals.css, but the Next.js app had no PostCSS configuration.

The web package uses Tailwind CSS 4 and @import "tailwindcss". Tailwind 4 requires its PostCSS plugin to be wired into the build pipeline. Without apps/web/postcss.config.mjs registering @tailwindcss/postcss, the utility classes were not being compiled into the served stylesheet. The result was effectively default browser HTML despite the UI source containing the intended StorySprout styling.

This was a styling-pipeline defect, not a missing UI implementation.

## Shared foundation remediation

Added apps/web/postcss.config.mjs to register @tailwindcss/postcss.

Hardened apps/web/app/globals.css without changing the established token values: warm-neutral canvas, restrained indigo/violet accents, editor dark-surface tokens, browser baseline sizing, font smoothing, pointer/disabled cursor behavior, visible focus treatment, and reduced-motion behavior.

The existing shared primitives in apps/web/components/ui.tsx remain the primary visual contract for buttons, fields, panels, cards, shell/header, notices and save state.

## QA findings

Dashboard: creative-home hierarchy and continuation remain intact.

Create Story: AI and Blank remain two deliberate creative entry modes.

Story Setup: story idea remains the visual hero; supporting choices remain subordinate.

Story Outline: storyboard hierarchy, creator-shaped/AI-generated distinction, metrics and save states remain intact.

Characters: Project Cast and In this Story remain distinct; restrained initials/shapes remain the placeholder visual language.

Scene Setup: pre-production/staging structure remains intact with existing save state and Editor handoff.

Editor: signature dark workspace, stage, asset rail, inspector, save state, Preview and Render controls remain intact.

Preview: calm saved-Composition review surface remains intact.

Render: production/status flow remains integrated with Editor; renderer architecture is unchanged.

## Regression coverage

Added apps/web/tests/ui-foundation.test.tsx to protect shared primary/AI action styling contracts and branded/contextual StudioHeader rendering.

## Validation

GitHub Actions CI run #159 (35437795279) passed for exact branch HEAD c67b69a0e77ad282240ce4255d6782d800d2b7aa.

- Web: lint, typecheck, unit tests, production build — PASS
- API: compile, tests — PASS
- Renderer: typecheck, tests, build — PASS
- E2E: infrastructure startup and browser suite — PASS

The browser E2E environment included PostgreSQL, MinIO, renderer, API and web.

## Explicit non-goals

- No Timeline implementation.
- No animation/audio/voice work.
- No new backend/domain model.
- No Composition schema change.
- No renderer architecture change.
- No new product flow or feature.
