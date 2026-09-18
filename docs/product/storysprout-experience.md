# StorySprout Product Experience

**Status:** DESIGN FOUNDATION — specification only  
**Scope:** Creator experience and visual language for the current V1 journey  
**Implementation status:** No UI/application implementation is authorized by this document

## 1. Product experience direction

### Product thesis

StorySprout should not position itself as a general-purpose AI video generator.

The product experience is:

> **StorySprout is an AI-assisted animation studio for building persistent children's story worlds.**

The creator is the director. AI accelerates ideation, planning and asset creation, but the creator reviews and controls the result. The structured project and Composition remain the source of truth.

The long-term mental model is:

```
Project
 ├── Stories / Episodes
 ├── Characters
 ├── Places
 ├── Props
 ├── Scenes
 └── Composition
```

V1 deliberately implements only the concepts already supported by the repository. The experience should nevertheless make the eventual reusable-world model understandable without exposing unfinished navigation.

### Why a creator should choose StorySprout

General AI video tools increasingly offer prompt-to-video, avatars, AI B-roll, templates and automated editing. HeyGen, for example, emphasizes prompt-to-finished video, editable scenes and consistent avatars; Canva emphasizes drag-and-drop video editing, templates and AI-assisted editing; Adobe Express combines templates, drag/drop editing and generative AI; CapCut combines script-to-video, templates, scenes, captions, music and timeline editing.

StorySprout should therefore compete on **structured creative continuity**, not on generic generation speed.

The differentiating experience is:

1. **A story-first workflow** — the creator starts with a story, not a generic video prompt.
2. **Persistent identity** — characters belong to the Project and can be reused across Stories.
3. **Scene continuity** — the creator builds a scene deliberately from the Story Outline and Scene Setup.
4. **Creator-controlled AI** — AI proposes; the creator accepts, edits or rejects.
5. **Structured editing** — the visible scene maps to canonical Composition rather than a disposable generated video.
6. **A path to a reusable story world** — today's Characters and Scenes are the foundation for tomorrow's recurring cast, places and episodes.
7. **A director-like experience** — the product should make creators feel they are directing a small animated production.

This is intentionally different from promising "better AI video generation."

## 2. Current product reality

The repository was inspected on `main` after SPEC-006 Editor was merged.

Current architecture:
- `apps/web`: Next.js + TypeScript + Tailwind.
- `apps/api`: Java 21 + Spring Boot.
- `services/renderer`: separate renderer skeleton.
- `packages/editor-model`: canonical Composition Model, now schema 1.1.
- PostgreSQL: application metadata.
- MinIO: local media-storage direction.

Current validated creator journey:

```
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
Editor
```

Preview and Render are defined as product screens but are not implemented.

Current Composition constraints:
- fixed logical 1920×1080 stage;
- 30 fps;
- schema 1.1;
- explicit `CHARACTER` / `PROP` object semantics;
- Scene Setup initializes Composition once;
- after initialization, Composition is authoritative;
- Timeline remains an empty boundary in Editor V1;
- dialogue and action intent remain Scene Setup data.

The merged Editor implementation is validated by the repository CI run associated with the final Editor fix.

## 3. Current UX assessment

### Dashboard

**Exists:** Project/story list, Create Story CTA, loading, empty and error states.

**Problems:**
- Looks like a generic SaaS project list.
- Story identity is text-first rather than visual.
- There is little sense of an ongoing creative studio.
- Project and Story hierarchy is technically present but visually weak.
- No strong "continue creating" emphasis.
- Status and timestamps consume attention without helping the creator decide what to do next.

**Direction:** Make Dashboard feel like a creative home: recent story work first, strong visual thumbnails/placeholders, clear continuation, and a quiet Project identity.

### Create Story

**Exists:** AI vs Blank selection.

**Problems:**
- Choice is currently two generic cards with emoji.
- The decision feels like a setup option rather than a creative starting mode.
- The page does not preview what the two paths produce.

**Direction:** Present the choice as two creative starting rituals:
- **Start with AI** — "Bring me from idea to first draft."
- **Start Blank** — "I want to direct the story myself."

The visual treatment should communicate that both routes lead to the same studio.

### Story Setup

**Exists:** Idea, target age, duration, visual style, language.

