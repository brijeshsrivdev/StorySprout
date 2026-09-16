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

## Changing a decision
Record the reason, affected components, migration/compatibility implications, and the new decision before or alongside implementation where practical.
