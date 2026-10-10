---
last_mapped_commit: 96fb0f7cf29f2ea11b9ebc7038d9e8a13d6c672f
last_mapped_at: 2026-10-11
---
# Technical Concerns & Debt

**Analysis Date:** 2026-10-11

## V1 Functional & Feature Limitations

1. **Timeline & Animation Timing (`services/renderer`):**
   - Current renderer intentionally rejects non-empty Timeline content because V1 animation timing and keyframe interpolation are not yet implemented (`docs/project/current-state.md`).
   - Animation semantics in V1 are restricted to preset animations rather than unrestricted AI motion (`ADR-008`).

2. **Out of Scope V1 Features:**
   - No direct YouTube or social media video publishing (`ADR-010`).
   - No audio/music/SFX timing or lip-sync facial animation.
   - No unrestricted AI video generation or camera control path in V1.
   - No production authentication/authorization or payment integration in V1 slice.

## Architectural & Code Debt

1. **Composition Model Schema Migration:**
   - Legacy Composition `1.0` JSON snapshots lack explicit `objectType` fields. Migration utility `migrate10To11` in `packages/editor-model` requires caller-supplied types rather than guessing (`ADR-014`).
   - Any future Composition schema evolution must maintain strict schema versioning discipline (`ADR-002`).

2. **Spring AI & Provider Rate Limits:**
   - AI generation budget is explicitly capped at 2 interactive attempts per request (`ADR-013`).
   - Must monitor provider latency and rate limits to prevent cascading timeouts in `apps/api`.

3. **Styling & Design Tokens:**
   - Previous UI remediation fixed Tailwind v4 PostCSS processing (`apps/web/postcss.config.mjs`). Care must be taken to ensure shared design tokens are consistently used across all new editor components and screens.

## Security & Reliability Considerations

1. **API Key Management:**
   - Google Gemini API key (`SPRING_AI_GOOGLE_GENAI_API_KEY`) must never be logged, committed to source control, or exposed to the web client bundle.

2. **Object Storage Binary Cleanup:**
   - Rendering generates temporary MP4 video artifacts and media assets in MinIO/S3. Garbage collection / lifecycle policies for orphan assets should be addressed before production deployment.

---
*Concerns analysis: 2026-10-11*
*Update after addressing technical debt or identifying new risks*