**Problems:**
- Form-first layout.
- The creator has to configure several values before feeling creative progress.
- AI mode is indicated mainly through labels/buttons.
- No visual story preview or creative framing.

**Direction:** Make the story idea the hero. Keep supporting setup choices visually subordinate. The page should feel like a creative briefing, not a form.

### Story Outline

**Exists:** Scene list, editing, duration metrics, add/delete/reorder, AI generation, save states.

**Problems:**
- Strongly resembles an admin CRUD screen.
- Scene cards are text-heavy and visually flat.
- Story target/planned/variance metrics are useful but visually dominate.
- AI generation is a single generic action.
- Scenes do not yet feel like story beats or shots.

**Direction:** Introduce a storyboard mental model while preserving the current data model. A scene should feel like a tangible beat in a movie: number, title, short beat description, duration and eventually a visual thumbnail. V1 can use controlled visual placeholders rather than generated imagery.

### Characters

**Exists:** Story characters, Project library, create/edit, add/remove/delete, AI suggestion, deterministic placeholder avatars.

**Problems:**
- Emoji avatars make the product feel playful in a childish way rather than premium/playful.
- Project library and Story cast are conceptually correct but visually similar.
- The reusable nature is described in text rather than strongly represented visually.
- AI suggestion is separated into a form panel instead of feeling like an assistant to character creation.

**Direction:** Make the cast feel like a real creative asset library. Use restrained illustrated placeholder silhouettes/cards, not emoji as the primary visual language. Clearly distinguish "In this Story" from "Project Cast."

### Scene Setup

**Exists:** Background presets, Story Characters, props, dialogue, action intents, scene navigation and Save Changes.

**Problems:**
- Five sections feel like a configuration checklist.
- The creator cannot yet see the scene being assembled.
- The relationship between Scene Setup and Editor is not visually obvious.
- Dialogue and actions are presented as independent forms rather than scene ingredients.
- Prop additions can feel disconnected from the final scene.

**Direction:** Treat Scene Setup as a **pre-production table**: "What belongs in this scene?" The page should visually preview the scene ingredients and make the Editor handoff feel like the natural next step.

### Editor

**Exists:** Scene Objects panel, 1920×1080 stage, Properties panel, background controls, object move/scale/visibility/delete/layer order, explicit Save, dirty/conflict/error states, empty Timeline boundary.

**Problems:**
- Current visual treatment is still an implementation shell.
- Stage objects are text boxes rather than visual assets.
- Left/right panels are functional but visually generic.
- Toolbar hierarchy is weak.
- The Timeline boundary looks like a placeholder rather than a deliberate future surface.
- There is no strong director/scene context.
- The editor does not yet communicate "this is the heart of the product."

**Direction:** Establish the Editor as the signature StorySprout screen. It should be the first place where the creator feels they are directing an animated scene.

### Preview

**Exists:** Product-screen definition only; not implemented.

**Direction:** A calm cinematic review surface. It should remove editing chrome, show the scene/story playback prominently, and provide a clear return-to-edit action.

### Render

**Exists:** Product-screen definition only; not implemented.

**Direction:** Treat rendering as a production step, not a generic download modal. Show what is being produced, progress, completion and a clear artifact handoff.

## 4. Emotional/product personality

StorySprout should feel:

- **Playful** — enough warmth to make children's storytelling inviting.
- **Premium** — polished enough that creators trust it with a serious channel/project.
- **Cinematic** — visual hierarchy should suggest scenes, casts, framing and storytelling.
- **Approachable** — no requirement to understand professional editing terminology.
- **Inspiring** — the UI should encourage making, not configuring.
- **Calm** — avoid noisy dashboards, aggressive gradients and excessive badges.
- **Trustworthy** — AI actions are visible, reviewable and reversible where practical.

Avoid:
- childish cartoon chrome;
- generic purple AI gradients;
- enterprise/admin density;
- excessive cards inside cards;
- glassmorphism as decoration;
- giant prompt boxes on every screen;
- professional-editor complexity before it is needed.

## 5. StorySprout design language

### 5.1 Brand personality

**Warm cinematic studio.**

The visual language should combine the restraint of a modern creative tool with small signals of imagination. The interface is mostly neutral and quiet; color appears when it communicates creation, selection, AI assistance or progress.

The product should look more like a compact digital animation studio than a children's education app.

