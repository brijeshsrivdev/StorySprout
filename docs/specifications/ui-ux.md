# Specification — StorySprout UI/UX Foundation

**Specification ID:** SPEC-UIUX-001  
**Status:** SPECIFIED — design only  
**Purpose:** Establish the shared creator experience and visual language for StorySprout V1  
**Implementation authorization:** None. This document does not authorize application/UI changes.

## 1. Problem

StorySprout's V1 creator workflow is functionally established through Story Creation, Story Outline, Characters, Scene Setup and Editor. The current screens are intentionally implementation-oriented and visually inconsistent. They communicate functionality but do not yet communicate a coherent creative product.

The product also needs a clear answer to the strategic question:

> Why would a creator build a children's video in StorySprout instead of a general-purpose AI video generator?

The answer cannot depend on generic claims of better AI generation. StorySprout's product experience must communicate structured storytelling, persistent characters, deliberate scene direction and creator control.

## 2. Goal

Create a focused design foundation that:

1. makes StorySprout feel like a modern creative studio;
2. makes the creator feel like the director;
3. communicates the persistent story-world model without prematurely implementing future world features;
4. gives all current and future screens one visual language;
5. makes AI feel contextual and collaborative;
6. keeps structured project state visibly trustworthy;
7. gives the Editor a signature creative-studio experience;
8. remains achievable with the current Next.js/Tailwind architecture;
9. does not expand V1 functional scope.

## 3. Product experience statement

> **StorySprout is an AI-assisted animation studio for building persistent children's story worlds.**

The creator directs.

AI assists.

The creator reviews and controls.

Project/Story/Scene/Composition state remains canonical.

## 4. Product differentiation

StorySprout differentiates through workflow continuity:

```
Idea
 ↓
Story
 ↓
Outline
 ↓
Reusable Cast
 ↓
Scene Setup
 ↓
Visual Direction
 ↓
Preview
 ↓
Finished Episode
```

The strategic asset is not a one-off generated video. It is the creator's growing story world.

V1 expresses this through Project-scoped reusable Characters and stable Story/Scene structure.

Future Places, Props, Episodes, visual consistency and world-level tooling remain deferred.

## 5. User stories

### Creator onboarding

As a first-time creator, I want to understand how StorySprout helps me make a story without learning video-editing software first.

### Story creation

As a creator, I want to start with AI or a blank story while retaining control over the final story.

### Story planning

As a creator, I want to see my story broken into understandable scenes before I visually stage it.

### Cast creation

As a creator, I want characters to feel reusable and persistent rather than disposable prompt outputs.

### Scene preparation

As a creator, I want to decide what belongs in a scene before arranging it visually.

### Scene direction

As a creator, I want to directly manipulate the visual scene and understand exactly what is selected and saved.

### AI collaboration

As a creator, I want AI suggestions to be contextual, reviewable and non-destructive.

### Trust

As a creator, I want save, failure and conflict states to preserve my work and communicate what happened.

## 6. Scope

### Included in this specification

- Dashboard experience;
- Create Story experience;
- Story Setup experience;
- Story Outline experience;
- Characters experience;
- Scene Setup experience;
- Editor visual/interaction direction;
- Preview conceptual experience;
- Render conceptual experience;
- shared visual system;
- AI interaction conventions;
- Story World direction;
- reusable component vocabulary;
- accessibility;
- responsive behavior;
- loading/error/success/save/generation states;
- V1/V1.1/Future prioritization.

### Explicitly not implemented by this specification

- Timeline;
- playhead;
- timing controls;
- keyframes;
- animation engine;
- animation generation/playback;
- voice;
- music;
- SFX;
- Preview implementation;
- Render implementation;
- YouTube publishing;
- advanced asset management;
- image-generation pipeline;
- new AI provider;
- new API endpoint;
- database migration;
- Composition schema change;
- renderer change;
- authentication;
- collaboration.

## 7. Visual system contract

### Color

Primary `#4F46E5`  
Primary strong `#3730A3`  
Secondary `#0F766E`  
Accent `#F59E0B`  
Background `#F7F7F5`  
Surface `#FFFFFF`  
Elevated `#FCFCFA`  
Border `#E5E7EB`  
Text `#171717`  
Muted `#6B7280`  
Success `#15803D`  
Warning `#B45309`  
Error `#B91C1C`  
AI `#7C3AED`

