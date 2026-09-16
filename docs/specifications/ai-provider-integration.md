# Feature Specification — AI Provider Integration

**Specification ID:** SPEC-002  
**Status:** SPECIFIED — implementation not started

## 1. Problem

StorySprout now has a provider-independent `StoryGenerator` boundary, but no real AI provider integration. Future AI features need a reusable infrastructure boundary that can use a hosted model without leaking provider SDKs, Spring AI APIs, credentials, model-specific types, or provider-specific failure semantics into StorySprout application/domain logic.

The current repository already establishes the intended abstraction: `StoryService` depends on `StoryGenerator`, whose contract is `StoryGenerator.generate(StoryGenerationRequest) -> StoryGenerationResult`. The existing Story Creation result remains only generated title and draft content. fileciteturn358file0

## 2. Goal

Introduce a reusable Spring AI integration architecture that can provide a real Gemini-backed implementation of `StoryGenerator` while preserving the existing application boundary and allowing future providers to be substituted without changing StoryService or Story Creation API contracts.

This specification is intentionally infrastructure/integration focused. It does not implement real Gemini calls, change Story Creation behavior, add Story Outline, or introduce any new AI-powered product capability.

## 3. Current repository baseline

- API runtime: Java 21 and Spring Boot 4.1.1. fileciteturn327file0
- Current StoryGenerator is a small application-level interface with no provider dependency. fileciteturn358file0
- StoryService receives StoryGenerator by dependency injection and constructs a provider-neutral `StoryGenerationRequest`. fileciteturn359file0
- Current request contains idea, target age, duration, visual style and language. fileciteturn357file0
- Current result contains generated title and generated draft only. fileciteturn356file0
- Existing architecture explicitly requires AI providers to stay behind internal abstractions/adapters. fileciteturn349file0
- Current Maven configuration contains no Spring AI dependency. fileciteturn327file0
- Story Creation currently uses a deterministic provider-free generator; real provider integration is not implemented. fileciteturn325file0

## 4. User/system story

As a StorySprout application, I need a real AI provider implementation behind `StoryGenerator` so that Story Creation and future generation features can use an external model without coupling their application contracts to Google, Gemini, Google GenAI SDK classes, or Spring AI APIs.

## 5. Proposed target architecture

```text
StoryService
    |
    v
StoryGenerator
    +-----------------------------+
    |                             |
    v                             v
Fake/Deterministic           GeminiStoryGenerator
StoryGenerator                     |
                                   v
                              Spring AI ChatClient
                                   |
                                   v
                         Google GenAI Chat Model
                                   |
                                   v
                            Gemini provider
```

The important ownership rule is:

```text
Domain/Application
      |
      v
StoryGenerator
      |
      v
Infrastructure AI adapter
      |
      v
Spring AI
      |
      v
Google GenAI / Gemini
```

Spring AI is an infrastructure integration mechanism, not the StorySprout domain model.

## 6. Package/layer boundaries

### Application/domain layer

The existing `com.storysprout.api.story.StoryGenerator`, `StoryGenerationRequest`, `StoryGenerationResult`, `StoryService`, and related Story Creation domain classes remain provider-independent.

Application/domain code MUST NOT import:

- `org.springframework.ai.*`
- `org.springframework.ai.google.genai.*`
- `com.google.genai.*`
- Gemini-specific response/request classes
- provider-specific authentication classes

The existing `StoryGenerator` signature is preserved. No redesign is authorized by this specification.

### Infrastructure layer

The real provider implementation belongs in a provider/infrastructure package, proposed as:

```text
com.storysprout.api.ai.infrastructure.google
```

The exact package may be refined during implementation only if it remains clearly outside the application/domain package and does not require a new architecture decision.

Expected implementation responsibility:

```text
GeminiStoryGenerator implements StoryGenerator
```

It owns:

- Spring AI `ChatClient` interaction
- Gemini-specific prompt construction
- model-specific options
- provider response mapping
- provider exception translation
- provider-specific configuration access
- provider-specific observability metadata handling

Only this adapter layer may know that the selected provider is Google Gemini.

### Configuration layer

Spring Boot configuration properties for AI provider selection and credentials belong outside the domain/application model. Secrets must be supplied externally.

### Future provider adapters

Future implementations may include:

```text
OpenAiStoryGenerator
AnthropicStoryGenerator
VertexGeminiStoryGenerator
LocalStoryGenerator
```

without changing `StoryService` or the `StoryGenerator` contract.

## 7. Spring AI compatibility investigation