### 5.2 Color system

Use a warm-neutral foundation with a restrained indigo/violet creative accent.

| Token | Proposed value | Purpose |
|---|---|---|
| Primary | `#4F46E5` | Main creative actions, selected states, links |
| Primary-strong | `#3730A3` | Hover/pressed emphasis |
| Secondary | `#0F766E` | Optional world/asset secondary accent |
| Accent | `#F59E0B` | Moments of discovery, highlights, creative sparks |
| Background | `#F7F7F5` | Overall application canvas |
| Surface | `#FFFFFF` | Main panels and working surfaces |
| Elevated surface | `#FCFCFA` | Floating/contextual surfaces |
| Border | `#E5E7EB` | Quiet structure |
| Text | `#171717` | Primary readable content |
| Muted text | `#6B7280` | Metadata/helper content |
| Success | `#15803D` | Saved/completed state |
| Warning | `#B45309` | Attention without alarm |
| Error | `#B91C1C` | Validation/failure |
| AI accent | `#7C3AED` | AI suggestion/generation state |

Rules:
- Do not use gradients as the default brand treatment.
- Primary color should appear on intentional actions, not every heading.
- AI purple must identify AI assistance, not become the overall brand color.
- Warm accent is for moments of discovery, not status decoration.
- Status colors must never be the sole indication of state.

### 5.3 Typography

V1 should use one coherent sans-serif family rather than introducing multiple font dependencies.

Recommended:
- **UI/body:** Inter, with system sans-serif fallback.
- **Display/large headings:** Inter with tighter tracking and heavier weight rather than a second decorative font.
- **Editor labels:** Inter at 12–14px.
- **Metadata:** Inter at 11–13px.

Typography hierarchy:
- Display: 40–48px / 700
- Page heading: 28–36px / 700
- Section heading: 18–22px / 650–700
- Body: 14–16px / 400–500
- Caption: 12–13px / 400–500
- Metadata: 11–12px / 500
- Editor labels: 11–12px / 600

Do not use all-caps for large portions of copy. Small uppercase labels are acceptable for navigation context.

### 5.4 Spacing

Use a 4px base rhythm:
- 4px: icon/text micro spacing
- 8px: compact controls
- 12px: field/control internals
- 16px: component spacing
- 24px: section spacing
- 32px: major groups
- 48px: page-level separation
- 64px+: hero/empty-state breathing room

Avoid arbitrary spacing values.

### 5.5 Radius

- Small controls: 8px
- Buttons/inputs: 10px
- Cards/panels: 14–16px
- Major creative surfaces: 18–20px
- Modal/dialog: 16px
- Editor stage: 14–18px

The editor should use slightly tighter geometry than onboarding screens.

### 5.6 Elevation

Use three levels:
1. **Flat:** borders only.
2. **Raised:** very soft shadow for cards/panels.
3. **Floating:** stronger but still soft shadow for popovers/dialogs.

Avoid heavy shadows around every card.

### 5.7 Iconography

Use one consistent outline icon family at approximately 1.75–2px stroke.

Rules:
- icons support labels;
- do not use emoji as primary navigation or control icons;
- object/category illustrations can be expressive;
- destructive icons require labels or accessible names;
- AI spark icon is reserved for AI actions.

### 5.8 Motion

Motion should communicate creation and state:
- hover: 120–160ms;
- press: 80–120ms;
- panel transitions: 180–220ms;
- page/route transitions: subtle 180–240ms;
- AI generation: gentle progress/placeholder motion;
- save success: brief confirmation, not a celebratory animation;
- scene/object selection: fast highlight;
- drag: immediate response, no artificial lag.

Respect `prefers-reduced-motion`.

## 6. Core UX principles

1. **Creator first** — show the creative decision before configuration.
2. **AI is a collaborator** — suggestions require creator review.
3. **Story is persistent** — the project should feel like an ongoing creative world.
4. **Progressive complexity** — expose advanced controls only when the creator needs them.
5. **Visual feedback** — every meaningful action should have a clear state.
6. **Safe editing** — destructive actions confirm when necessary and never silently discard work.
7. **Source of truth** — UI semantics must map cleanly to the structured story/composition model.
8. **One obvious next step** — each screen should have one primary continuation.
9. **Context stays visible** — the creator should always know which Story and Scene they are editing.
10. **Direct manipulation where possible** — move/arrange visually rather than through configuration.
11. **Quiet chrome** — the UI should recede when the creative surface needs attention.
12. **Consistency over novelty** — controls behave the same way throughout the studio.
13. **Recoverability** — loading, saving, failure and conflict states should preserve creator work.
14. **Future-ready, not future-heavy** — reserve visual space and conceptual hierarchy for future capabilities without shipping unfinished features.

