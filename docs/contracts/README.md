# Contracts

Contracts define boundaries between parts of StorySprout.

## Canonical contracts
- **Composition contract:** `packages/editor-model` is the canonical, versioned semantic model shared by editor and renderer.
- **Cross-application types:** `packages/shared-types` contains types shared between applications/services.
- **Runtime validation:** `packages/validation` is the validation boundary for data that must be checked at runtime.
- **API conventions:** existing endpoint conventions are documented in `docs/api/api-conventions.md`.

## Rules
API DTOs must not casually redefine Composition semantics. Provider-specific AI contracts must remain behind internal abstractions. Contracts that affect persisted or rendered data require deliberate compatibility/versioning treatment.
