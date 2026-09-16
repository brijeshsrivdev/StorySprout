# Feature Specification — AI Provider Integration

**Specification ID:** SPEC-002  
**Status:** SPECIFIED — implementation not started

## 1. Problem

StorySprout has a provider-independent `StoryGenerator`, but no real AI provider. Future AI features need reusable provider infrastructure without leaking Spring AI, Google GenAI, Gemini, credential, or provider-specific failure types into application/domain code.

The existing Story Creation contract is:

```text
StoryGenerator.generate(StoryGenerationRequest)
    -> StoryGenerationResult
```

The current request contains idea, target age, duration, visual style and language; the current result contains only generated title and draft. This contract must remain unchanged.

## 2. Goal

Introduce a reusable architecture for Spring AI + Google GenAI/Gemini behind the existing `StoryGenerator` boundary.

This specification is **specification only**. It does not add dependencies, provider code, credentials, migrations, Story Outline, or new Story Creation behavior.

## 3. Current repository baseline

- API: Java 21 + Spring Boot 4.1.1.
- Current `StoryGenerator` is provider-independent.
- `StoryService` depends only on `StoryGenerator` and provider-neutral request/result types.
- Current deterministic generator is credential-free.
- Maven currently has no Spring AI dependency.
- Existing AI architecture requires provider-specific SDKs/adapters to remain behind internal abstractions.

References: `docs/architecture/ai-boundary.md`, `docs/specifications/story-creation.md`, `docs/features/story-creation/implementation.md`.

## 4. Target architecture

```text
StoryService
    ↓
StoryGenerator
    ├── Fake/DeterministicStoryGenerator
    └── GeminiStoryGenerator
            ↓
       Spring AI ChatClient
            ↓
       Google GenAI Chat Model
            ↓
       Gemini
```

The boundary is:

```text
Domain/Application
      ↓
StoryGenerator
      ↓
Infrastructure adapter
      ↓
Spring AI
      ↓
Google GenAI / Gemini
```

Spring AI is an infrastructure mechanism, not a StorySprout domain model.

## 5. Package/layer boundaries

### Application/domain

Existing Story Creation classes remain provider-independent:

```text
com.storysprout.api.story.StoryGenerator
com.storysprout.api.story.StoryGenerationRequest
com.storysprout.api.story.StoryGenerationResult
com.storysprout.api.story.StoryService
```

These classes MUST NOT import:

- `org.springframework.ai.*`
- `org.springframework.ai.google.genai.*`
- `com.google.genai.*`
- Gemini-specific DTOs
- provider-specific authentication classes

### Infrastructure

The real adapter should live outside the Story Creation application/domain package, for example:

```text
com.storysprout.api.ai.infrastructure.google
```

Expected implementation:

```text
GeminiStoryGenerator implements StoryGenerator
```

It owns Spring AI interaction, Gemini prompt construction, model options, provider response mapping, provider exception translation and provider-specific observability/configuration.

Only the adapter may know the provider is Gemini.

### Future adapters

Future capabilities/providers should use small capability-specific interfaces such as `StoryOutlineGenerator`, `CharacterGenerator`, `BackgroundGenerator`, or `VoiceGenerator`. Do not create a generic provider-specific `AIService` that leaks model options into callers.

## 6. Spring AI compatibility

Spring AI 2.0.x supports Spring Boot 4.0.x and 4.1.x. The repository is on Spring Boot 4.1.1, so Spring AI 2.0.1 is compatible at the documented framework level. Spring AI 2.0.0 also explicitly moved to Spring Boot 4.1.0. The implementation should use the Spring AI 2.0.1 BOM/dependency-management line rather than manually mixing module versions.

No dependency is added by this specification.

## 7. Google GenAI dependency