## 7. V1 creator journey

```
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
Editor
  ↓
Preview          [future]
  ↓
Render           [future]
```

Each step should answer one creator question:

| Screen | Creator question |
|---|---|
| Dashboard | What am I making next? |
| Create Story | How do I want to begin? |
| Story Setup | What is this story about? |
| Story Outline | What happens, scene by scene? |
| Characters | Who belongs in this story world? |
| Scene Setup | What belongs in this scene? |
| Editor | How do I direct this scene visually? |
| Preview | Does this feel right? |
| Render | How do I turn it into the finished episode? |

## 8. Screen-by-screen experience

### 8.1 Dashboard

**Desired emotion:** "This is my studio."

**Primary goal:** Resume work or start a new Story.

**Primary CTA:** `Create Story`

**Secondary actions:** Open Story, rename/archive later, view Project.

**Information hierarchy:**
1. Recent/active Stories.
2. Continue editing.
3. Project identity.
4. Secondary metadata.

**Layout:** Quiet studio header + recent-story workspace. Prefer large visual Story tiles over dense project tables.

**Empty state:** One strong visual invitation: "Your first story starts here." Show Create Story and a short example of the journey.

**Loading:** Skeleton story tiles and header; no large generic loading card.

**Error:** Inline studio-level message with retry; preserve navigation.

**Success:** After creation, route directly into the next creative step.

**AI:** No giant AI panel. A small "Start with AI" affordance belongs in creation.

**Navigation:** Keep global navigation minimal in V1. Suggested shell:
- Studio/Home
- current Project context
- Create

Do not add Cast/Places/Props as global navigation yet.

**Responsive:** Desktop-first. At narrower widths, story grid collapses; no attempt to reproduce full editor behavior on mobile.

**Accessibility:** Keyboard-visible focus, semantic headings, story links with descriptive names, non-color status.

**Future extensibility:** Story thumbnail/cover can later become the visual representation of an Episode.

### 8.2 Create Story

**Desired emotion:** "I am beginning something."

**Primary goal:** Choose a starting mode.

**Primary CTA:** Continue after selecting a mode.

**Two choices:**
- Start with AI
- Start Blank

Each choice should have:
- visual motif;
- short description;
- clear expected result;
- selected state;
- keyboard support.

**AI interaction:** Selecting AI should preview the promise: "Give me an idea; StorySprout will shape the first draft." It should not imply that the entire final video is generated immediately.

**Blank interaction:** Communicate creative control, not lack of functionality.

**Responsive:** Two-column desktop; stacked cards on narrow screens.

### 8.3 Story Setup

**Desired emotion:** "I can see the story forming."

**Primary goal:** Define the creative brief.

**Primary CTA:** Generate Story / Continue.

**Information hierarchy:**
1. Story idea.
2. Audience.
3. Length.
4. Visual language.
5. Language.

**Layout:** Story idea gets approximately half the visual weight. Supporting controls form a compact "Story recipe" area.

**AI:** In AI mode, provide contextual helper copy next to the idea field rather than a separate chat panel.

**Generated draft:** Show it as a proposed story card that can be reviewed before continuing. Avoid presenting AI output as final truth.

**Loading:** Replace the draft area with a calm generation state: "Growing your first draft…" and a subtle progress treatment.

**Error:** Preserve all entered setup values; explain whether retry is safe.

**Success:** Transition directly into Outline.

### 8.4 Story Outline

**Desired emotion:** "I can see the movie in my head."

**Primary goal:** Shape narrative beats.

**Primary CTA:** Continue to Characters.

**Secondary actions:** Generate Outline, Add Scene, Save Changes.

**Layout:** Story context header + storyboard sequence + compact duration summary.

Scene cards should emphasize:
- scene number;
- title;
- one-line beat;
- duration;
- reorder/delete;
- future thumbnail region.