Spring AI 2.0.x documentation states support for Spring Boot 4.0.x and 4.1.x. The repository uses Spring Boot 4.1.1, so the selected Spring AI line is compatible at the documented framework level. citeturn1search10turn3search8

Spring AI 2.0.0 was explicitly upgraded to Spring Boot 4.1.0, and Spring AI 2.0.1 is the current stable 2.0.x release referenced by the supplied documentation. citeturn3search0

Implementation should therefore use the Spring AI 2.0.1 dependency-management/BOM line rather than manually mixing Spring AI module versions. The repository's Spring Boot parent remains authoritative for its application dependency management unless a concrete dependency conflict is discovered during implementation.

No dependency is added by this specification.

## 8. Google GenAI dependency

For Spring AI 2.0.1, the documented Google GenAI starter is:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-google-genai</artifactId>
</dependency>
```

The 2.0.1 Google GenAI documentation identifies this starter as the Spring Boot auto-configuration entry point and separately documents the underlying `spring-ai-google-genai` module. citeturn2search0

Implementation MUST use Spring AI dependency management/BOM and MUST NOT introduce an independently versioned Google GenAI SDK merely to bypass Spring AI.

## 9. ChatClient vs GoogleGenAiChatModel

### Decision for StorySprout: use ChatClient at the adapter boundary

`GeminiStoryGenerator` should depend on Spring AI's `ChatClient` rather than directly constructing or orchestrating `GoogleGenAiChatModel`.

Reasoning:

1. `ChatClient` is the Spring AI high-level portable API for communicating with chat models. citeturn1search6
2. Spring AI's ChatClient is explicitly designed to allow portable chat-model usage and model substitution. citeturn1search4
3. Structured output is exposed directly through `ChatClient`, including typed `.entity(...)` mapping and provider-native structured-output controls. citeturn1search0turn1search3
4. Using ChatClient keeps Google-specific model classes out of `GeminiStoryGenerator`'s public contract and leaves room for another Spring AI-supported provider later.
5. The current StoryGenerator use case is a normal synchronous application request, not a low-level provider SDK feature requiring direct `GoogleGenAiChatModel` control.

`GoogleGenAiChatModel` remains a Spring AI infrastructure implementation behind ChatClient. It should be used directly only if a future concrete requirement cannot be expressed through ChatClient and that requirement is documented as an architecture decision.

This is an infrastructure decision, not a domain API change.

## 10. Gemini model compatibility — blocking finding

The requested model is `gemini-2.0-flash`.

Spring AI 2.0.1's Google GenAI documentation still demonstrates `gemini-2.0-flash` through the low-level `GoogleGenAiChatModel` example and exposes a `ChatModel.GEMINI_2_0_FLASH` model constant in the documented API surface. citeturn2search0turn2search6

However, the same current Spring AI 2.0.1 reference's supported-model configuration list centers on newer Gemini models, while its model-support table identifies Gemini 2.0 Flash as `gemini-2.0-flash-001`. citeturn2search0

More importantly, Google's current Gemini deprecation documentation states that `gemini-2.0-flash` and `gemini-2.0-flash-001` had a shutdown date of **June 1, 2026**. The current date is September 16, 2026. Google lists `gemini-3.6-flash` as the recommended replacement. citeturn0search1

### Specification consequence

`gemini-2.0-flash` MUST NOT be treated as a production-ready implementation target at this point. The requested model is no longer an appropriate live integration target because Google's documented shutdown date has passed.

This is an explicit compatibility/blocking finding, not a silent change to the requested product decision. Before real Gemini implementation begins, the product/architecture decision must select a currently supported Gemini model. The specification does not silently substitute another model.

The adapter MUST keep the model identifier configuration-driven so that this decision can be changed without changing `StoryGenerator` or StoryService.

## 11. Gemini authentication

Spring AI's Google GenAI integration supports two documented modes:

### Gemini Developer API

Uses a Google AI Studio API key. Spring AI documents `spring.ai.google.genai.api-key` for this mode. When this property is supplied, Spring AI uses the Gemini Developer API instead of Vertex AI. citeturn2search0

This mode is appropriate for controlled development/manual integration testing where the operational requirements are limited.

### Vertex AI

Uses Google Cloud project/location and Google Cloud credentials. Spring AI documents:

- `spring.ai.google.genai.project-id`
- `spring.ai.google.genai.location`
- `spring.ai.google.genai.credentials-uri`

and Google Cloud authentication/application-default credentials for Vertex AI. citeturn2search0

### StorySprout approach

The architecture MUST support both modes through configuration, while avoiding application-level branching on provider credential details.

For initial real-provider development, Gemini Developer API may be used for a credentialed manual smoke test because it is the simplest documented path. Production deployment should be evaluated separately for Vertex AI and Google Cloud workload identity/managed credentials rather than baking a service-account key into the application.

This specification does not authorize production authentication changes yet.

## 12. Configuration

Provider configuration must be externalized. No secret belongs in source code, tests, database records, Docker images, repository configuration files, or GitHub workflow YAML.

Expected configuration concepts:

```text
SPRING_AI_MODEL_CHAT=google-genai
SPRING_AI_GOOGLE_GENAI_API_KEY=<secret, development only>
SPRING_AI_GOOGLE_GENAI_PROJECT_ID=<project>
SPRING_AI_GOOGLE_GENAI_LOCATION=<region>
SPRING_AI_GOOGLE_GENAI_CREDENTIALS_URI=<credential source when required>
SPRING_AI_GOOGLE_GENAI_CHAT_MODEL=<approved supported model>
```

The exact environment-variable binding names must follow Spring Boot relaxed binding for the documented Spring AI properties rather than creating a parallel provider-specific configuration namespace unless implementation demonstrates a concrete need.

Model selection must be configuration-driven. `StoryGenerator` must not contain a hard-coded model identifier.

## 13. Provider selection

The application must continue to have one application-level `StoryGenerator` dependency.

The implementation should support deterministic selection by Spring configuration/profile/conditional bean wiring:

```text
story generator mode
    |
    +-- fake/deterministic → Fake/DeterministicStoryGenerator
    |
    +-- Gemini             → GeminiStoryGenerator
