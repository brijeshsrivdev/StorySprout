# Feature Specification — Characters

**Specification ID:** SPEC-004  
**Status:** SPECIFIED — implementation not started

## 1. Problem

StorySprout currently takes a creator from Story Creation into Story Outline. The next creator need is to establish the recurring characters that will appear in the story before visual Scene Setup begins.

Characters are reusable story assets. A creator should be able to define a character once, reuse that character in multiple scenes of the same story, and retain the character in a practical reusable library so it can be used again in future stories/episodes within the same project.

The current repository has no Character domain, Character API, Character persistence, or Character UI. Story Outline is implemented as a planning layer and does not contain character data. The future Scene Setup stage is currently only a navigation placeholder. SPEC-004 must therefore add a character-planning/asset-reference layer without creating Scene Setup or Composition semantics.

## 2. Goal

Allow a creator to:

1. open Characters from a completed Story Outline;
2. see the characters associated with the Story;
3. create a character manually;
4. optionally ask AI to suggest a character definition;
5. edit and accept the suggestion;
6. reuse an existing Project character in the Story;
7. edit or remove a Story's character usage without deleting the reusable Project character;
8. keep character identity and visual description available for the future Scene Setup stage.

Characters must remain creator-controlled canonical project data. AI may suggest character information, but it must not create hidden canonical state.

## 3. Product flow

The approved V1 creator journey remains:

```text
Dashboard
  ↓
Create Story
  ↓
Story Setup
  ↓
Story Outline
  ↓
Characters
  ↓
Scene Setup
  ↓
Editor / Timeline
  ↓
Composition
  ↓
Preview
  ↓
Render
```

SPEC-004 owns only the Characters step and the minimum navigation required to enter it and leave it toward the existing Scene Setup placeholder.

## 4. V1 reusable-character decision

### Decision

**Characters are Project-scoped reusable records, with an explicit Story membership/usage relationship.**

A Character belongs to the Project's reusable character library, not directly to only one Story.

A Story selects which Project characters it uses. The same Character can therefore be associated with multiple Stories in the same Project without duplicating its identity record.

This is the practical V1 compromise between story-local records and a full asset-management system:

- **Persist now:** reusable Character identity and Story membership.
- **Reuse now:** the same Character record can be used by multiple scenes and multiple Stories in the same Project.
- **Defer:** global account-wide character libraries, marketplace/sharing, advanced asset versioning, visual generation pipelines, character rigging, and cross-project asset transfer.

### Why Project scope

A Story-local Character would make reuse across future episodes require duplication or migration. A global library would introduce account ownership, search, sharing, lifecycle, and asset-management concerns before StorySprout has those concepts. Project scope supports a small implementation while preserving the intended reuse model.

### Ownership rules

- `Project` owns the reusable Character record.
- `Story` references Characters through a membership/usage relation.
- Deleting a Story-character membership does **not** delete the Project Character.
- Deleting a Project Character is allowed only when it is not referenced by any Story, or must be explicitly rejected with a controlled conflict if references exist. V1 should prefer rejection rather than silently removing the character from multiple Stories.
- A Character has one canonical identity record within a Project.

## 5. User flow

### 5.1 Entry from Story Outline

The Story Outline screen adds the smallest continuation change required:

```text
Story Outline → Characters
```

The Characters route is story-specific, for example:

`/stories/{storyId}/characters`

The Characters page receives the Story ID and resolves the associated Project through the Story API. It does not require the creator to manually select a Project.

The existing Story Outline page currently continues to the Scene Setup placeholder. SPEC-004 replaces only that continuation destination with Characters and adds the Characters → Scene Setup continuation. The Scene Setup page itself remains a placeholder and is not implemented as a feature.

### 5.2 Characters page

On entry:

1. Load the Story and its current character membership.
2. Load the Project character library needed for selection/reuse.
3. Show characters already used by the Story first.
4. Allow the creator to add an existing Project Character to the Story.
5. Allow the creator to create a new Character in the Project and associate it with the Story.
6. Allow editing the character definition.
7. Allow removing a Character from this Story without deleting it from the Project library.
8. Allow deletion of an unused Project Character from the library.
9. Continue to the future Scene Setup placeholder only after the creator has a valid Characters state.

