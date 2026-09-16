# AI Provider Integration — Implementation Memory

**Status:** IMPLEMENTED — validation pending CI result
**Specification:** `docs/specifications/ai-provider-integration.md` (SPEC-002)
**Model decision:** `gemini-2.5-flash`
**Spring AI:** 2.0.1

## Feature purpose

Introduce a real Gemini story-generation adapter behind the existing provider-independent `StoryGenerator` boundary. The feature connects Story Creation's existing AI mode to Spring AI 2.0.1 and Google GenAI without changing the Story Creation request/result contract or introducing Story Outline, scenes, characters, assets, Composition, timeline, or rendering behavior.

## Specification reference

- `docs/specifications/ai-provider-integration.md`
- `SPEC-002`

The approved model decision supersedes the earlier compatibility blocker: `gemini-2.5-flash` is the initial model. `gemini-2.0-flash` and `gemini-2.0-flash-001` are not used.

## Acceptance criteria reference

The implementation targets AC-01 through AC-17 from SPEC-002, with the approved `gemini-2.5-flash` model satisfying the previously blocked model-selection criterion.

## User flow

1. Creator selects **Start with AI** in the existing Story Creation flow.
2. Creator provides the existing Story Setup fields.
3. `StoryService` creates the story in `GENERATING` state.
4. `StoryService` calls the existing `StoryGenerator` interface.
5. Spring selects `GeminiStoryGenerator` only when `storysprout.ai.provider=gemini`.
6. The adapter builds a provider-specific prompt containing the complete Story Generation setup.
7. Spring AI `ChatClient` requests a typed `GeminiStoryResponse` using provider structured output.
8. The adapter validates title and draft and converts them to `StoryGenerationResult`.
9. `StoryService` persists `COMPLETED` generation exactly as before.
10. Provider, timeout, malformed-output, or exhausted transient failures become the existing controlled `StoryGenerationException`, and Story Creation persists `FAILED` exactly as before.

## End-to-end architecture flow

```text
StoryService
    |
    v
StoryGenerator
    |------------------------------+
    |                              |
    v                              v
DeterministicStoryGenerator   GeminiStoryGenerator
                                   |
                                   v
                            GeminiStoryPromptBuilder
                                   |
                                   v
                              Spring AI ChatClient
                                   |
                                   v
                         Google GenAI / Gemini
                                   |
                                   v
                            gemini-2.5-flash
```

Application/domain code remains unaware of Spring AI, Google GenAI, credentials, and provider response types.

## Frontend implementation

No frontend code changed. The existing Story Creation UI continues to send the same Story Creation request and consumes the same response contract.

## Backend implementation

### StoryGenerator boundary

The existing interface remains unchanged:

```java
StoryGenerationResult generate(StoryGenerationRequest request)
```

`StoryService` was not changed. It continues to depend only on `StoryGenerator` and provider-neutral Story Creation types.

### Provider selection

`DeterministicStoryGenerator` is active by default when `storysprout.ai.provider` is `fake` or absent. `GeminiStoryGenerator` is active only when `storysprout.ai.provider=gemini`. This prevents ambiguous `StoryGenerator` beans and keeps normal CI credential-free.

### Gemini adapter

`GeminiStoryGenerator` owns:
- Spring AI `ChatClient` interaction.
- Prompt invocation.
- Typed structured response mapping.
- Result validation.
- Timeout handling.
- Transient/permanent failure classification.
- Bounded retry behavior.
- Safe operational logging.

### Prompt construction

`GeminiStoryPromptBuilder` validates the existing Story Creation constraints before building the prompt:
- target age: `3_5`, `6_8`, `9_12`
- duration: `1`, `3`, `5`
- visual style: `2D`, `3D`, `HYBRID`
- language: `ENGLISH`, `HINDI`

It includes the story idea and all four setup dimensions in a testable infrastructure-level prompt.

### Structured output

The adapter calls:

```text
ChatClient
  .prompt()
  .user(prompt)
  .call()
  .entity(GeminiStoryResponse.class, spec -> spec.useProviderStructuredOutput())
```

Provider-native structured output is used. Schema self-correction is deliberately not enabled in this slice so structured-output correction calls cannot multiply the bounded transport retry budget.

The response DTO is infrastructure-only:

```text
GeminiStoryResponse
├── title
└── draft
```

Missing, null, or blank values are rejected before conversion to `StoryGenerationResult`.