Color requirements:
- no default gradients;
- no color-only state communication;
- AI accent is reserved for AI interactions;
- warm accent is used sparingly for discovery/highlight;
- status colors must maintain accessible contrast.

### Typography

V1 recommendation:
- Inter for UI/body;
- system sans-serif fallback;
- one-family hierarchy rather than multiple font dependencies.

Sizes:
- display 40–48px;
- page heading 28–36px;
- section heading 18–22px;
- body 14–16px;
- caption 12–13px;
- metadata/editor labels 11–12px.

### Spacing

4px base rhythm with common values:
`4, 8, 12, 16, 24, 32, 48, 64`.

### Radius

- controls: 8–10px;
- cards: 14–16px;
- major creative surfaces: 18–20px;
- dialogs: 16px;
- Editor stage: 14–18px.

### Elevation

Three levels only:
- flat/bordered;
- raised;
- floating.

### Iconography

One consistent outline icon family. Icons support labels rather than replacing them. Emoji must not be used as primary application navigation/control iconography.

### Motion

- hover: 120–160ms;
- press: 80–120ms;
- panel transition: 180–220ms;
- route transition: 180–240ms;
- selection feedback: immediate;
- AI generation: subtle progress;
- save success: brief and quiet.

Respect `prefers-reduced-motion`.

## 8. Shared UX principles

1. Creator first.
2. AI is a collaborator.
3. Story state is persistent.
4. Progressive complexity.
5. Visual feedback.
6. Safe editing.
7. Structured source of truth.
8. One obvious next step.
9. Persistent context.
10. Direct manipulation.
11. Quiet chrome.
12. Recoverability.
13. Consistency.
14. Future-ready without unfinished-feature UI.

## 9. Screen requirements

### 9.1 Dashboard

**Current:** Project/story cards, Create Story, loading, empty, error.

**Required direction:**
- creative home/studio framing;
- recent Stories prioritized;
- stronger visual Story identity;
- clear Continue action;
- minimal metadata;
- Project context retained.

**Primary CTA:** Create Story.

**Empty:** inspirational first-story invitation.

**Loading:** story-card skeletons.

**Error:** inline retryable message.

**Responsive:** story grid collapses; editor is not part of mobile requirement.

### 9.2 Create Story

**Current:** AI/Blank cards.

**Required direction:**
- make AI and Blank meaningful creative choices;
- selected state must be unmistakable;
- explain expected outcome;
- maintain one Continue action.

**AI choice:** "Bring me from idea to first draft."

**Blank choice:** "Start with a clean story and direct it yourself."

### 9.3 Story Setup

**Current:** idea, age, duration, style, language form.

**Required direction:**
- story idea is visual hero;
- configuration is secondary;
- AI mode is contextual;
- generated draft is clearly a proposal;
- preserve input on failure;
- use one strong continuation action.

### 9.4 Story Outline

**Current:** CRUD-style scene cards, duration metrics, generation and save.

**Required direction:**
- storyboard mental model;
- scene beat hierarchy;
- visual placeholder region;
- compact target/planned/variance summary;
- clear next step to Characters;
- no destructive regeneration;
- preserve current duration rules.

### 9.5 Characters

**Current:** Story Cast, Project Character Library, CRUD, AI suggestion, placeholder emoji avatars.

**Required direction:**
- cast-board mental model;
- reusable Project identity is visually clear;
- replace emoji-first visuals with restrained placeholders/illustration treatment;
- AI suggestion is provisional;
- Story Cast visually prioritized.

### 9.6 Scene Setup

**Current:** Background, Characters, Props, Dialogue, Actions.

**Required direction:**
- frame as scene staging/pre-production;
- visually group scene ingredients;
- make Open Editor the natural handoff;
- keep dialogue/actions clearly preparation-only;
- no timeline semantics.

### 9.7 Editor

**Current:** left Scene Objects, central fixed stage, right Properties, explicit Save, background controls, empty Timeline boundary.

**Required direction:**
- signature StorySprout screen;
- scene context always visible;
- central stage visually dominant;
- contextual asset rail;
- properties panel appears useful, not generic;
- minimal toolbar;
- direct manipulation;
- selection states;
- Save state always understandable;
- Timeline remains a boundary only.

Editor layout direction:

```
┌───────────────────────────────────────────────────────────────┐
│ Story / Scene                                      Save state │
├───────────────┬──────────────────────────────┬───────────────┤
│ Scene / Assets│                              │ Properties    │
│               │            STAGE             │               │
│ Characters    │         1920 × 1080          │ Selected      │
│ Props         │                              │ Transform     │
│ Background    │                              │ Visibility    │
│               │                              │ Layers        │
├───────────────┴──────────────────────────────┴───────────────┤
│ Timeline boundary — future timing layer                      │
└───────────────────────────────────────────────────────────────┘
```

This diagram describes hierarchy, not an implementation requirement.

### 9.8 Preview

Conceptual only.

Required experience:
- large playback surface;
- minimal editing chrome;
- scene/story playback;
- return to Editor;
- continue to Render.

### 9.9 Render

Conceptual only.

Required states:
- Ready;
- Queued;
- Rendering;
- Completed;
- Failed.

The Composition remains safe regardless of render failure.

## 10. AI interaction contract

AI actions must be contextual.

| Screen | Example action | Canonical effect |
|---|---|---|
| Story Setup | Help create this story | Proposed draft |
| Outline | Suggest scenes | Proposed scene sequence |
| Characters | Help design character | Editable character suggestion |
| Scene Setup | Suggest staging | Future proposal |
| Editor | Improve this scene | Future composition suggestion |

Required visual states:
- Suggestion;
- Generating;
- Generated;
- Accept;
- Reject;
- Regenerate;
- Error;
- Unavailable.

Rule:

```
AI suggestion
 ↓
creator review
 ↓
accept/edit/reject
 ↓
canonical state
```

AI must never silently replace creator-authored state.

A giant prompt box should not be the default AI interaction.

## 11. Story World contract

### V1

Communicate Story World indirectly through:
- Project;
- reusable Characters;
- Story;
- stable Scenes.

Do not add empty Places/Props/World global navigation.

### V1.1

Evaluate Project-level world navigation once multiple reusable asset categories exist.

### Future

Potential World sections:
- Cast;
- Places;
- Props;
- Stories/Episodes;
- visual references;
- world rules;
- versions.

## 12. Editor interaction requirements

### Selection

Selected object must have:
- visual outline;
- accessible name;
- property panel context;
- keyboard focus where applicable.

### Movement

Direct drag changes logical x/y.

Property fields remain an alternative to drag.

### Scale

Uniform scale remains within existing 0.25–3.0 bounds.

### Visibility

Visibility must be visually obvious and accessible.

### Delete

Delete removes only the Composition object. Destructive confirmation should be introduced if future behavior becomes more consequential; current SPEC-006 semantics remain authoritative.

### Layers

Layer order is understandable as back-to-front scene order.

### Background

Background remains a scene-level property.

### Save

Visible states:
- Saved;
- Unsaved changes;
- Saving;
- Save failed;
- Conflict.

### Timeline boundary

No clip creation, timing or animation control may appear as implemented functionality under this specification.

## 13. State requirements

All major screens should define:

- initial/loading;
- empty;
- ready;
- editing;
- unsaved;
- saving;
- saved;
- failure;
- retry.

AI-enabled screens additionally define:

- generating;
- generated suggestion;
- AI failure;
- AI unavailable.

Render-enabled future screen additionally defines:

- queued;
- rendering;
- completed;
- failed.

State must not erase creator input.

## 14. Accessibility

Required:
- keyboard access;
- visible focus;
- semantic labels;
- accessible status announcements;
- adequate contrast;
- no color-only state;
- keyboard alternative for direct manipulation;
- descriptive destructive actions;
- reduced motion;
- sufficient target sizes;
- accessible Editor object names.

## 15. Responsive behavior

V1 is desktop-web-first.

Standard pages:
- desktop creative workspace;
- tablet two-to-one column adaptation;
- mobile readable/continuation experience.

Editor:
- desktop primary target;
- collapse Asset rail before shrinking stage;
- Properties can become drawer;
- Save/context remain visible;
- no requirement for mobile parity in V1.

## 16. Reusable component direction

Shared:
- StudioShell
- StudioHeader
- ProjectContext
- StoryCard
- StoryThumbnail
- PrimaryAction
- SecondaryAction
- IconButton
- SaveState
- StatusBadge
- EmptyState
- ErrorState
- AiSuggestion
- AiActionButton
- GenerationState
- ConfirmDialog
- InlineNotice