### 5.3 Character creation paths

V1 provides two creation paths:

- **Create manually** — creator enters the character fields.
- **Create with AI** — creator supplies a short character idea/context and receives a structured suggestion that is shown as an editable draft before persistence.

AI generation does not create a visual image in V1.

### 5.4 Reuse

Within a Story, a Character is selected once and is available to future Scene Setup work for any scene in that Story.

There is no per-scene Character placement record in SPEC-004. Scene-level usage belongs to future Scene Setup.

Across Stories in the same Project, the creator can add the same Project Character to another Story. The Character identity remains shared.

## 6. Character identity model

### Required V1 fields

#### `name`

Human-readable character name.

Example: `Milo`

#### `roleDescription`

Short narrative role/description explaining who the character is in the story.

Example: `A curious young rabbit who loves solving small problems for friends.`

#### `category`

A simple category useful for creator organization and AI prompting.

Approved V1 values:

- `CHILD`
- `ADULT`
- `ANIMAL`
- `FANTASY`
- `OBJECT`
- `OTHER`

A separate numeric age is **not** required in V1. Story target age already describes the intended audience, while a character's exact age is often not meaningful for animals, fantasy characters, objects, or non-human characters. If age is useful, the creator can express it naturally in `roleDescription` or `visualDescription`.

#### `visualDescription`

Human-readable visual identity that should remain consistent when the character is later used in Scene Setup.

It may describe:

- appearance;
- colors;
- clothing;
- body/shape characteristics;
- distinctive visual traits;
- accessories that define the character's identity.

It must remain descriptive rather than becoming an image-generation prompt with camera, animation, or composition instructions.

### Optional V1 field

#### `personality`

A short optional description of personality/behavioral traits. This is useful for AI-assisted generation and future scene writing, but it is not required for a manually created character.

No other character attributes are required in V1.

## 7. Character visual asset decision

### V1 behavior

A Character is primarily a reusable identity record. **Actual character image generation/upload is not implemented in SPEC-004.**

The Characters UI displays a deterministic placeholder/avatar derived from the character category and name. The placeholder is clearly presented as a placeholder and is not treated as a final visual asset.

The Character API/data model should therefore not introduce a fake image URL or pretend that the placeholder is a generated asset.

### Future visual asset boundary

A future image-generation/asset feature may attach one or more actual visual assets to a Character. That future feature will define object-storage metadata, asset lifecycle, generation status, image variants, and replacement behavior.

SPEC-004 may expose a nullable future-facing `visualAssetId` only if the existing asset architecture already provides a stable reference contract. The current repository does not provide a Character asset contract, so **SPEC-004 does not create one**.

This keeps Characters useful now without prematurely coupling them to `packages/editor-model`, MinIO/object storage, or a future asset-management system.

## 8. Character editing model

The creator owns the canonical Character definition after creation.

Editable fields:

- name;
- roleDescription;
- category;
- visualDescription;
- personality.

AI-generated suggestions are fully editable before or after persistence.

There is no immutable AI-generated portion of a Character.

Changing a Character updates the shared Project Character definition. Because the same Character may be associated with multiple Stories, the UI should make the shared nature clear before saving edits that affect other Stories.

V1 does not implement per-Story forks/variants of a Character. If a creator needs a materially different version, they create a separate Project Character.

## 9. Story membership and reuse model

The reusable library and Story membership are distinct concepts.

Conceptually:

```text
Project
  └── Character Library
        ├── Character A
        ├── Character B
        └── Character C

Story 1 ── uses ── Character A
Story 1 ── uses ── Character B

Story 2 ── uses ── Character A
```

A Character can be used by many Stories. A Story can use many Characters.

For future Scene Setup:

```text
Story
  ↓
Story Characters
  ↓
Scene Setup selects from Story Characters
```

SPEC-004 does **not** persist scene-level placement such as position, scale, pose, animation, camera, or timeline timing.

## 10. Persistence model

Implementation should add new Flyway migration(s) without modifying existing migrations.

### `characters`

Proposed fields:

- `id` UUID primary key
- `project_id` UUID foreign key to `projects.id`
- `name` VARCHAR NOT NULL
- `role_description` TEXT NOT NULL
- `category` VARCHAR NOT NULL
- `visual_description` TEXT NOT NULL
- `personality` TEXT NULL
- `created_at` TIMESTAMPTZ NOT NULL
- `updated_at` TIMESTAMPTZ NOT NULL

Constraints:

- project must exist;
- name nonblank and bounded;
- role description nonblank and bounded;
- category must be one of the approved V1 values;
- visual description nonblank and bounded;
- personality, when present, must remain bounded;
- timestamps required.

A Project-local character name should not be globally unique. V1 should not reject two characters with the same name solely because they belong to the same Project; creators may intentionally have similarly named entities. The UI should nevertheless make duplicate names understandable.

Recommended indexes:

- `characters.project_id`;
- `characters.updated_at` if needed for library ordering.

### `story_characters`

Proposed fields:

- `story_id` UUID foreign key to `stories.id`;
- `character_id` UUID foreign key to `characters.id`;
- `created_at` TIMESTAMPTZ NOT NULL.

Primary key:

`(story_id, character_id)`

Constraints:

- both referenced records must exist;
- the Character must belong to the same Project as the Story;
- duplicate membership is prohibited;
- removing membership must not delete the Character.

The cross-project ownership rule should be enforced by application validation and, where practical, by persistence design. Do not rely on the UI alone.

### Deletion

- Story membership deletion removes only the join record.
- Character deletion must not cascade into deleting Stories.
- If a Character is used by one or more Stories, V1 should return a controlled conflict rather than silently removing all memberships.
- If a Character has no Story memberships, it may be deleted from the Project library.

No Story deletion semantics are introduced by this feature.

## 11. API contract

Base path remains `/api/v1` and the existing success/error envelope remains authoritative.

### Get Story Characters

`GET /api/v1/stories/{storyId}/characters`

Returns:

- Story reference sufficient for page context;
- characters currently associated with the Story;
- enough character data to render the Story character list.

Ordered by a stable creator-friendly order, preferably character `name ASC` with deterministic ID tie-breaking unless implementation conventions establish a different ordering.

### Get Project Character Library

`GET /api/v1/projects/{projectId}/characters`

Returns the Project's reusable Character records, excluding or marking characters already used by the current Story when a future query parameter is introduced. V1 can return the full library and let the UI indicate membership.

No generic advanced asset-library filtering/search API is required.

### Create Character

`POST /api/v1/projects/{projectId}/characters`

Creates a Project Character from creator-authored fields.

The response returns the persisted Character.

This endpoint does not automatically attach the Character to a Story unless the API contract explicitly combines creation and membership. For a clear boundary, creation and Story membership should remain separate operations.

### Update Character

`PATCH /api/v1/projects/{projectId}/characters/{characterId}`

Updates editable Character identity fields.

The server verifies that the Character belongs to the supplied Project.

### Delete Character

`DELETE /api/v1/projects/{projectId}/characters/{characterId}`

Deletes an unused Project Character.

If it is referenced by any Story, return HTTP `409 Conflict` and do not modify memberships.

### Add Character to Story

`POST /api/v1/stories/{storyId}/characters/{characterId}`

Associates an existing Project Character with the Story.

Validation must confirm:

- Story exists;
- Character exists;
- Character's Project equals Story's Project;
- membership does not already exist.

### Remove Character from Story

`DELETE /api/v1/stories/{storyId}/characters/{characterId}`

Removes only Story membership.

The reusable Project Character remains available in the Project library.

### Generate Character Suggestion

`POST /api/v1/projects/{projectId}/characters/generate`

Generates a provider-independent structured Character suggestion from creator-supplied context.

The response is a suggestion only. It does **not** create a database Character and does **not** attach anything to a Story.

The UI can then let the creator edit the suggestion and explicitly create the Character.

The request should minimally support:

- character idea/context;
- optional Story context when invoked from a Story;
- target age when available from the Story;
- language when available from the Story.

The generated result should contain only the approved Character identity fields:

- name;
- roleDescription;
- category;
- visualDescription;
- personality.

No image, animation, scene, dialogue, audio, timeline or Composition output is returned.

### Add-and-create UX

The UI may provide a convenience flow such as `Create Character and Add to Story`, but it must remain logically equivalent to:

```text
Generate/edit or manually enter Character
        ↓
Create Project Character
        ↓
Add Character to Story
```

This keeps Project ownership and Story membership explicit.

## 12. AI architecture

### Capability boundary

Introduce a separate provider-independent capability:

```text
CharacterGenerator.generate(CharacterGenerationRequest)
    -> CharacterGenerationResult
```

This must not reuse `StoryGenerator` or `StoryOutlineGenerator` for unrelated semantics.

Suggested application-level package follows the existing conventions under `com.storysprout.api.character` or equivalent feature-local structure. Exact package placement is an implementation detail, but provider-neutral contracts must remain outside Google/Spring AI infrastructure.

### Request

The request should contain only the context necessary to suggest a Character:

- creator's character idea;
- optional Story title/idea/draft context;
- target age when available;
- language;
- optionally visual style when relevant to the visual description.

Do not send the entire Project or unrelated Characters unless a later specification explicitly requires character-aware consistency generation.

### Result

```text
CharacterGenerationResult
- name
- roleDescription
- category
- visualDescription
- personality
```

The result contains no database ID, Project ID, Story ID, asset ID, scene ID, or Composition data.

### Gemini

Reuse the existing Spring AI `ChatClient`, Gemini configuration, timeout/retry infrastructure and approved `gemini-2.5-flash` model.

Conceptually:

```text
CharacterGenerator
        ↓
GeminiCharacterGenerator
        ↓
existing Spring AI ChatClient infrastructure
        ↓
Gemini 2.5 Flash
```

Spring AI/Google GenAI types remain in the infrastructure adapter. Application/domain code must not import provider-specific classes.

The existing repository already uses `ChatClient` and provider-native structured output for Gemini. SPEC-004 follows that established pattern rather than introducing another AI integration mechanism. Spring AI 2.0.1 documents typed `ChatClient.entity(...)` responses and provider-native structured output through `useProviderStructuredOutput()`. citeturn0search0turn0search1turn0search8

### Structured output

Use a typed response DTO. Do not parse arbitrary prose into Character fields.

Validate before the suggestion is returned to the UI:

- name nonblank and bounded;
- roleDescription nonblank and bounded;
- category is an approved enum value;
- visualDescription nonblank and bounded;
- personality optional but bounded;
- no provider-specific IDs or hidden state.

Malformed/invalid output becomes a controlled generation failure.

### Deterministic fake

Normal CI and standard tests must use `DeterministicCharacterGenerator`.

The fake must:

- require no credentials;
- require no network;
- return stable output for the same supported request;
- exercise the same provider-neutral result contract;
- support controlled failure in tests.

### AI does not persist

Character generation is a suggestion operation. The generator never writes PostgreSQL.

Persistence happens only after creator acceptance through the Character service/API.

## 13. Character generation UX

### Manual creation

The creator sees a form for:

- Name
- Role / description
- Category
- Visual description
- Personality (optional)

`Create Character` is disabled until required fields are valid.

### AI-assisted creation

The creator enters a short idea such as:

`A shy little fox who helps other animals find their way home.`

The UI shows a generation state and then an editable suggestion.

The creator can:

- edit any generated field;
- accept/create the Character;
- cancel the suggestion;
- retry generation only while no Character has been created from that suggestion.

V1 does not provide field-by-field AI regeneration.

### Existing character reuse

A library picker shows Project Characters. Characters already associated with the Story are clearly marked and cannot be added twice.

Selecting an existing Character adds the membership only; it does not clone the Character.

## 14. UI requirements

### Header

Show:

- Story title;
- compact Story metadata: target age, duration, visual style, language;
- current character count;
- navigation back to Story Outline;
- continuation to Scene Setup.

### Character list

Each Character card should show:

- placeholder/avatar;
- name;
- category;
- role/description;
- concise visual description;
- Story membership state;
- Edit action;
- Remove from Story action.

### Project library

A `Character Library` area or modal should show reusable Project Characters not currently used by the Story.

Actions:

- `Use in Story`
- `Create Character`
- `Create with AI`

The library should remain intentionally simple. No advanced filtering, tagging, bulk operations, asset version history, or marketplace behavior.