The storyboard should read left-to-right conceptually even if the current V1 layout remains vertical for simplicity.

**AI:** "Suggest scenes" is contextual. Generated scenes are clearly marked as suggestions until saved.

**Empty state:** "Start with your first scene" with Generate Outline and Add Scene.

**Loading:** Skeleton scene beats, not a generic full-page spinner.

**Error:** Keep current scenes intact.

**Success:** Save state is visible but quiet.

### 8.5 Characters

**Desired emotion:** "These are my characters."

**Primary goal:** Build the reusable cast.

**Primary CTA:** Create Character / Create & Add to Story.

**Secondary actions:** Add from Project library, Edit, Remove from Story, Delete unused Project Character.

**Layout:**
- Story Cast as primary section.
- Project Cast as secondary section.
- Character creation/AI suggestion in a contextual side panel or focused creation drawer.

**Character card:**
- visual placeholder;
- name;
- category;
- short role;
- Story membership state;
- actions.

**Empty state:** A welcoming cast board, not a blank table.

**AI:** Generate Suggestion should populate editable draft fields. The suggestion should be visibly provisional until explicit creation.

**Success:** New character joins the Story Cast immediately and visually.

**Future:** Real character art can replace placeholders without changing card structure.

### 8.6 Scene Setup

**Desired emotion:** "I am staging the scene."

**Primary goal:** Decide scene ingredients before directing them visually.

**Primary CTA:** Save Changes / Open Editor.

**Layout direction:**
- Scene context at top.
- Ingredient controls arranged as Background, Characters, Props.
- Dialogue and Actions lower in hierarchy because they are preparation data, not visual placement in Editor V1.
- A compact "Scene ingredients" summary should make the Editor handoff obvious.

**Current constraints remain visible:**
- Backgrounds are presets.
- Characters come from Story Cast.
- Props are controlled presets.
- Dialogue is manual.
- Actions are intent only.

**AI:** No broad AI generation in V1. Future contextual AI can be added without changing the layout model.

**Empty state:** Show the scene as intentionally unbuilt, with one clear next action.

**Success:** Save feedback is local and calm; Open Editor is visually stronger after the setup is saved.

### 8.7 Editor

**Desired emotion:** "I am directing this scene."

**Primary goal:** Compose the visual scene.

**Primary CTA:** Save.

**Core workspace:**
- compact scene context/header;
- left Asset/Scene Objects panel;
- central stage;
- right Properties panel;
- reserved lower timeline boundary.

The exact implementation should evolve from the current Editor, not be replaced by a different architecture.

**Header:**
- Story name;
- Scene number/title;
- save state;
- Save action;
- Back to Scene Setup.

**Left panel:**
- Scene Objects;
- Characters;
- Props;
- future asset categories;
- selected object highlight.

Use tabs or compact sections rather than long lists once the asset library grows.

**Stage:**
- visually dominant;
- fixed logical 1920×1080;
- generous neutral surround;
- selected objects receive a clear outline/handles;
- stage background and objects should visually read as a scene, not UI boxes.

**Right panel:**
- object name/type;
- position;
- scale;
- visibility;
- layer order;
- background context.

Hide irrelevant controls until an object is selected.

**Toolbar:**
V1 should remain small:
- Select;
- move/hand if required by interaction;
- add Character;
- add Prop;
- background;
- delete;
- layer actions where contextually useful.

Do not introduce professional editor toolbars prematurely.

**Timeline boundary:**
Keep the lower region intentionally quiet. It can communicate "Timing comes next" without pretending that timeline functionality exists.

**AI assistance entry point:**
A single contextual "Ask StorySprout" / spark action belongs near the scene context, not as a permanent prompt box. In V1 it can remain a design placeholder; no implementation is authorized here.

Potential future actions:
- "Make the scene feel warmer."
- "Suggest a better character arrangement."
- "Create three staging options."

Each would produce a suggestion, never silently mutate Composition.

**Selection states:**
- hover;
- selected;
- multi-selection future;
- hidden;
- unavailable/missing reference.

**Empty editor state:**
A visually calm stage with:
"Your scene is ready. Add a Character or Prop to begin directing."

**Loading:** Stage skeleton + panel skeleton, preserving scene context.

**Saving:** Header status transitions `Unsaved → Saving → Saved`.