Story:
- SceneBeat
- CharacterCard
- AssetCard

Editor:
- EditorToolbar
- EditorAssetRail
- EditorStage
- CanvasSelection
- PropertyPanel
- LayerList
- EditorBottomBoundary

These are design-system concepts only.

## 17. Competitive pattern guidance

Current public product research indicates:

- HeyGen emphasizes streamlined AI Studio, editable scenes and consistent avatars.
- Canva emphasizes drag/drop editing, templates, multi-layer timelines and AI-assisted edits.
- Adobe Express/Firefly emphasizes accessible browser editing, templates, generative AI and timeline/properties workflows.
- CapCut emphasizes script-to-video, scene-oriented creation, templates and continuing manual editing.

StorySprout should borrow interaction patterns such as direct manipulation, staged AI assistance and review-before-finalization, while avoiding general-purpose design-suite or social-template positioning.

## 18. WOW requirements

The experience should deliberately create these moments:

1. Story idea becomes first draft.
2. Draft becomes storyboard.
3. Character joins a persistent cast.
4. Scene Setup becomes a visible scene.
5. Character moves naturally on stage.
6. AI proposes contextual improvements.
7. Save state builds trust.
8. Preview transforms editing into viewing.
9. Render completion produces a finished episode.

## 19. Prioritization

### V1

- Shared visual foundation.
- Dashboard refinement.
- Create Story refinement.
- Story Setup refinement.
- Story Outline refinement.
- Characters refinement.
- Scene Setup refinement.
- Editor visual hierarchy refinement.
- State patterns.
- Accessibility.
- Desktop-first responsive behavior.

### V1.1

- visual Story/Scene thumbnails;
- richer character visual placeholders/assets;
- contextual AI entry points;
- stronger Project/Story navigation;
- improved Editor asset browsing;
- Preview.

### Future

- Story World;
- Places;
- reusable Props;
- advanced asset generation;
- Timeline;
- animation;
- audio/voice/music/SFX;
- Render;
- publishing.

## 20. Non-functional design requirements

- No new frontend dependency is required by this specification.
- Existing Tailwind architecture remains sufficient.
- Existing API contracts remain unchanged.
- Existing Composition Model remains unchanged.
- No renderer changes are required.
- No database changes are required.
- No new service is required.
- Visual improvements must not alter canonical data semantics.
- Design changes must preserve existing save/error/conflict behavior.

## 21. Validation scenarios for future implementation

### Visual consistency
- All V1 screens use the shared token system.
- Primary actions are visually consistent.
- AI actions use the reserved AI accent.
- Status states are consistent.

### Navigation
- Creator always knows Story and current step.
- Primary continuation is obvious.
- Back navigation preserves context.
- Unsaved work is not silently discarded.

### AI
- AI suggestion is distinguishable from canonical content.
- Accept/reject/regenerate are explicit.
- AI failure preserves creator work.

### Editor
- Stage remains visually dominant.
- Selection is obvious.
- Property controls correspond to selected object.
- Save state is always understandable.
- Timeline is visibly future/boundary only.

### Accessibility
- Keyboard access works.
- Focus is visible.
- Labels exist.
- Color is not the sole signal.
- Reduced motion is respected.

### Scope
- No Timeline behavior is introduced.
- No Preview/Render functionality is implemented by this specification.
- No Story World navigation is introduced prematurely.

## 22. Architectural relationship

This specification follows existing ADR-001, ADR-003, ADR-004, ADR-005, ADR-009, ADR-011 and ADR-012.

No new ADR is required.

A future ADR should be proposed only if implementation requires:
- a new persistence model;
- a new service;
- a new Composition schema;
- a new AI-provider boundary;
- a materially different navigation architecture.

## 23. Definition of done for this specification

The design/specification task is complete when:
- current implementation is documented;
- differentiation is explicit;
- visual system is defined;
- UX principles are defined;
- each V1 screen has direction;
- Editor has detailed direction;
- AI interaction is defined;
- Story World is considered without premature implementation;
- WOW moments are identified;
- V1/V1.1/Future are separated;
- reusable components are identified;
- accessibility/responsive requirements exist;
- no application code is changed.

No UI implementation is included in this specification.