### Empty state

When the Story has no Characters:

- explain that Characters are reusable across scenes;
- provide `Create Character`;
- provide `Create with AI`;
- provide `Use Existing Character` if the Project library contains characters.

### Loading states

- Story loading;
- Character list loading;
- Project library loading;
- AI generation loading;
- create/update/delete operation state.

### Error states

- Story not found;
- Project/Character not found;
- failed library load;
- failed character save;
- failed membership add/remove;
- AI generation failure;
- character deletion conflict because it is used by Stories.

Errors use the existing API error envelope and user-safe messages.

### Unsaved changes

Character editing should not silently discard changes. V1 may use the browser/navigation confirmation pattern already established by Story Outline. No autosave is required.

### Continuation

`Continue to Scene Setup` navigates to:

`/stories/{storyId}/scene-setup`

The current Scene Setup page remains a placeholder. Characters does not implement Scene Setup behavior.

The button should be blocked while the current Character form has invalid/unsaved data.

A Story does not require a minimum number of Characters to exist. An empty Character list is valid because some stories can be narrated without recurring characters. The creator may continue with zero Characters.

## 15. Validation rules

### Character fields

Recommended V1 limits:

- `name`: required, trimmed, 1–100 characters;
- `roleDescription`: required, trimmed, 1–500 characters;
- `category`: required, approved enum;
- `visualDescription`: required, trimmed, 1–1000 characters;
- `personality`: optional, trimmed, maximum 500 characters.

Exact database column sizes should be consistent with these application limits.

Whitespace-only values are invalid.

### Story membership

- Story must exist.
- Character must exist.
- Character must belong to Story's Project.
- Duplicate membership is invalid; return a controlled conflict or idempotent success only if the existing API conventions support it. V1 should prefer `409 Conflict` for duplicate explicit add requests so accidental duplicates are visible.

### Delete

- Character deletion is allowed only when no Story references it.
- Removing a Character from a Story never deletes the Character.

### AI result

All generated fields must satisfy the same domain validation before being presented as an accepted Character suggestion.

## 16. Error semantics

Use existing API conventions.

Recommended status mapping:

- `400 Bad Request` — invalid Character fields or malformed request;
- `404 Not Found` — unknown Story, Project, or Character;
- `409 Conflict` — duplicate Story membership, cross-project association conflict, or deleting a Character still referenced by Stories;
- `422 Unprocessable Entity` — controlled AI generation failure/malformed generated result;
- `500 Internal Server Error` — unexpected server failure.

No provider error details, credentials, prompts, raw SDK payloads, or stack traces are returned to the browser.

## 17. Security and content-safety considerations

- No authentication/authorization is introduced by SPEC-004; this remains consistent with the current V1 foundation.
- Character fields are creator-controlled text and must be treated as untrusted input.
- AI-generated text is also untrusted and must pass the same validation as creator-authored data.
- Do not render Character descriptions as unsanitized HTML.
- Do not log API keys, authorization headers, full prompts, or full generated payloads by default.
- Do not persist provider credentials in Character records.
- Do not allow a Story to attach a Character belonging to another Project.
- Content safety policy remains a separate repository concern; SPEC-004 does not introduce a new moderation service.

## 18. Composition and renderer boundary

Characters are **not** part of `packages/editor-model` Composition schema `1.0` in SPEC-004.

No Character data is written into:

- `Composition`;
- `Scene` in the editor model;
- `SceneObject`;
- `TimelineClip`;
- animation state;
- camera state;
- renderer input;
- render jobs.

The intended future dependency is reference-only:

```text
Project Character
       ↓
Story Character membership
       ↓
future Scene Setup selects Character
       ↓
future Editor/Composition decides how that character is represented visually
```

Scene Setup may later introduce a reference from a visual scene object to a Character ID, but that is a future feature decision. SPEC-004 must not modify the Composition schema to prepare for it.

## 19. Out of scope — SPEC-004

The following are explicitly not part of Characters V1:

- Scene Setup implementation;
- scene-level Character placement;
- Composition changes;
- timeline clips;
- exact timing;
- animation timelines;
- character rigging;
- skeletal animation;
- facial mocap;
- lip-sync;
- automatic body motion;
- camera keyframes;
- dialogue generation;
- voice generation;
- audio/music/SFX;
- character image generation;
- character image upload pipeline;
- advanced asset management;
- asset versioning/variants;
- global/account-wide character library;
- marketplace/community character sharing;
- collaboration;
- character analytics;
- YouTube publishing;
- renderer implementation;
- render jobs;
- authentication/authorization;
- payments/subscriptions;
- production deployment changes;
- bulk character operations;
- character search/tagging/filtering beyond a simple library list;
- per-Story Character forks/variants;
- automatic consistency enforcement across generated images/scenes;
- automatic detection of duplicate characters;
- automatic character extraction from the Story or Story Outline.

## 20. Acceptance criteria

### Navigation

**AC-01 — Story Outline to Characters**  
A valid Story Outline can continue to `/stories/{storyId}/characters` without creating a new navigation system.

**AC-02 — Characters to Scene Setup**  
Characters can continue to the existing Scene Setup placeholder after the current Character state is valid/saved.

### Character library and membership

**AC-03 — Story character list**  
The Characters page displays the Characters currently associated with the Story.

**AC-04 — Project reuse**  
A Character created for one Story can be associated with another Story in the same Project without cloning the Character identity.

**AC-05 — Remove from Story**  
Removing a Character from a Story does not delete it from the Project library.

**AC-06 — Cross-project protection**  
A Character from another Project cannot be attached to the Story.

### CRUD

**AC-07 — Manual create**  
Creator can create a Character with all required V1 fields.

**AC-08 — Edit**  
Creator can edit all defined Character fields and persisted changes survive refresh.

**AC-09 — Delete unused Character**  
An unused Project Character can be deleted.

**AC-10 — Delete referenced Character**  
A Character referenced by a Story cannot be silently deleted; the API returns a controlled conflict and existing memberships remain intact.

### AI

**AC-11 — AI suggestion**  
Creator can request a structured Character suggestion through `CharacterGenerator`.

**AC-12 — AI suggestion is not persistence**  
AI generation alone does not create a Character record or Story membership.

**AC-13 — AI result editable**  
Every generated Character field is editable before creation.

**AC-14 — Deterministic CI**  
Normal CI uses a deterministic fake generator and does not require Gemini credentials/network access.

**AC-15 — Gemini reuse**  
The live adapter reuses the existing Spring AI/Gemini infrastructure and approved `gemini-2.5-flash` configuration without changing the existing StoryGenerator contract.

### Visual identity

**AC-16 — Placeholder visual**  
Every Character displays a clear deterministic placeholder/avatar when no real visual asset exists.

**AC-17 — No fake generated asset**  
The system does not claim that a placeholder is a generated image or persist a fabricated asset URL.

### Validation and UX

**AC-18 — Field validation**  
Invalid/blank Character fields are blocked at the UI and rejected authoritatively by the API.

**AC-19 — Loading/error states**  
Story, library, generation, save, membership, and deletion operations expose appropriate loading/error states.

**AC-20 — Empty Story**  
A Story with zero Characters is a valid state and offers create/reuse actions.

**AC-21 — Unsaved edits**  
The UI does not silently discard an in-progress Character edit when leaving the page.

### Architecture

**AC-22 — No Composition**  
SPEC-004 creates no Composition, TimelineClip, SceneObject, animation, camera, or renderer state.

**AC-23 — AI boundary**  
Application/domain code depends only on `CharacterGenerator`; Spring AI/Google GenAI remains infrastructure-only.

**AC-24 — Existing regression**  
Story Creation and Story Outline behavior remains compatible with the new Characters continuation.

## 21. Test cases

### Frontend unit/component tests