```

The default test configuration MUST select the deterministic generator and MUST NOT require a network connection or credential.

Production/runtime configuration may select `GeminiStoryGenerator` explicitly.

The implementation must prevent accidental simultaneous active implementations that cause ambiguous Spring injection. Selection should be explicit and deterministic.

## 14. StoryGenerator interaction

The existing boundary remains exactly:

```text
StoryGenerator.generate(StoryGenerationRequest)
    -> StoryGenerationResult
```

The current request remains:

```text
idea
 targetAge
 durationMinutes
 visualStyle
 language
```

The current result remains:

```text
generatedTitle
generatedDraft
```

No scenes, characters, assets, dialogue, Composition JSON, timeline clips, image prompts, or render instructions may be added to this result as part of provider integration.

The adapter maps the provider response into `StoryGenerationResult` and no provider response object may cross the application boundary.

## 15. Prompt construction

Prompt construction belongs inside `GeminiStoryGenerator` or a provider-independent prompt-building component in the infrastructure/application integration layer. It must not be embedded in controllers, repositories, or domain entities.

The prompt must use the complete `StoryGenerationRequest` and must preserve the current Story Creation semantics:

- target age is one of `3_5`, `6_8`, `9_12`
- duration is 1, 3, or 5 minutes
- visual style is 2D, 3D, or Hybrid
- language is English or Hindi

Prompt text must not be persisted merely for generation. If prompts are logged for debugging, logging must use a redacted/sanitized representation and must not expose sensitive creator data by default.

Prompt templates must be versionable so future prompt changes are reviewable and testable.

## 16. Structured output strategy

The current `StoryGenerationResult` is already a typed application result. The Gemini adapter should request a structured response rather than relying on arbitrary prose parsing.

Spring AI 2.0.1's `ChatClient` supports `.entity(Type.class)` for typed structured output. It also supports provider-native structured output and schema validation/self-correction. citeturn1search0turn1search7

For the StoryGenerator implementation:

1. Define an infrastructure/provider response DTO containing only the fields required to map to `generatedTitle` and `generatedDraft`.
2. Request typed output through ChatClient.
3. Prefer provider-native structured output when supported by the selected Gemini model; Spring AI documents native structured output for Google GenAI models including Gemini 1.5 Pro and later. citeturn1search3
4. Validate the mapped result before returning it through `StoryGenerator`.
5. Treat missing/blank title or draft as a controlled generation failure.
6. Do not expose the Spring AI DTO or schema to the application/domain layer.

`validateSchema()` may be used as an additional reliability mechanism when the selected model/provider path supports it. It introduces additional model calls and therefore additional latency/token cost, so retry limits must be explicit rather than relying on broad defaults. citeturn1search7

## 17. Timeout handling

AI calls are external network operations and must have a bounded timeout.

The timeout policy must:

- prevent an AI request from hanging indefinitely;
- be configurable by deployment environment;
- be measured separately from normal API processing where observability is available;
- translate timeout failures into the existing controlled `StoryGenerationException`/generation-failure behavior;
- avoid exposing provider exception details to API consumers.

The exact timeout value is intentionally left for implementation benchmarking rather than invented in this specification. The implementation must establish a finite default and allow configuration.

## 18. Retry behavior

Retries must distinguish transient provider/network failures from permanent failures.

Retryable examples:
- connection reset;
- temporary transport failure;
- provider 5xx;
- rate limiting when the provider supplies a retryable response and backoff guidance.

Non-retryable examples:
- invalid credentials;
- invalid model identifier;
- malformed request;
- safety/policy rejection where retrying the same request will not change the result;
- deterministic structured-output validation failure after the configured maximum attempts.

Spring AI exposes retry infrastructure and provider-specific integrations may use retry configuration. citeturn3search11

Implementation must define a small bounded retry policy appropriate to interactive Story Creation. It must not inherit an unexpectedly large retry budget that can multiply token usage and user-visible latency.

Structured-output self-correction retries and transport retries are separate concerns and must not combine into an unbounded retry loop.

## 19. AI provider failure handling

Provider failures are translated at the infrastructure boundary into the existing application-level `StoryGenerationException` or an explicitly documented subtype if more granularity is required.

The application continues to own generation state. For Story Creation:

```text
GENERATING → COMPLETED
GENERATING → FAILED
```

The existing Story Creation API behavior remains unchanged.

Provider names, raw SDK exceptions, request IDs containing sensitive information, stack traces, API keys, authorization headers, and raw provider payloads must not be returned to the browser.

## 20. Malformed output handling

Malformed output includes:

- missing title;
- missing draft;
- wrong JSON type;
- invalid JSON;
- unexpected structure;
- blank generated content;
- content that cannot be mapped to the expected infrastructure DTO.

Handling:

1. Prefer provider-native structured output where available.
2. Parse into an infrastructure DTO.
3. Validate required fields.
4. Optionally use bounded Spring AI schema-validation retry.
5. If still invalid, throw controlled generation failure.
6. Never persist malformed generated content as a successful StoryGenerationResult.

## 21. Observability and logging

AI calls should be observable without exposing sensitive creator data or secrets.

Recommended measurements:

- provider/model identifier;
- generation operation name, e.g. `story_generation`;
- request duration;
- success/failure count;
- failure category;
- retry count;
- token usage where the provider response exposes it;
- structured-output validation failures;
- timeout count;
- rate-limit count.

Do NOT log by default:

- API keys;
- authorization headers;
- raw Google credentials;
- full creator prompts;
- full generated stories;
- arbitrary provider payloads.

If tracing later includes prompt/response capture, it must be explicitly configured, redacted, access-controlled, and reviewed against child-safety/privacy requirements.

Spring AI exposes observability facilities and ChatResponse metadata can provide usage information, which can support token/cost accounting without making provider metadata part of StoryGenerator's domain result. citeturn0view0turn1search0

## 22. Token and cost considerations

AI generation must be treated as a metered external operation.

Implementation should track, where reliably available:

- input token usage;
- output token usage;
- total token usage;
- retry-induced additional usage;
- model identifier.

The application should not persist provider-specific usage fields in the Story domain as part of this integration unless a later product/billing specification requires it.

Prompt size should remain bounded. Retry counts and output token limits must be bounded to prevent accidental cost multiplication.

Thinking features should not be enabled for a model that does not support them. Spring AI's current documentation identifies Gemini 2.0 Flash as not supporting thinking configuration. citeturn2search0

## 23. Testing strategy

### Unit tests — no network

Required:

- Fake/deterministic generator returns deterministic result.
- Gemini adapter maps a provider response into `StoryGenerationResult`.
- Prompt construction includes all `StoryGenerationRequest` fields.
- Approved target-age values are passed without transformation errors.
- Structured result mapping handles valid title/draft.
- Blank title/draft is rejected.
- Malformed model output becomes controlled generation failure.
- Provider exception becomes controlled generation failure.
- Timeout becomes controlled generation failure.
- Retry policy stops after its configured bound.
- Non-retryable provider failure is not retried.
- Model identifier comes from configuration rather than application/domain code.

### Configuration tests

- Gemini configuration binds correctly without a credential present in source.
- Fake generator can start the application without Gemini credentials.
- Gemini mode fails fast or reports a clear configuration error when required credentials/configuration are missing.
- Provider selection does not create multiple ambiguous `StoryGenerator` beans.

### Application behavior tests

StoryService tests continue to mock `StoryGenerator`. No Spring AI classes should be needed to prove StoryService behavior.

The existing Story Creation tests therefore remain deterministic and provider-independent.

### Integration tests

A Spring application-context test may verify that the Gemini adapter and ChatClient wiring can start with mocked/fake external clients. It must not call the real Gemini service.

### Optional manual real-provider smoke test

A separately invoked/manual integration test may call Gemini with a developer credential to verify:

- authentication;
- model availability;
- prompt/response mapping;
- structured output;
- timeout behavior;
- usage metadata.

This test MUST be opt-in and MUST NOT run in normal CI.

No Gemini API key may be stored in GitHub Actions.

## 24. Security and secrets

- No API keys in Git.
- No credentials in application source.
- No credentials in test fixtures.
- No credentials in Dockerfiles.
- No credentials in GitHub Actions YAML.
- Secrets must be injected by the runtime environment/secret manager.
- Logs must not expose secrets.
- Error responses must not expose provider authentication details.
- Creator story ideas and generated drafts are user content and must be handled as untrusted input.

Production credential strategy should prefer managed Google Cloud identity/secret management rather than long-lived service-account JSON files when the deployment platform supports it.

## 25. Future provider replacement

The provider replacement contract is:

```text
StoryService
    ↓