**Error:** Keep local edits. Never replace the canvas with a generic error if the composition can still be edited locally.

**Conflict:** Explain that another version exists and offer Reload Latest. Do not overwrite automatically.

**Responsive:** Desktop-first. Minimum supported editor viewport should preserve stage usability. On smaller widths, collapse Properties into a drawer and Assets into a collapsible rail rather than shrinking the stage below useful dimensions.

**Accessibility:**
- keyboard selection;
- keyboard delete/escape;
- focusable object controls;
- labels for numeric properties;
- visible focus;
- no color-only selection indication;
- stage objects must have accessible names;
- reduced-motion support.

### 8.8 Preview

**Status:** Future screen; design only.

**Desired emotion:** "It is becoming a real episode."

**Primary goal:** Watch the story without editing distractions.

**Layout:** Large playback surface, scene/story navigation, compact playback controls, Return to Editor, Continue to Render.

**AI:** No AI prompt by default. AI assistance remains contextual to editing.

### 8.9 Render

**Status:** Future screen; design only.

**Desired emotion:** "I am finishing something."

**Primary goal:** Produce a finished artifact.

**Layout:** Output preview, title, output settings, render action, job progress, completed artifact.

**States:** Ready → Queued → Rendering → Completed / Failed.

**Failure:** Preserve the Composition and allow retry.

## 9. AI interaction design

### Principle

AI should feel like a collaborator sitting beside the creator, not a replacement for the studio.

The default interaction pattern:

```
AI suggestion
    ↓
Creator review
    ↓
Accept / Edit / Reject
    ↓
Canonical project state
```

### Contextual AI map

| Context | AI action | Result |
|---|---|---|
| Story Setup | Help create this story | Draft suggestion |
| Outline | Suggest scenes | Ordered scene proposal |
| Characters | Help design character | Editable character fields |
| Scene Setup | Suggest staging | Future scene suggestion |
| Editor | Improve this scene | Future visual/composition suggestion |

### Visual conventions

**Suggestion:** Purple AI accent + "Suggestion" label.

**Generating:** "Creating…" plus subtle animated indicator.

**Generated:** Show result in a bounded suggestion surface.

**Accept:** Explicit primary action.

**Reject:** Secondary action; no destructive semantics.

**Regenerate:** Generates a new suggestion while preserving the current accepted state.

**Error:** "AI couldn't generate a suggestion. Your work is unchanged."

**Unavailable:** Explain that the creator can continue manually.

Never make AI-generated content visually indistinguishable from canonical creator-authored state before acceptance.

### Avoid prompt-box overload

Use prompt input only where natural language materially helps. Most AI actions should begin from the current context and offer a focused action.

Good:
- "Suggest 3 scenes"
- "Help design this character"
- "Improve this scene"

Avoid:
- a giant "Ask AI anything" box on every screen.

## 10. Story World visual concept

### Strategic direction

The Story World is important to the product identity, but it should not become a premature V1 information architecture.

### V1

Use existing concepts:
- Project;
- Story;
- Characters;
- Scene Setup.

Make reusable Characters visually feel Project-owned. This is the first concrete signal that StorySprout is building a world, not a disposable video.

### V1.1

Introduce a stronger Project-level "World" view only when there are enough reusable assets to justify it.

Candidate sections:
- Cast;
- Places;
- Props;
- Stories/Episodes.

### Future

A dedicated Story World experience can become the creator's persistent production space:
- recurring cast;
- canonical locations;
- visual references;
- world rules;
- episode history;
- asset versions.

Do not add empty "Places" or "World" navigation to V1 merely to signal the future.

## 11. Competitive pattern research

Research was performed against current public product information from HeyGen, Canva, Adobe Express/Firefly and CapCut.

Useful patterns:

### HeyGen
- streamlined AI Studio for creators unfamiliar with traditional editing;
- script/scene structure;
- direct scene controls;
- editable generated content;
- consistent avatar identity;
- preview before final generation.

**Use for StorySprout:** contextual scene structure and creator control.

**Do not copy:** presenter/avatar-first model. StorySprout is story-world-first.

### Canva
- drag-and-drop editing;
- multi-layer timeline;
- templates;
- AI assistance integrated into an existing editor.

**Use for StorySprout:** direct manipulation and approachable editing.

**Do not copy:** broad all-purpose design-suite navigation.