- `T-UI-01` Story Outline continuation points to Characters.
- `T-UI-02` Characters page loads Story context and current Story Characters.
- `T-UI-03` Empty Story state shows create/reuse actions.
- `T-UI-04` Character card displays name/category/role/visual description.
- `T-UI-05` Manual Character validation blocks blank/invalid required fields.
- `T-UI-06` Create Character submits the correct payload.
- `T-UI-07` Edit Character persists the changed fields.
- `T-UI-08` Remove from Story removes membership but leaves reusable library state.
- `T-UI-09` Existing Project Character can be added to Story.
- `T-UI-10` Already-used Character cannot be added twice.
- `T-UI-11` AI generation shows loading and then editable structured fields.
- `T-UI-12` AI failure preserves the creator's existing form state.
- `T-UI-13` Placeholder/avatar is shown when no visual asset exists.
- `T-UI-14` Continue to Scene Setup is blocked when an edited Character is invalid/unsaved.
- `T-UI-15` Continue to Scene Setup routes to the existing placeholder.
- `T-UI-16` Loading and error states are rendered accessibly.

### Backend/domain unit tests

- `T-BE-01` Character create validates required fields.
- `T-BE-02` Character category accepts only approved values.
- `T-BE-03` Character update validates the same field rules as create.
- `T-BE-04` Character belongs to the supplied Project.
- `T-BE-05` Story membership requires same Project ownership.
- `T-BE-06` Duplicate Story membership is rejected.
- `T-BE-07` Removing Story membership does not delete Character.
- `T-BE-08` Referenced Character deletion returns controlled conflict.
- `T-BE-09` Unused Character deletion succeeds.
- `T-BE-10` AI result is validated before Character persistence.
- `T-BE-11` AI generation alone does not persist a Character.
- `T-BE-12` AI failure produces controlled 422 behavior without partial persistence.
- `T-BE-13` Deterministic fake is stable and credential-free.

### AI adapter tests

- `T-AI-01` CharacterGenerator request/result contains no provider-specific types.
- `T-AI-02` Gemini prompt includes supplied character idea/context and relevant Story context.
- `T-AI-03` Gemini uses typed structured output.
- `T-AI-04` Valid Gemini response maps to CharacterGenerationResult.
- `T-AI-05` Missing/blank generated name is rejected.
- `T-AI-06` Missing/blank role description is rejected.
- `T-AI-07` Invalid category is rejected.
- `T-AI-08` Missing/blank visual description is rejected.
- `T-AI-09` Oversized generated fields are rejected.
- `T-AI-10` Provider exception is converted to controlled generation failure.
- `T-AI-11` Timeout is bounded and converted to controlled failure.
- `T-AI-12` Transient retry remains within the existing bounded AI retry policy.
- `T-AI-13` No credentials/prompts/full generated content are logged.
- `T-AI-14` Fake provider remains the default in normal tests.
- `T-AI-15` Existing StoryGenerator/StoryOutlineGenerator behavior is unchanged.

### Persistence/API integration tests

- `T-API-01` Create Project Character persists correctly.
- `T-API-02` List Project Characters returns reusable library records.
- `T-API-03` List Story Characters returns only that Story's memberships.
- `T-API-04` Add same-Project Character to Story succeeds.
- `T-API-05` Add cross-Project Character fails with 409/controlled error.
- `T-API-06` Remove Story membership succeeds without deleting Character.
- `T-API-07` Character update persists across refresh.
- `T-API-08` Unused Character delete succeeds.
- `T-API-09` Referenced Character delete fails and memberships remain intact.
- `T-API-10` AI suggestion endpoint returns a structured suggestion and creates no DB row.
- `T-API-11` AI generation failure creates no partial Character data.
- `T-API-12` Existing API success/error envelope remains unchanged.

### Database/Flyway tests

- `T-DB-01` New Character migration applies cleanly after all existing migrations.
- `T-DB-02` Existing Flyway migrations remain unchanged.
- `T-DB-03` Character Project foreign key is enforced.
- `T-DB-04` Story Character membership primary key prevents duplicates.
- `T-DB-05` Character deletion is protected while memberships exist.
- `T-DB-06` Removing membership leaves Character intact.
- `T-DB-07` Character fields and category constraints reject invalid persistence values.

### E2E tests

- `T-E2E-01` Story Outline → Characters navigation loads the correct Story.
- `T-E2E-02` Create Character manually → refresh → Character remains available.
- `T-E2E-03` Add existing Project Character to a Story → membership persists.
- `T-E2E-04` Remove Character from Story → Character remains in Project library.
- `T-E2E-05` Edit shared Character → updated definition is shown after refresh.
- `T-E2E-06` AI Character suggestion → edit generated fields → create → Story membership.
- `T-E2E-07` AI failure → recover without losing creator-entered context.
- `T-E2E-08` Continue Characters → existing Scene Setup placeholder.
- `T-E2E-09` Story Creation regression remains green.
- `T-E2E-10` Story Outline regression remains green.

