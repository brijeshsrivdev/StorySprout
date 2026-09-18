# Scene Setup — Implementation

**Feature:** Scene Setup  
**Specification:** `docs/specifications/scene-setup.md` (SPEC-005)  
**Status:** IMPLEMENTED — UI foundation milestone

## UI foundation milestone
Scene Setup now presents the existing preparation model as a lightweight staging board. The page remains pre-editor preparation: it does not edit Outline duration, create Composition data, introduce AI, or add timeline/animation/audio behavior.

### Experience
- Strong read-only scene identity: order, title, summary and planned duration from Outline.
- Subtle `Outline → Characters → Scene Setup → Editor` progression context.
- Background preset tiles with accessible selection semantics and deterministic visual treatment.
- Story Character cast cards with one-instance-per-character-per-scene semantics.
- Scene-local prop instances with duplicate presets explicitly supported.
- Dialogue presented as ordered conversational lines with Character/Narrator distinction.
- Action intent presented as lightweight character/action rows, explicitly framed as intent rather than animation.
- Explicit Save Changes for background, characters, dialogue and actions; prop add/remove retains the existing approved immediate persistence behavior.
- Save/unsaved/saving feedback, recoverable loading/error states and preservation of local edits after save failure.
- Primary Editor handoff remains the existing `/editor?scene=` navigation.
- No AI treatment is introduced on this screen.
- Responsive layout uses the existing Tailwind foundation; selectable controls expose keyboard focus and `aria-pressed`.

## Existing architecture preserved
No backend/API contracts, database schema, domain models, Composition semantics, renderer behavior or Editor implementation were changed.

The existing Scene Setup API client and service remain authoritative. The page still initializes missing Scene Setup through the existing POST endpoint and uses the existing background, character, prop, dialogue, action and Editor-handoff endpoints.

## Automated tests
Updated `apps/web/tests/e2e/scene-setup.spec.ts` for the redesigned UI. Coverage includes:
- scene identity and read-only duration;
- background selection and keyboard activation;
- Character selection and one-instance UI constraint;
- duplicate prop instances;
- dialogue Character/Narrator handling and ordering;
- action intent selection;
- explicit save and saved/unsaved states;
- refresh persistence;
- Open Editor handoff;
- empty sections.

## Validation
Local frontend execution was not available in the current environment. No test/build/lint result is claimed for this milestone.

## Known limitations
- Preset visuals are deterministic placeholders, not generated media.
- Prop add/remove remains immediately persisted by the existing API semantics.
- Editor is not redesigned by this milestone.
- Full automated CI validation is still required before marking this UI milestone VALIDATED.

## FILE-BY-FILE IMPLEMENTATION MAP
| File | Responsibility |
|---|---|
| `apps/web/app/stories/[storyId]/scene-setup/page.tsx` | Scene staging board UI over existing Scene Setup APIs. |
| `apps/web/tests/e2e/scene-setup.spec.ts` | Scene Setup UI regression/E2E coverage. |
| `docs/features/scene-setup/implementation.md` | Feature implementation memory and UI milestone status. |
| `docs/project/current-state.md` | Current UI foundation milestone state. |
| `docs/features/FEATURE_INDEX.md` | Feature status/index entry. |