### Adobe Express / Firefly
- simple browser editing;
- templates;
- AI generation inside the editing workflow;
- timeline + properties;
- explicit distinction between generation and editing.

**Use for StorySprout:** contextual AI generation alongside manual control.

**Do not copy:** general-purpose creative-suite breadth.

### CapCut
- AI planning/script-to-video;
- scene-oriented creation;
- templates;
- timeline;
- ability to continue manual editing after AI generation.

**Use for StorySprout:** "AI draft → manual refinement" flow.

**Do not copy:** trend/social-first visual language.

### StorySprout's differentiated pattern

The product should combine:
- story planning;
- reusable cast;
- scene preparation;
- visual directing;
- structured canonical state.

That sequence is the differentiator, not a particular button layout.

## 12. WOW moments

### WOW-01 — First story takes shape

**Action:** Creator submits a simple story idea.

**Response:** StorySprout presents a clean story draft and immediately moves toward Outline.

**Visual:** Draft appears as a designed story card rather than raw generated text.

**Motion:** Gentle reveal.

**Emotional purpose:** "My idea is becoming something."

### WOW-02 — Outline becomes a storyboard

**Action:** Creator opens Outline.

**Response:** Scenes appear as a coherent sequence.

**Visual:** Numbered storyboard beats with strong titles and visual placeholders.

**Motion:** Scene beats enter sequentially.

**Purpose:** Make structure feel cinematic.

### WOW-03 — The cast comes alive

**Action:** Creator creates the first character.

**Response:** Character joins the Story Cast and receives a strong visual identity card.

**Motion:** Card joins the cast board.

**Purpose:** Establish ownership and continuity.

### WOW-04 — Scene ingredients become a stage

**Action:** Creator finishes Scene Setup and opens Editor.

**Response:** Background, characters and props appear in their initial composition.

**Visual:** Immediate scene context, not an empty editor.

**Purpose:** "My story is now a place."

### WOW-05 — Direct manipulation feels effortless

**Action:** Creator drags a character.

**Response:** Character follows smoothly, logical position updates, selection remains obvious.

**Motion:** Immediate direct manipulation.

**Purpose:** Remove the feeling of configuring software.

### WOW-06 — AI suggestion feels contextual

**Action:** Creator asks for scene help.

**Response:** StorySprout proposes a small number of concrete options.

**Visual:** Suggestions sit beside the scene rather than replacing it.

**Purpose:** AI feels like a director's assistant.

### WOW-07 — Save feels trustworthy

**Action:** Creator saves.

**Response:** Quiet `Saved` state and preserved work.

**Purpose:** Trust in the studio as a persistent workspace.

### WOW-08 — Preview feels like a movie

**Future action:** Creator previews.

**Response:** Editing chrome recedes and the story fills the screen.

**Purpose:** Convert "editing work" into "finished storytelling."

### WOW-09 — Finished episode

**Future action:** Render completes.

**Response:** A polished episode artifact with title/thumbnail/output information.

**Purpose:** Give closure and encourage the next episode.

## 13. Reusable design-system architecture

### Retain conceptually

Existing simple page-level patterns are useful:
- semantic headings;
- explicit loading/error/saved states;
- Tailwind utility styling;
- direct Next.js navigation;
- small feature-local components.

### Redesign

The following existing patterns should be visually redesigned without changing their product semantics:
- page headers;
- primary/secondary buttons;
- cards;
- form fields;
- selection cards;
- status indicators;
- empty states;
- scene cards;
- character cards;
- Scene Setup sections;
- Editor panels.

### New shared components

Specification-level component vocabulary:

```
StudioShell
StudioHeader
ProjectContext
StoryCard
StoryThumbnail
PrimaryAction
SecondaryAction
IconButton
StatusBadge
SaveState
EmptyState
ErrorState
AiSuggestion
AiActionButton
GenerationState
SceneBeat
CharacterCard
AssetCard
SectionPanel
PropertyField
PropertyPanel
CanvasSelection
LayerList
EditorToolbar
EditorStage
EditorBottomBoundary
ConfirmDialog
Toast/InlineNotice
```

These are design concepts, not implementation authorization.

### State patterns

Reusable states:
- idle;
- loading;
- empty;
- editing;
- unsaved;
- saving;
- saved;
- generating;
- generated;
- failed;
- conflict;
- unavailable.