### Reliability

The Google GenAI model's Spring AI retry layer is configured to one attempt. StorySprout's adapter owns the interactive-generation retry budget and allows at most two adapter attempts. This prevents nested Spring AI transport retries from multiplying the adapter retry budget.

The adapter retries only `TransientAiException`. `NonTransientAiException`, timeout, validation failure, and other provider/runtime failures are not retried.

Every provider call is executed through `GeminiAiCallExecutor` with a finite timeout (default 20 seconds). Timed-out work is cancelled and converted to a controlled `StoryGenerationException`.

### Observability

The adapter logs only safe operational fields:
- provider
- model
- duration
- attempt count
- success/failure category
- timeout events

Creator prompts, generated stories, credentials, authorization headers, and raw provider payloads are not logged.

## Database/persistence

No database migration was added. No AI credentials or provider configuration is persisted in Story records.

Existing Story Creation persistence remains authoritative for generation state and generated title/draft.

## AI/external services

### Spring AI

Spring AI 2.0.1 is imported through the Spring AI BOM. The Google GenAI starter is:

```text
org.springframework.ai:spring-ai-starter-model-google-genai
```

Spring AI 2.0.1 documents compatibility with Spring Boot 4.1.x and exposes `gemini-2.5-flash` as a supported Google GenAI chat model.

### Authentication

Gemini Developer API is supported through the Spring AI property:

```text
spring.ai.google.genai.api-key
```

Vertex AI remains supported through:

```text
spring.ai.google.genai.project-id
spring.ai.google.genai.location
spring.ai.google.genai.credentials-uri
```

No credentials are committed. The application/domain layer does not branch on authentication mode.

### Model configuration

The initial model is configured as:

```text
spring.ai.google.genai.chat.model=${GEMINI_MODEL:gemini-2.5-flash}
```

The model can therefore be changed without changing `StoryGenerator` or `StoryService`.

## Renderer impact

None. Gemini produces only Story Creation title/draft content. No Composition JSON, scene objects, timeline clips, render instructions, or media binaries are generated.

## API contracts

No API endpoint or DTO changed.

The existing endpoint remains:

```text
POST /api/v1/projects/{projectId}/stories
```

## Data model

No data model or Flyway migration changed.

## Error handling

Provider-specific failures are translated to the existing `StoryGenerationException`. Browser-visible Story Creation behavior remains the existing `GENERATING → COMPLETED` or `GENERATING → FAILED` flow.

Raw SDK/provider messages are not propagated because they may contain credentials, headers, payload details, or implementation-specific information.

## Security considerations

- No API key, credential, or service-account JSON is stored in source control.
- No Gemini secret is added to GitHub Actions.
- Normal tests use mocks/deterministic generation and do not call Gemini.
- Creator story ideas are treated as untrusted content and are not written to logs by the AI adapter.
- Provider errors are sanitized before reaching the API layer.

## Automated tests

Added/updated tests cover:
- Gemini timeout and default retry configuration.
- Model configuration default.
- Prompt construction and all Story Generation fields.
- Exact approved target-age validation.
- Typed ChatClient response mapping.
- Successful `StoryGenerationResult` conversion.
- Null/blank title and draft rejection.
- Timeout conversion.
- Transient retry and retry bound.
- Permanent failure without retry.
- Provider error sanitization.
- Fake provider selection and single active `StoryGenerator` behavior.

Normal CI remains credential-free and does not call the live Gemini service.

## Known limitations

- Real Gemini API validation requires a runtime Gemini Developer API key or Vertex AI credentials and was not embedded into CI.
- The current environment cannot execute the repository locally because outbound repository/network access is unavailable to the execution container. GitHub Actions is therefore the authoritative post-push validation path.
- Token usage metrics are left to Spring AI's existing observability metadata; Story domain billing fields were not introduced.

## Future improvements

- Add an explicit opt-in/manual real-provider smoke test workflow if needed.
- Add richer AI usage metrics after a separate product/observability requirement.
- Add other capability-specific AI adapters only through separate specifications.

## Related decisions

- ADR-003 — AI proposes suggestions/assets, not project state.
- ADR-004 — AI providers are abstracted.
- ADR-013 — Spring AI is infrastructure behind provider-independent AI boundaries.

## FILE-BY-FILE IMPLEMENTATION MAP