StoryGenerator
    ↓
provider adapter
```

Replacing Gemini must not require changes to:

- StoryService;
- StoryCreation API DTOs;
- Story persistence schema;
- StoryGenerator request/result types;
- frontend Story Creation behavior.

Provider-specific differences belong inside the adapter. If a future provider cannot satisfy the existing `StoryGenerator` semantics, that mismatch must be addressed with a new specification/decision rather than leaking provider-specific concepts into the current contract.

## 26. Future AI capabilities

The same infrastructure architecture is intended to support future boundaries such as:

```text
StoryOutlineGenerator
CharacterGenerator
BackgroundGenerator
VoiceGenerator
MusicGenerator
```

Each future capability should have its own provider-independent application contract and feature specification.

Do not create one generic `AIService` that exposes provider-specific options to all callers. Prefer small capability-oriented contracts with shared infrastructure underneath.

## 27. Renderer and Composition impact

None for this specification.

Real Gemini story generation continues to produce story content only. It does not generate or persist Composition JSON, scenes, characters, timeline clips, render instructions, or media binaries.

The existing Composition source-of-truth architecture remains unchanged. AI remains an input/suggestion mechanism; the project/editor remains the canonical owner of state. fileciteturn323file0

## 28. API impact

No new public API endpoint is required by this specification.

Story Creation's existing endpoint remains:

```text
POST /api/v1/projects/{projectId}/stories
```

Its request and response contract do not change merely because the underlying `StoryGenerator` implementation changes.

## 29. Persistence impact

No new database table or migration is required.

AI provider configuration and credentials must not be persisted in the Story domain.

Optional future AI usage/cost persistence requires a separate product/architecture specification.

## 30. Acceptance criteria

- **AC-01:** Spring AI 2.0.1 compatibility with Spring Boot 4.1.1 is documented and implementation uses the compatible dependency-management approach.
- **AC-02:** A real Gemini implementation can be introduced behind `StoryGenerator` without changing StoryService's contract.
- **AC-03:** Domain/application code has no dependency on Spring AI or Google GenAI classes.
- **AC-04:** Gemini integration uses Spring AI ChatClient as the normal adapter-level API; direct `GoogleGenAiChatModel` usage requires a separately justified decision.
- **AC-05:** Provider configuration and model identifier are externalized; credentials are not committed.
- **AC-06:** Fake/deterministic StoryGenerator remains available for normal tests and local runs without credentials.
- **AC-07:** Gemini authentication supports the documented Gemini Developer API and Vertex AI modes without changing StoryGenerator.
- **AC-08:** Gemini provider failures, timeouts, and malformed output are translated into controlled StoryGenerator failures.
- **AC-09:** Retry behavior is bounded, classifies retryable/non-retryable failures, and cannot create an unbounded token/cost loop.
- **AC-10:** Structured StoryGenerationResult mapping is typed and validated before successful application return.
- **AC-11:** No provider secrets, raw credentials, full prompts, or full generated responses are logged by default.
- **AC-12:** Normal CI tests do not require a live Gemini API or credential.
- **AC-13:** An optional/manual real-provider smoke test can validate the configured provider without being part of normal CI.
- **AC-14:** StoryGenerator's current request/result semantics remain unchanged.
- **AC-15:** Story Creation does not gain scenes, characters, assets, Composition state, or rendering behavior from this integration.
- **AC-16:** Model selection remains configuration-driven so a currently supported Gemini model can replace the requested but retired `gemini-2.0-flash` without changing application/domain contracts.

## 31. Test scenarios

### Architecture/boundary
- **T-AI-01:** StoryService compiles/tests without Spring AI types.
- **T-AI-02:** StoryGenerator request/result contain no provider types.
- **T-AI-03:** Gemini adapter is the only Story Creation implementation importing Spring AI/Google-specific APIs.

### Configuration
- **T-AI-04:** Fake mode starts without Gemini credentials.
- **T-AI-05:** Gemini mode binds model/configuration correctly.
- **T-AI-06:** Missing required Gemini credentials/configuration produces a controlled startup/runtime configuration error.
- **T-AI-07:** No duplicate `StoryGenerator` bean exists under each supported configuration.

### Prompt and output
- **T-AI-08:** Prompt includes idea, target age, duration, visual style and language.
- **T-AI-09:** Valid structured model response maps to title/draft.
- **T-AI-10:** Malformed JSON/structured response fails safely.
- **T-AI-11:** Missing title/draft fails safely.
- **T-AI-12:** No scenes/characters are introduced into StoryGenerationResult.

### Failure/retry
- **T-AI-13:** Timeout becomes controlled generation failure.
- **T-AI-14:** Transient provider failure retries within the configured bound.
- **T-AI-15:** Non-retryable provider failure is not retried.
- **T-AI-16:** Structured-output correction retries remain bounded.
- **T-AI-17:** Retry usage/latency is observable without sensitive payload logging.

### Security/observability
- **T-AI-18:** Secrets never appear in source/config/test fixtures.
- **T-AI-19:** Error responses do not expose provider credentials or raw exceptions.
- **T-AI-20:** Logs contain operation/model/duration/failure category but not raw prompts or secrets.

### Application regression
- **T-AI-21:** Existing StoryService tests continue to pass with FakeStoryGenerator.
- **T-AI-22:** Blank Story behavior remains unchanged.
- **T-AI-23:** Story Creation API contract remains unchanged.

### Optional manual integration
- **T-AI-24:** Credentialed Gemini smoke test succeeds only when explicitly enabled and configured.

## 32. Known compatibility concern requiring explicit follow-up

The requested `gemini-2.0-flash` model is no longer a viable live target as of this specification date because Google documents shutdown on June 1, 2026. Spring AI 2.0.1 still contains API/documentation support references for the Gemini 2.0 model family, but provider availability is the controlling constraint. citeturn0search1turn2search0

**Implementation blocker:** select an actively supported Gemini model before implementing `GeminiStoryGenerator`. Do not silently substitute a model in application code.

## 33. Out of scope

- Implementing Spring AI dependencies.
- Implementing `GeminiStoryGenerator`.
- Changing `StoryGenerator`.
- Changing StoryService behavior.
- Changing Story Creation UI/API behavior.
- Adding real Gemini credentials.
- Adding GitHub Actions secrets.
- Real Gemini CI calls.
- Story Outline.
- Character/scene/background generation.
- Image/audio/video generation.
- Composition generation.
- RAG/vector stores.
- MCP/tool calling.
- Agent workflows.
- AI billing persistence.
- Production deployment changes.
- Database migrations.

## 34. Dependencies

Existing:

- Java 21.
- Spring Boot 4.1.1.
- Existing StoryGenerator abstraction.
- Existing Story Creation tests.
- Existing API error envelope.
- Existing provider-independent AI architecture.

Future implementation dependency:

- Spring AI 2.0.1 dependency management.
- Spring AI Google GenAI starter.
- A currently supported Gemini model selected explicitly before implementation.
- External credentials supplied only at runtime.

## 35. Related decisions

- ADR-003 — AI proposes suggestions/assets, not canonical project state.
- ADR-004 — AI providers are abstracted.
- ADR-011 — Avoid premature infrastructure.
- ADR-012 — SDD + TDD and durable feature implementation memory.
- ADR-013 — Spring AI is an infrastructure mechanism behind provider-independent AI boundaries.

No Story Creation API or Composition decision is changed by this specification.