## 22. Implementation implications

Implementation should follow the repository's established feature-local structure and SDD/TDD sequence:

```text
Specification
  ↓
Acceptance Criteria
  ↓
Test Cases
  ↓
Failing Tests
  ↓
Implementation
  ↓
Passing Tests
  ↓
Refactor
  ↓
Validation
  ↓
Feature Implementation Documentation
  ↓
Current-State Update
```

Likely implementation areas are:

### Frontend

- Story Outline continuation link;
- `apps/web/app/stories/[storyId]/characters/page.tsx`;
- Character UI state/components as needed;
- typed API client additions;
- frontend unit/component tests;
- Characters E2E tests.

Do not introduce a new frontend design system.

### Backend

- Character domain and service;
- Character repository;
- Story Character membership repository/service behavior;
- Character REST controller/DTOs;
- CharacterGenerator application boundary;
- deterministic fake;
- Gemini infrastructure adapter;
- controlled errors and existing API envelope integration;
- backend unit/integration tests.

Do not modify the existing StoryGenerator or StoryOutlineGenerator contracts.

### Database

- New Flyway migration only;
- no edits to `V1__foundation.sql`, `V2__story_creation.sql`, or `V3__story_outline.sql`;
- PostgreSQL integration coverage.

### Renderer / Composition

No implementation changes expected.

### Documentation after implementation

Once actually implemented and validated, create:

`docs/features/characters/implementation.md`

with the repository-required file-by-file implementation map and update `docs/features/FEATURE_INDEX.md` and `docs/project/current-state.md`.

Do not create the implementation document during the specification-only phase because it would describe unimplemented functionality.

## 23. Regression expectations

SPEC-004 must preserve the existing validated behavior of:

- Story Creation;
- Story Creation AI/Blank flows;
- Story Outline generation;
- Story Outline CRUD/reorder/duration planning;
- Story Outline's planning-only boundary;
- existing Spring AI/Gemini provider abstraction;
- existing Composition Model `1.0`;
- renderer build/runtime contract.

The only intentional workflow change is the continuation path:

```text
Story Outline → Characters → existing Scene Setup placeholder
```

No Story Outline data should be rewritten as part of Characters.

## 24. Decisions intentionally deferred

The following are deliberately left for later specifications rather than guessed now:

1. How a Character becomes a visual `SceneObject` in Composition.
2. How generated character images are stored and versioned.
3. Whether multiple visual variants can exist for one Character.
4. How Character consistency is maintained across generated images.
5. How Scene Setup references Character IDs.
6. Whether a future global library spans multiple Projects.
7. Whether Character personality should influence future dialogue generation.
8. Whether Characters can be copied between Projects.

These are not blockers for SPEC-004 because the V1 Character record is a reusable identity/planning asset, not a rendered visual object.

## 25. Summary of V1 scope

**In scope:**

- Story Outline → Characters navigation;
- Story-specific Character list;
- Project-scoped reusable Character library;
- Story ↔ Character membership;
- manual Character create/edit/delete;
- add/remove reusable Character from Story;
- AI-assisted Character suggestion;
- deterministic fake CharacterGenerator;
- Gemini adapter through existing Spring AI infrastructure;
- typed structured Character generation result;
- creator editing of all generated fields;
- deterministic placeholder/avatar behavior;
- PostgreSQL persistence and Flyway migration;
- REST API;
- validation;
- loading/empty/error states;
- Characters → existing Scene Setup placeholder navigation;
- Story Creation and Story Outline regression coverage.

**Not in scope:** visual generation, advanced assets, Scene Setup, Composition, animation, audio, dialogue, rendering, publishing, collaboration, marketplace, authentication, or production infrastructure.

**Architectural boundary:**

```text
Story Outline
      ↓
Characters
      ↓
Scene Setup
      ↓
Editor / Timeline
      ↓
Composition
      ↓
Renderer
```

Characters remain reusable Project data and do not become Composition data in SPEC-004.
