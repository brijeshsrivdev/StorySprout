# Characters — Implementation

**Feature:** Characters  
**Specification:** `docs/specifications/characters.md` (SPEC-004)  
**Status:** IMPLEMENTED — functional behavior validated previously; UI foundation milestone pending local execution

## Feature purpose

Characters establishes a practical Project-scoped reusable character library and explicit Story membership before Scene Setup. Characters are canonical creator-owned planning/asset-reference records; no visual asset pipeline or Composition data is created.

## UI foundation milestone

The Characters experience now presents the existing domain as a reusable creative cast rather than an administrative record list.

The visual treatment:
- uses the shared StorySprout UI foundation and design tokens;
- presents Story membership and the reusable Project library as distinct creative surfaces;
- uses deterministic initials/category placeholder visuals instead of emoji, external image URLs, or generated images;
- keeps the five canonical V1 fields focused and approachable;
- makes shared Project-character editing explicit;
- treats AI output as an editable suggestion with an explicit review/create step;
- keeps destructive membership/library actions visually distinct and preserves existing server-side safety behavior;
- provides loading, empty, failure, saving and AI-generation states with accessible semantics;
- adapts the character cards and creation workflow for desktop, tablet and narrow layouts.

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
- Story-specific character list with clear `In this Story` membership context.
- Project reusable Character library with explicit `Project Cast` context.
- Intentional deterministic placeholder visuals based on character initials/category.
- Manual create/edit form using shared `Field`, `Input`, `Textarea`, `Select` and `Button` primitives.
- Add/remove Story membership.
- Project Character deletion with reference protection.
- AI suggestion flow: Describe → Suggest → Review → Create.
- AI suggestion status and retry without automatic persistence.
- Shared-character warning before editing/saving.
- Loading, empty, API failure, save, delete/membership failure and generation states.
- Responsive card/form layout and keyboard-visible focus treatment.
- Continue to existing Scene Setup placeholder.

`apps/web/lib/api.ts` retains the existing typed Character REST operations and contracts; no API client contract was changed.

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

No API contract or backend behavior changed in this UI milestone.

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

The UI surfaces the existing API status/error messages without weakening server-side rules:
- 400 — Character validation failures.
- 404 — Project, Story, or Character not found.
- 409 — cross-project membership, duplicate membership, or deletion of referenced Character.
- 422 — controlled AI generation failure.
- 500 — unexpected failures through existing handler.

## Security considerations

No authentication/authorization, provider credentials, or persistence semantics were changed.

## Automated tests

### Frontend / E2E
`apps/web/tests/e2e/characters.spec.ts` now covers:
- Character library and empty state;
- manual create with canonical fields and category selection;
- edit of a shared Project Character;
- remove Story membership while retaining reusable library membership;
- delete after membership removal;
- AI suggestion remaining non-persistent until explicit creation;
- form validation and keyboard-accessible category selection.

Backend unit/integration coverage remains unchanged because this milestone changes presentation and frontend interaction only.

## Known limitations

- Local frontend execution is unavailable in the current environment because repository/build services cannot be reached.
- Actual image generation/upload remains intentionally unimplemented; UI uses deterministic placeholder visuals.
- Project-level Character editing is shared across all Stories using that Character; per-Story forks are deferred.
- No global/account-wide library, marketplace, advanced search, versioning, or asset lifecycle exists.

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
| `apps/web/app/stories/[storyId]/characters/page.tsx` | Characters creative library UI | Reads existing Character APIs; presents Story membership, reusable Project library, CRUD and AI suggestion workflow without changing canonical contracts. |
| `apps/web/lib/api.ts` | Frontend API client | Existing Character types/endpoints remain unchanged. |
| `apps/web/tests/e2e/characters.spec.ts` | Characters UI E2E | Verifies library, create/edit/remove/delete, AI suggestion, validation and accessibility interactions. |
| `apps/api/src/main/java/com/storysprout/api/character/CharacterService.java` | Application service | Existing ownership, membership, reuse and deletion safety remain authoritative. |
| `apps/api/src/main/java/com/storysprout/api/character/CharacterController.java` | REST API | Existing Character endpoints remain unchanged. |
| `apps/api/src/main/resources/db/migration/V4__characters.sql` | Character persistence | Existing Project/Story ownership model remains unchanged. |
| `docs/specifications/characters.md` | Specification | SPEC-004 source of truth. |

## P1 remediation — shared accessibility and membership actions

The P1 UI remediation keeps `Field` responsible for label/control association and removes the unusable library-delete action from Story Character cards. Story membership continues to use `Remove from Story`; Project Character Library retains deletion only where the existing domain rules allow it. Backend deletion protection and Character contracts are unchanged.