| File | Layer | Responsibility | Important behavior | Dependencies / interactions | Why it belongs to this feature |
|---|---|---|---|---|---|
| `apps/api/pom.xml` | Build | Adds Spring AI 2.0.1 BOM and Google GenAI starter | Keeps Spring AI versions aligned | Spring Boot 4.1.1, Spring AI BOM | Enables the approved provider integration |
| `apps/api/src/main/resources/application.yml` | Configuration | Externalizes provider, model, authentication and reliability settings | Defaults to fake provider; Gemini model defaults to `gemini-2.5-flash`; secrets are environment-backed | Spring Boot configuration, Spring AI Google GenAI properties | Provides runtime provider/model selection without domain coupling |
| `apps/api/src/main/java/com/storysprout/api/story/DeterministicStoryGenerator.java` | Application/AI boundary | Existing credential-free generator | Now conditional so it is the default/fake provider and cannot coexist with Gemini | `StoryGenerator` | Preserves normal CI/local behavior and prevents bean ambiguity |
| `apps/api/src/main/java/com/storysprout/api/ai/infrastructure/google/GeminiAiProperties.java` | Infrastructure configuration | Bounds Gemini timeout and adapter retry budget | Timeout must be positive; adapter attempts are capped at two | Spring Boot `@ConfigurationProperties` | Keeps operational reliability settings outside domain logic |
| `apps/api/src/main/java/com/storysprout/api/ai/infrastructure/google/GeminiAiConfiguration.java` | Infrastructure configuration | Builds the Spring AI `ChatClient` only for Gemini mode | Uses Spring's auto-configured `ChatClient.Builder` | Spring AI ChatClient | Keeps Spring AI wiring inside the adapter boundary |
| `apps/api/src/main/java/com/storysprout/api/ai/infrastructure/google/GeminiAiCallExecutor.java` | Infrastructure reliability | Applies finite execution timeout | Uses Java 21 virtual threads and cancels timed-out calls | Java concurrency | Prevents indefinite provider blocking |
| `apps/api/src/main/java/com/storysprout/api/ai/infrastructure/google/GeminiStoryPromptBuilder.java` | Infrastructure prompt | Validates setup and constructs the Gemini prompt | Uses all current Story Generation fields; rejects arbitrary target ages | `StoryGenerationRequest`, `StoryGenerationException` | Keeps provider prompt construction isolated and testable |
| `apps/api/src/main/java/com/storysprout/api/ai/infrastructure/google/GeminiStoryResponse.java` | Infrastructure DTO | Holds typed Gemini title/draft response | Never crosses the application boundary | Spring AI structured mapping | Prevents provider response types from entering Story domain code |
| `apps/api/src/main/java/com/storysprout/api/ai/infrastructure/google/GeminiStoryGenerator.java` | Infrastructure adapter | Implements the real Gemini `StoryGenerator` | ChatClient call, structured output, validation, timeout, bounded transient retry, sanitized failures and safe logs | `StoryGenerator`, `ChatClient`, prompt builder, executor, properties | Isolates all Gemini/Spring AI knowledge behind the approved boundary |
| `apps/api/src/test/java/com/storysprout/api/ai/infrastructure/google/GeminiAiPropertiesTest.java` | Test | Verifies reliability configuration bounds | Confirms default timeout/retry and rejects excessive retry budget | GeminiAiProperties | Proves configuration is finite |
| `apps/api/src/test/java/com/storysprout/api/ai/infrastructure/google/GeminiStoryGeneratorTest.java` | Test | Exercises adapter behavior without network | Covers validation, success, timeout, transient/permanent failures, retry bounds and sanitized errors | Mockito, Gemini adapter | Proves core reliability and mapping behavior |
| `apps/api/src/test/java/com/storysprout/api/ai/infrastructure/google/GeminiStoryGeneratorChatClientTest.java` | Test | Verifies actual ChatClient fluent interaction | Confirms complete prompt is sent and typed response mapping is requested | Mockito, Spring AI ChatClient API | Proves the adapter uses ChatClient rather than a provider SDK directly |
| `apps/api/src/test/java/com/storysprout/api/ai/infrastructure/google/StoryGeneratorSelectionTest.java` | Test | Verifies fake-provider selection | Ensures exactly one StoryGenerator is active in fake mode | Spring Boot ApplicationContextRunner | Prevents bean ambiguity and credential requirements in normal CI |