Every state should have:
- clear text;
- visual distinction;
- accessible announcement where appropriate;
- preserved user work.

## 14. Accessibility foundation

Minimum design requirements:
- WCAG-oriented contrast for text and controls;
- visible keyboard focus;
- keyboard access to every action;
- labels for every form control;
- status messages exposed through appropriate live regions;
- no meaning conveyed by color alone;
- destructive actions have descriptive labels;
- editor object selection has non-color indication;
- drag interaction must have a keyboard/property-panel alternative;
- reduced-motion mode;
- readable minimum body size;
- sufficient hit targets for controls;
- errors located near the affected context.

Accessibility is especially important because the creator workflow should not require precision mouse interaction.

## 15. Responsive foundation

V1 is desktop-web-first.

### Standard pages

- desktop: centered creative workspace;
- tablet: single-column or two-column adaptation;
- mobile: readable continuation/status pages, but not full Editor parity.

### Editor

Desktop is the supported primary target.

At reduced width:
1. preserve stage usability;
2. collapse Properties to a drawer;
3. collapse Asset rail when necessary;
4. keep Save and scene context visible.

Do not solve mobile Editor by simply shrinking the stage.

## 16. V1 / V1.1 / Future

### V1 — implement now

Design foundation and focused UI refinement for:
- Dashboard;
- Create Story;
- Story Setup;
- Story Outline;
- Characters;
- Scene Setup;
- Editor;
- shared states/components;
- coherent typography/color/spacing;
- stronger creative hierarchy;
- accessible responsive patterns.

Keep current product boundaries:
- no Timeline;
- no animation engine;
- no audio/voice;
- no Preview implementation;
- no Render implementation;
- no advanced asset generation;
- no Story World navigation.

### V1.1 — after core workflow works

- visual scene thumbnails;
- richer Character artwork placeholders/generated assets;
- stronger Project/Story visual navigation;
- contextual AI assistance in existing screens;
- improved Editor toolbar and asset browser;
- first Preview experience;
- carefully specified Timeline foundation.

### Future — strategic vision

- Story World;
- Places/Locations;
- reusable Props;
- persistent visual style;
- episode templates;
- advanced character consistency;
- AI staging/directing;
- animation;
- voices/music/SFX;
- full Timeline;
- Preview and Render pipeline;
- publishing integrations.

## 17. Architecture alignment

The experience specification preserves existing architectural invariants:

```
AI
 ↓
suggestion / asset
 ↓
creator review
 ↓
Project / Story / Scene
 ↓
Composition
 ↓
Renderer
```

The UI must not create a second source of truth.

Characters remain reusable Project-owned records with Story membership.

Story Outline remains planning state.

Scene Setup remains preparation state.

Editor/Composition remains canonical visual editing state.

Future Timeline owns exact timing.

Renderer consumes versioned Composition.

No UI concept in this document requires a new backend service, queue, database, or architectural boundary.

## 18. Research references

The competitive review used current public product information from:
- HeyGen AI Studio and AI Video Generator;
- Canva Video Editor;
- Adobe Express / Adobe Firefly Video Editor;
- CapCut AI Video Editor and AI Template Generator.

The purpose of this research is interaction-pattern analysis only. StorySprout should not reproduce their visual identity or information architecture.

## 19. Design acceptance criteria

- Current implemented screens are documented against their actual repository behavior.
- Product differentiation is based on persistent story-world continuity and creator-controlled structured editing.
- Visual personality is defined as warm cinematic studio.
- Color, typography, spacing, radius, elevation, iconography and motion are defined.
- Creator-first, AI-collaborative, persistent-story, progressive-complexity and source-of-truth principles are explicit.
- Dashboard, Create Story, Story Setup, Outline, Characters, Scene Setup and Editor have screen-level direction.
- Preview and Render are specified without implementation.
- Editor is treated as the core signature experience without expanding SPEC-006 functionality.
- AI interaction is contextual and non-destructive.
- Story World is future-facing without premature V1 navigation.
- WOW moments are defined.
- V1/V1.1/Future priorities are separated.
- Reusable component/state patterns are defined.
- Accessibility and responsive behavior are included.
- No application code, API contract, database schema, Composition schema or renderer behavior is changed by this specification.