Spring AI 2.0.1 documents this starter:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-google-genai</artifactId>
</dependency>
```

The implementation should use Spring AI dependency management and should not introduce an independently versioned Google GenAI SDK merely to bypass Spring AI.

## 8. ChatClient vs GoogleGenAiChatModel

### Decision: use `ChatClient`

`GeminiStoryGenerator` should depend on Spring AI `ChatClient`, not directly orchestrate `GoogleGenAiChatModel`.

Reasons:

1. `ChatClient` is Spring AI's high-level portable chat API.
2. It supports synchronous calls suitable for the current StoryGenerator use case.
3. It provides typed structured-output APIs.
4. It keeps Google-specific model classes out of the application contract.
5. It makes future Spring AI provider replacement easier.

`GoogleGenAiChatModel` remains the Spring AI infrastructure implementation underneath ChatClient. Direct use is allowed only if a future concrete requirement cannot be expressed through ChatClient and a new architectural decision records why.

## 9. Gemini model compatibility — blocking finding

The requested model is:

```text
gemini-2.0-flash
```

Spring AI 2.0.1 still contains API/documentation references to Gemini 2.0 Flash, including a low-level example and model constant. However, Google's current Gemini deprecation documentation states that `gemini-2.0-flash` and `gemini-2.0-flash-001` had a shutdown date of **June 1, 2026**. The current date is September 16, 2026.

Therefore:

**`gemini-2.0-flash` is not an appropriate live integration target now.**

This is a compatibility finding, not a silent product decision. The implementation MUST NOT silently substitute another model. A currently supported Gemini model must be explicitly selected before `GeminiStoryGenerator` is implemented.

The model identifier MUST remain configuration-driven so the model can change without changing `StoryGenerator` or `StoryService`.

## 10. Gemini authentication

Spring AI documents two Google GenAI modes.

### Gemini Developer API

Uses a Google AI Studio API key through:

```text
spring.ai.google.genai.api-key
```

This is suitable for controlled development/manual smoke testing.

### Vertex AI

Uses Google Cloud configuration/credentials through:

```text
spring.ai.google.genai.project-id
spring.ai.google.genai.location
spring.ai.google.genai.credentials-uri
```

Production deployment should evaluate Vertex AI with managed Google Cloud identity/secret management rather than embedding long-lived service-account keys.

The application/domain layer must not branch on authentication mode.

## 11. Configuration and secrets

Configuration must be externalized. No secret may be committed to source control, tests, database records, Docker images, or GitHub Actions YAML.

Configuration concepts are:

```text
SPRING_AI_MODEL_CHAT=google-genai
SPRING_AI_GOOGLE_GENAI_API_KEY=<development secret>
SPRING_AI_GOOGLE_GENAI_PROJECT_ID=<project>
SPRING_AI_GOOGLE_GENAI_LOCATION=<region>
SPRING_AI_GOOGLE_GENAI_CREDENTIALS_URI=<credential source>
SPRING_AI_GOOGLE_GENAI_CHAT_MODEL=<approved supported model>
```

The implementation should use Spring Boot relaxed binding for the documented Spring AI properties. Do not create a second provider configuration system without a concrete reason.

## 12. Provider selection

There must remain one application-level `StoryGenerator` dependency.

Selection should be explicit through Spring configuration/profile/conditional bean wiring:

```text
fake/deterministic → FakeStoryGenerator
Gemini              → GeminiStoryGenerator
```

Tests must default to the deterministic generator and require no network or credentials.

The implementation must prevent simultaneous active `StoryGenerator` beans from creating ambiguity.

## 13. StoryGenerator contract

Do not redesign the existing contract:

```text
StoryGenerator.generate(StoryGenerationRequest)
    -> StoryGenerationResult
