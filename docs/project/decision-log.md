# StorySprout Decision Log

## D001 — Repository is durable engineering memory
**Status:** Accepted

Important project context must be recoverable from the repository rather than depending on a ChatGPT Project conversation. Durable context includes architecture, decisions, current state, constraints, implementation notes, validation, and session handoff.

## D002 — Composition is the editable visual source of truth
**Status:** Accepted

Composition schema 1.1 is the canonical persisted visual state for the Editor and downstream Preview/Render flow. New representations must not silently become competing sources of truth.

## D003 — AI follows suggest → review → create
**Status:** Accepted

AI-generated content is treated as a starting point or proposal where creator review matters. The UI must make the transition from AI suggestion to creator-shaped/canonical content understandable.

## D004 — Renderer consumes immutable snapshots
**Status:** Accepted

A render job captures the canonical Composition version and an immutable snapshot. The renderer must render that snapshot and must not read current Composition state from PostgreSQL.

## D005 — Static Preview and Render share scene interpretation
**Status:** Accepted

Preview and V1 renderer use the shared editor-model scene interpretation so the creator sees the same structural scene representation before rendering.

## D006 — V1 animation scope remains intentionally limited
**Status:** Accepted

Timeline, keyframes, animation playback/generation, audio timing, lip-sync, advanced camera controls, and related professional animation features remain outside the current V1 implementation unless explicitly specified.
