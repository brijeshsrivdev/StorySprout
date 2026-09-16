# Characters — Implementation

**Feature:** Characters  
**Specification:** `docs/specifications/characters.md` (SPEC-004)  
**Status:** IMPLEMENTED — final CI validation pending

## Feature purpose

Characters establishes a practical Project-scoped reusable character library and explicit Story membership before Scene Setup. Characters are canonical creator-owned planning/asset-reference records; no visual asset pipeline or Composition data is created.

## Specification / acceptance criteria

Implemented against SPEC-004. Key behavior: create/edit/delete Project Characters, add/remove Story membership, reuse one Character across Stories in a Project, prevent deletion while referenced, deterministic placeholder avatar, AI suggestion workflow, and Characters navigation between Story Outline and the existing Scene Setup placeholder.

## User flow

`Dashboard → Story Outline → Characters → Scene Setup placeholder`

The Characters page loads Story membership and the Project library. A creator can manually create a character and add it, add an existing reusable character, edit a shared Project character, remove Story membership, or delete an unused Project character. AI suggestions populate the editable form but are not persisted until explicit creation.

## End-to-end architecture flow

```text
Next.js Characters UI
        ↓
Character REST client
        ↓
Spring Boot CharacterController
        ↓
CharacterService
   ↙              ↘
PostgreSQL       CharacterGenerator
                      ↓
                Fake or Gemini adapter
                      ↓
              existing Spring AI ChatClient
```

Characters remain outside `packages/editor-model`. Scene-level placement and Composition are untouched.

## Frontend implementation

`apps/web/app/stories/[storyId]/characters/page.tsx` provides:
- Story-specific character list.
- Project reusable character library.
- Manual create/edit form.
- Add/remove Story membership.
- Project Character deletion with reference protection.
- AI suggestion form.
- Deterministic category/name placeholder avatars.
- Empty, loading and error states.
- Explicit shared-character messaging.
- Continue to existing Scene Setup placeholder.

`apps/web/lib/api.ts` adds typed Character models and REST functions.

Story Outline's continuation now routes to Characters. The Scene Setup page remains a placeholder and only changes its return link/message.

## Backend implementation

Feature package: `com.storysprout.api.character`.

`CharacterService` owns:
- Project/Story existence checks;
- Character field validation;
- Project ownership;
- Character CRUD;
- Story membership add/remove;
- duplicate membership prevention;
- cross-project membership rejection;
- deletion conflict for referenced Characters;
- AI suggestion orchestration without persistence.

`CharacterRepository` uses JdbcTemplate for Character and Story membership persistence.

## Database / persistence

Migration:

`apps/api/src/main/resources/db/migration/V4__characters.sql`

Creates:
- `characters` — Project-owned reusable identity records;
- `story_characters` — many-to-many Story membership.

Character names are not unique within a Project. Membership has composite primary key `(story_id, character_id)`. Character deletion is rejected when memberships exist; removing Story membership never deletes the Character.

Existing migrations V1–V3 were not modified.

## AI / external services

Application boundary:

```text
CharacterGeneration.Generator.generate(CharacterGeneration.Request)
    -> CharacterGeneration.Result
```

`DeterministicCharacterGenerator` is the credential-free default for CI/local use.

`GeminiCharacterGenerator` is infrastructure-only and reuses:
- existing `ChatClient`;
- existing `GeminiAiCallExecutor`;
- existing `GeminiAiProperties`;
- existing `gemini-2.5-flash` configuration.

`GeminiCharacterPromptBuilder` supplies creator context and approved Character categories. Typed provider structured output is validated before being returned. AI returns only editable identity fields; no database IDs or media/Composition state.

## Renderer impact

None. Renderer is unchanged. No Composition, TimelineClip, SceneObject, animation, camera or render data is produced.

## API contracts

- `GET /api/v1/projects/{projectId}/characters`
- `GET /api/v1/stories/{storyId}/characters`
- `POST /api/v1/projects/{projectId}/characters`
- `PATCH /api/v1/projects/{projectId}/characters/{characterId}`
- `DELETE /api/v1/projects/{projectId}/characters/{characterId}`
- `POST /api/v1/stories/{storyId}/characters/{characterId}`
- `DELETE /api/v1/stories/{storyId}/characters/{characterId}`
- `POST /api/v1/projects/{projectId}/characters/generate`

Existing `{ data: ... }` success and `{ error: ... }` error envelopes remain authoritative.

## Data model

### Character
- id
- projectId
- name
- roleDescription
- category
- visualDescription
- personality
- createdAt
- updatedAt

### Story membership
- storyId
- characterId
- createdAt

No image URL, asset ID, scene placement, pose, animation, timing, dialogue or Composition fields are persisted.

## Error handling

- 400 — Character validation failures.
- 404 — Project, Story, or Character not found.
- 409 — cross-project membership, duplicate membership, or deletion of referenced Character.
- 422 — controlled AI generation failure.
- 500 — unexpected failures through existing handler.

AI suggestion failure does not create or modify a Character.

## Security considerations

- No authentication/authorization added.
- Character text is treated as untrusted creator/AI content.
- Input lengths are bounded in application validation and database schema.
- AI credentials remain external configuration.
- Provider prompts and generated content are not logged by the Character adapter.

## Automated tests

### Backend unit
`apps/api/src/test/java/com/storysprout/api/character/CharacterServiceTest.java` covers Project ownership, Character creation, AI suggestion non-persistence, cross-project rejection, membership removal, and referenced-character deletion protection.

