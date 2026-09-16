# StorySprout Decisions

These decisions are the durable architectural/product record. Do not silently reverse them.

## ADR-001 — Editor/Composition Model is the source of truth
**Status:** Accepted

AI output is editable input. The editor/project owns canonical state and serializes it as Composition JSON consumed by the renderer.

## ADR-002 — Composition schema is versioned
**Status:** Accepted

The Composition Model has an explicit schema version. The current foundation starts at `1.0`; compatibility changes require deliberate versioning/migration decisions.

## ADR-003 — AI proposes suggestions/assets, not project state
**Status:** Accepted

AI may generate story content or assets, but cannot bypass creator/project/editor ownership of canonical state.

## ADR-004 — AI providers are abstracted
**Status:** Accepted

Application behavior depends on internal AI gateways/interfaces. Provider-specific SDKs/adapters remain replaceable.

## ADR-005 — Renderer consumes Composition JSON
**Status:** Accepted

Renderer input is the versioned Composition JSON plus referenced assets. Renderer semantics must stay aligned with the Composition Model.

## ADR-006 — Media binaries use object storage
**Status:** Accepted

PostgreSQL stores application metadata. Media/generated binaries belong in object storage. MinIO is the local development implementation.

## ADR-007 — Renderer is separate from API
**Status:** Accepted

Rendering is isolated as a separate service so rendering workloads and implementation can evolve independently from API/UI workloads.

## ADR-008 — V1 uses preset animation
**Status:** Accepted

V1 uses controlled/preset animation semantics rather than unrestricted AI-generated motion. Any expansion requires an explicit product/architecture decision.

## ADR-009 — V1 is web desktop first
**Status:** Accepted

The first creator experience targets desktop web. Other clients are future scope.

## ADR-010 — No direct YouTube publishing in V1
**Status:** Accepted

V1 renders exportable video but does not directly publish to YouTube.

## ADR-011 — Avoid premature infrastructure
**Status:** Accepted

Do not introduce unnecessary microservices, Kubernetes, Redis/queues, production-scale infrastructure, or other complexity until a specified feature has a concrete need and the decision is recorded where architectural impact exists.

## ADR-012 — SDD + TDD + feature implementation memory
**Status:** Accepted

Meaningful features follow Specification → Acceptance Criteria → Test Cases → Failing Tests → Implementation → Passing Tests → Refactor → Validation → Feature Documentation → Current-State Update. Feature implementation docs preserve end-to-end knowledge and exact file responsibilities.

## ADR-013 — Spring AI is infrastructure behind provider-independent AI boundaries
**Status:** Accepted and implemented

Spring AI is used as the infrastructure/integration mechanism for external AI providers, but it remains behind StorySprout's application-level AI contracts. Application/domain code does not depend on Spring AI, Google GenAI, Gemini-specific classes, provider credentials, or provider-specific response types.

The Gemini integration implements the existing `StoryGenerator` contract through `GeminiStoryGenerator`. `ChatClient` is the adapter-level API. `GoogleGenAiChatModel` is used only by Spring AI's own auto-configuration underneath ChatClient; StorySprout code does not directly depend on it.

The approved initial Gemini model is `gemini-2.5-flash`. Model selection remains externalized through Spring AI configuration. The retired `gemini-2.0-flash` and `gemini-2.0-flash-001` models are not integrated.

Authentication remains a configuration concern supporting Gemini Developer API and Vertex AI. Normal tests use the deterministic generator and require no credentials or network access.

Reliability uses a deliberate two-layer boundary: Spring AI's model-level retry is limited to one attempt, while StorySprout's Gemini adapter owns a maximum two-attempt interactive generation budget. This prevents nested transport retries from multiplying the total AI request budget. Structured-output schema self-correction is not enabled in this slice for the same reason; provider-native structured output is used and the result is validated locally.

This implementation does not change StoryGenerator, StoryService, Story Creation API contracts, database schema, Composition, renderer behavior, or product scope. Story Outline remains out of scope.

## ADR-014 — Composition SceneObject carries explicit semantic object type
**Status:** Accepted — implementation deferred to SPEC-006

The current Composition schema `1.0` has no explicit Character-vs-Prop identity on `SceneObject`. Although the shared asset model has `CHARACTER` and `PROP` asset types, V1 Character/Prop visuals are not guaranteed to resolve through persisted Asset records because deterministic placeholders are valid. Therefore `SceneObject.assetId` is not a reliable semantic discriminator.

SPEC-006 requires an explicit canonical `SceneObject.objectType` with the values `CHARACTER` or `PROP`. This is a Composition semantic change and therefore requires Composition schema `1.1` rather than silently extending schema `1.0`.

The object type is assigned during Scene Setup → Composition initialization and when Editor adds an object. It is part of Composition JSON; no hidden external SceneObject-to-type mapping is permitted.

Dialogue and action intent remain Scene Setup preparation data in V1 and are not copied into Composition. Future Dialogue/Audio/Animation/Timeline features may explicitly read Scene Setup data by the stable Story + Outline Scene identity and define their own canonical Composition/Timeline representation. This does not create hidden Composition metadata or automatic synchronization.

The canonical Editor coordinate system remains a fixed logical `1920 × 1080` stage. Browser/CSS scaling changes presentation only; it does not rewrite Composition coordinates.

Implementation must define the `1.0 → 1.1` compatibility/migration behavior before changing the executable Composition model. No application code, Composition code, migration, API, test, or renderer implementation is included in this specification clarification pass.

## Changing a decision
Record the reason, affected components, migration/compatibility implications, and the new decision before or alongside implementation where practical.