```

Current result remains:

```text
generatedTitle
generatedDraft
```

Do NOT add scenes, characters, assets, dialogue, Composition JSON, timeline clips, image prompts, render instructions, or media references merely because Gemini can produce them.

No provider response type may cross the application boundary.

## 14. Prompt construction

Prompt construction belongs in the Gemini adapter or a clearly separated infrastructure prompt component.

It must use the complete current `StoryGenerationRequest`:

- idea
- target age
- duration
- visual style
- language

It must preserve the Story Creation constraints:

- target age: `3_5`, `6_8`, `9_12`
- duration: `1`, `3`, `5`
- visual style: `2D`, `3D`, `HYBRID`
- language: `ENGLISH`, `HINDI`

Prompt templates should be versionable and testable. Full creator prompts should not be logged by default.

## 15. Structured output

The adapter should use typed structured output rather than arbitrary prose parsing.

Spring AI 2.0.1 provides `ChatClient.entity(...)` for typed mapping and supports provider-native structured output plus schema validation/self-correction.

Implementation strategy:

1. Define an infrastructure-only response DTO containing title and draft.
2. Request the typed response through ChatClient.
3. Prefer provider-native structured output when supported by the selected Gemini model.
4. Validate title/draft before returning `StoryGenerationResult`.
5. Treat missing, blank or malformed output as controlled generation failure.
6. Keep Spring AI DTOs/schema outside the application/domain layer.

`validateSchema()` may be used as a bounded reliability mechanism. It can cause additional model calls and token usage, so its maximum attempts must be explicit.

## 16. Timeout handling

Every external AI call must have a finite configurable timeout.

Timeouts must:

- prevent indefinite request blocking;
- be configurable by environment;
- be observable separately where practical;
- become controlled `StoryGenerationException`/generation failure;
- never expose provider internals to API consumers.

The exact numeric timeout is intentionally deferred to implementation benchmarking rather than invented here.

## 17. Retry behavior

Retries must be bounded and classify failures.

Retryable examples:

- transient network failure;
- provider 5xx;
- rate limiting when retryable guidance is provided.

Non-retryable examples:

- invalid credentials;
- invalid/retired model;
- malformed request;
- non-recoverable safety/policy rejection;
- exhausted structured-output correction.

Transport retries and structured-output correction retries are separate budgets and must never form an unbounded retry loop.

Spring AI provides retry infrastructure, but StorySprout must establish an intentionally small interactive-generation retry budget rather than inheriting an unexpectedly large cost/latency budget.

## 18. Provider failure handling

Provider-specific exceptions are translated at the infrastructure boundary into the existing `StoryGenerationException` or an explicitly documented subtype.

Story Creation state remains:

```text
GENERATING → COMPLETED
GENERATING → FAILED
```

Existing API behavior remains unchanged.

Never return provider names, raw SDK exceptions, credentials, authorization headers, raw payloads or stack traces to the browser.

## 19. Malformed output

Malformed output includes invalid JSON, missing fields, wrong field types, blank title/draft, unexpected structure, or mapping failures.

Handling:

1. Prefer provider-native structured output.
2. Map into an infrastructure DTO.
3. Validate required fields.
4. Optionally perform bounded schema correction.
5. Fail the generation in a controlled way if the result remains invalid.

Malformed content must never be persisted as a successful generated result.

## 20. Observability

AI operations should expose safe metrics/traces such as:

- operation name (`story_generation`);
- configured model identifier;
- duration;
- success/failure;
- failure category;
- retry count;
- timeout count;
- rate-limit count;
- token usage where reliably available.

Do not log by default:

- API keys;
- credentials;
- authorization headers;
- full creator prompts;
- full generated stories;
- raw provider payloads.

Spring AI usage/response metadata may support token accounting, but provider metadata must remain infrastructure-level data rather than Story domain fields.

## 21. Token/cost controls

AI calls are metered external operations.

Implementation should bound:

- prompt size;
- output token count;
- transport retries;
- structured-output correction attempts.

Where provider metadata reliably exposes usage, collect input/output/total token counts for observability. Do not add provider billing fields to the Story domain without a separate product specification.

Thinking configuration must not be enabled for a model that does not support it.

## 22. Testing strategy

### Unit tests — no network

Required:

- Fake generator remains deterministic.
- Gemini adapter maps valid provider output to `StoryGenerationResult`.
- Prompt contains all StoryGenerationRequest fields.
- Approved target-age values are handled correctly.
- Valid structured result maps correctly.
- Missing/blank title or draft fails safely.
- Malformed model output fails safely.
- Provider exception becomes controlled failure.
- Timeout becomes controlled failure.
- Retry stops at configured bound.
- Non-retryable failure is not retried.
- Model identifier is configuration-driven.

### Configuration tests

- Fake mode starts without Gemini credentials.
- Gemini configuration binds correctly.
- Missing Gemini credentials/configuration produces a clear controlled configuration error.
- Provider selection produces exactly one active StoryGenerator.

### Application tests

Existing StoryService tests must continue to mock `StoryGenerator`. They must not require Spring AI classes, a Gemini client, network access or credentials.

### Integration tests

A Spring application-context/infrastructure test may verify Spring AI/ChatClient wiring with mocked external clients. It must not call real Gemini.

### Optional manual smoke test

A separate opt-in test may use a real credential to validate authentication, model availability, structured output, timeout and usage metadata. It must not run in normal CI.

No Gemini API key may be stored in GitHub Actions.

## 23. Security

- No API keys in Git.
- No credentials in source or fixtures.
- No credentials in Dockerfiles.
- No credentials in GitHub Actions YAML.
- Secrets injected at runtime.
- Logs redacted.
- API errors sanitized.
- Creator ideas/drafts treated as untrusted user content.

Production should prefer managed identity/secret management where supported by the deployment platform.

## 24. Future provider replacement

Replacing Gemini must not require changes to:

- StoryService;
- StoryGenerator request/result;
- Story Creation API DTOs;
- Story persistence schema;
- Story Creation frontend.

Provider-specific differences remain inside the adapter. If a provider cannot satisfy the current contract, create a new specification/decision rather than leaking provider-specific concepts into the existing boundary.

## 25. Future AI capabilities

The same infrastructure pattern may support:

```text
StoryOutlineGenerator
CharacterGenerator
BackgroundGenerator
VoiceGenerator
MusicGenerator
```

Each capability must have its own provider-independent contract and feature specification. Shared infrastructure may be reused; domain contracts must remain capability-oriented.

## 26. Composition/renderer impact

None.

Real Gemini story generation remains story content only. It must not create Composition JSON, scenes, characters, timeline clips, render instructions or media binaries.

The existing Composition source-of-truth and renderer boundaries remain unchanged.

## 27. API impact

No new public endpoint is required.

The existing Story Creation endpoint remains:

```text
POST /api/v1/projects/{projectId}/stories
```

Its request/response contract does not change merely because the underlying generator implementation changes.

## 28. Persistence impact

No database migration or table is required.

AI credentials/configuration must not be persisted in Story domain records.

Future AI usage/cost persistence requires a separate specification.

## 29. Acceptance criteria

- **AC-01:** Spring AI 2.0.1 compatibility with Spring Boot 4.1.1 is documented.
- **AC-02:** Gemini can be introduced behind the existing StoryGenerator without changing StoryService's contract.
- **AC-03:** Application/domain code has no Spring AI or Google GenAI dependency.
- **AC-04:** Gemini adapter uses ChatClient normally; direct GoogleGenAiChatModel use requires explicit justification.
- **AC-05:** Provider configuration and model identifier are externalized; credentials are never committed.
- **AC-06:** Fake generator remains available for normal tests/local runs without credentials.
- **AC-07:** Gemini Developer API and Vertex AI authentication modes are configuration concerns, not domain concerns.
- **AC-08:** Timeout, provider failures and malformed output become controlled StoryGenerator failures.
- **AC-09:** Retry behavior is bounded and distinguishes transient from permanent failures.
- **AC-10:** Structured title/draft mapping is typed and validated.
- **AC-11:** Sensitive prompts, generated content and credentials are not logged by default.
- **AC-12:** Normal CI tests require no live Gemini API.
- **AC-13:** Optional real-provider smoke testing is manual/opt-in only.
- **AC-14:** StoryGenerator request/result semantics remain unchanged.
- **AC-15:** No scenes, characters, assets, Composition or rendering behavior is added.
- **AC-16:** Model selection is configuration-driven and can be changed without application/domain changes.
- **AC-17:** Implementation cannot proceed against the retired `gemini-2.0-flash` model without an explicit supported-model decision.

## 30. Test scenarios

### Boundary
- **T-AI-01:** StoryService tests require no Spring AI classes.
- **T-AI-02:** StoryGenerator request/result contain no provider types.
- **T-AI-03:** Only infrastructure adapter imports Spring AI/Google-specific APIs.

### Configuration
- **T-AI-04:** Fake mode starts without Gemini credentials.
- **T-AI-05:** Gemini model/configuration binds correctly.
- **T-AI-06:** Missing required Gemini configuration is handled clearly.
- **T-AI-07:** Exactly one StoryGenerator implementation is selected.

### Prompt/output
- **T-AI-08:** Prompt contains idea, age, duration, style and language.
- **T-AI-09:** Valid structured output maps to title/draft.
- **T-AI-10:** Malformed output fails safely.
- **T-AI-11:** Missing/blank title or draft fails safely.
- **T-AI-12:** No scenes/characters enter StoryGenerationResult.

### Failure/retry
- **T-AI-13:** Timeout becomes controlled failure.
- **T-AI-14:** Transient provider failure retries within the bound.
- **T-AI-15:** Permanent provider failure is not retried.
- **T-AI-16:** Structured-output correction is bounded.

### Security/observability
- **T-AI-17:** Secrets are absent from source/config/test fixtures.
- **T-AI-18:** API errors do not expose provider credentials/raw exceptions.
- **T-AI-19:** Safe metrics/logs exclude raw prompts and secrets.

### Regression
- **T-AI-20:** Existing StoryService tests remain provider-independent.
- **T-AI-21:** Blank Story behavior is unchanged.
- **T-AI-22:** Story Creation API remains unchanged.

### Optional manual integration
- **T-AI-23:** Credentialed Gemini smoke test is explicitly opt-in and never required by CI.

## 31. Compatibility blocker

Google's current Gemini deprecation documentation lists `gemini-2.0-flash` and `gemini-2.0-flash-001` with a June 1, 2026 shutdown date. Therefore the requested `gemini-2.0-flash` target is retired as of this specification date.

Spring AI 2.0.1 may still expose API/documentation references for the model family, but provider availability is the controlling constraint.

**Required before implementation:** explicitly select a currently supported Gemini model. Do not silently substitute one in code.

## 32. Out of scope

- Spring AI dependency changes.
- GeminiStoryGenerator implementation.
- StoryGenerator redesign.
- StoryService changes.
- Story Creation UI/API changes.
- Real Gemini credentials.
- GitHub Actions secrets.
- Real Gemini CI calls.
- Story Outline.
- Character/scene/background generation.
- Image/audio/video generation.
- Composition generation.
- RAG/vector stores.
- MCP/tools/agent workflows.
- AI billing persistence.
- Database migrations.
- Production deployment changes.

## 33. Dependencies

Existing:

- Java 21.
- Spring Boot 4.1.1.
- Existing StoryGenerator abstraction.
- Existing Story Creation tests.
- Existing API error envelope.
- Existing provider-independent AI architecture.

Future implementation:

- Spring AI 2.0.1 BOM/dependency management.
- `spring-ai-starter-model-google-genai`.
- Explicitly approved currently supported Gemini model.
- Runtime-only credentials.

## 34. Related decisions

- ADR-003 — AI proposes suggestions/assets, not canonical project state.
- ADR-004 — AI providers are abstracted.
- ADR-011 — Avoid premature infrastructure.
- ADR-012 — SDD + TDD and durable feature implementation memory.
- ADR-013 — Spring AI is infrastructure behind provider-independent AI boundaries.