### Backend integration/API
`apps/api/src/test/java/com/storysprout/api/character/CharacterControllerIntegrationTest.java` uses PostgreSQL Testcontainers/Flyway and covers Character persistence, reuse across two Stories, membership removal preserving the Character, duplicate membership conflict, and referenced deletion conflict.

### Frontend / E2E
`apps/web/tests/e2e/characters.spec.ts` covers Story Outline → Characters navigation, create/add, removal while preserving Project reuse, and AI suggestion without automatic persistence.

Existing Story Creation and Story Outline E2E suites remain part of repository regression validation.

## Known limitations

- Actual image generation/upload is intentionally not implemented; UI uses a deterministic placeholder/avatar.
- Project-level Character editing is shared across all Stories using that Character; per-Story forks are deferred.
- There is no global/account-wide library, marketplace, advanced search, versioning, or asset lifecycle.
- Scene-level placement is deferred to Scene Setup.
- Real Gemini smoke testing remains outside normal CI.

## Future improvements

- Actual Character visual asset generation/upload as a separately specified feature.
- Scene Setup Character selection and placement.
- Character variants/versioning only if a separate product decision requires them.

## Related decisions

- ADR-001 — Composition Model is source of truth.
- ADR-003 — AI proposes suggestions/assets, not canonical project state.
- ADR-004 — AI providers are abstracted.
- ADR-006 — media binaries use object storage (not used by Characters V1 placeholder).
- ADR-009 — web desktop first.
- ADR-012 — SDD/TDD and feature implementation memory.
- ADR-013 — Spring AI behind provider-independent AI boundaries.

## FILE-BY-FILE IMPLEMENTATION MAP

| File | Responsibility | Important interaction |
|---|---|---|
| `apps/api/src/main/resources/db/migration/V4__characters.sql` | Character schema | Creates Project Characters and Story membership with FKs and duplicate protection. |
| `apps/api/src/main/java/com/storysprout/api/character/Character.java` | Domain record | Canonical reusable Project Character. |
| `apps/api/src/main/java/com/storysprout/api/character/CharacterCategory.java` | Domain enum | Approved V1 categories. |
| `apps/api/src/main/java/com/storysprout/api/character/CharacterRequests.java` | API input contracts | Create/update/generation request shapes. |
| `apps/api/src/main/java/com/storysprout/api/character/CharacterGeneration.java` | AI capability boundary | Provider-independent request/result/generator contract. |
| `apps/api/src/main/java/com/storysprout/api/character/CharacterRepository.java` | Persistence | Character CRUD and Story membership queries/mutations. |
| `apps/api/src/main/java/com/storysprout/api/character/CharacterService.java` | Application service | Validation, ownership, reuse, membership, deletion protection and AI suggestion orchestration. |
| `apps/api/src/main/java/com/storysprout/api/character/CharacterResponse.java` | API response | Serializes persisted Character identity. |
| `apps/api/src/main/java/com/storysprout/api/character/CharacterController.java` | REST API | Exposes Project/Story Character and generation endpoints. |
| `apps/api/src/main/java/com/storysprout/api/character/CharacterNotFoundException.java` | Error handling | Controlled 404. |
| `apps/api/src/main/java/com/storysprout/api/character/CharacterConflictException.java` | Error handling | Controlled 409. |
| `apps/api/src/main/java/com/storysprout/api/character/CharacterGenerationException.java` | Error handling | Controlled 422 AI failure. |
| `apps/api/src/main/java/com/storysprout/api/character/DeterministicCharacterGenerator.java` | AI fake | Credential-free deterministic CI/default provider. |
| `apps/api/src/main/java/com/storysprout/api/ai/infrastructure/google/GeminiCharacterGenerator.java` | Gemini adapter | Reuses ChatClient/executor/config and validates typed output. |
| `apps/api/src/main/java/com/storysprout/api/ai/infrastructure/google/GeminiCharacterPromptBuilder.java` | Gemini prompt | Builds provider-specific Character generation prompt. |
| `apps/api/src/main/java/com/storysprout/api/ai/infrastructure/google/GeminiCharacterResponse.java` | Gemini DTO | Infrastructure-only structured response. |
| `apps/api/src/main/java/com/storysprout/api/story/ApiExceptionHandler.java` | Shared errors | Maps Character 404/409/422 to existing API envelope. |
| `apps/web/lib/api.ts` | Frontend API client | Typed Character REST operations. |
| `apps/web/app/stories/[storyId]/outline/page.tsx` | Navigation | Continues Story Outline to Characters. |
| `apps/web/app/stories/[storyId]/characters/page.tsx` | Characters UI | Story membership, reusable library, CRUD and AI suggestion workflow. |
| `apps/web/app/stories/[storyId]/scene-setup/page.tsx` | Navigation placeholder | Receives Characters continuation without implementing Scene Setup. |
| `apps/web/tests/e2e/characters.spec.ts` | E2E tests | Proves creator navigation, create/reuse/removal and AI non-persistence. |
| `apps/api/src/test/java/com/storysprout/api/character/CharacterServiceTest.java` | Unit tests | Service ownership/membership/AI rules. |
| `apps/api/src/test/java/com/storysprout/api/character/CharacterControllerIntegrationTest.java` | Integration tests | PostgreSQL/Flyway/API persistence and membership proof. |
| `docs/specifications/characters.md` | Specification | SPEC-004 source of truth. |
| `docs/features/FEATURE_INDEX.md` | Feature memory | Tracks implementation status. |
| `docs/project/current-state.md` | Project memory | Records current implementation/validation state. |
