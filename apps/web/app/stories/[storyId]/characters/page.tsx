/* eslint-disable react-hooks/set-state-in-effect */
"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import {
  addCharacterToStory,
  ApiError,
  Character,
  CharacterCategory,
  createCharacter,
  deleteCharacter,
  generateCharacter,
  getOutline,
  listProjectCharacters,
  listStoryCharacters,
  removeCharacterFromStory,
  updateCharacter,
} from "../../../../lib/api";
import {
  AiBadge,
  Button,
  Card,
  EmptyState,
  Field,
  Input,
  Notice,
  Select,
  StudioHeader,
  StudioShell,
  Textarea,
} from "../../../../components/ui";

const categories: CharacterCategory[] = ["CHILD", "ADULT", "ANIMAL", "FANTASY", "OBJECT", "OTHER"];

const categoryLabels: Record<CharacterCategory, string> = {
  CHILD: "Child",
  ADULT: "Adult",
  ANIMAL: "Animal",
  FANTASY: "Fantasy",
  OBJECT: "Object",
  OTHER: "Other",
};

const categoryMarks: Record<CharacterCategory, string> = {
  CHILD: "CH",
  ADULT: "AD",
  ANIMAL: "AN",
  FANTASY: "FA",
  OBJECT: "OB",
  OTHER: "OT",
};

type Draft = {
  name: string;
  roleDescription: string;
  category: CharacterCategory;
  visualDescription: string;
  personality: string;
};

const emptyDraft: Draft = {
  name: "",
  roleDescription: "",
  category: "CHILD",
  visualDescription: "",
  personality: "",
};

function CharacterAvatar({ character }: { character: Character }) {
  const initials = character.name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? "")
    .join("") || categoryMarks[character.category];

  return (
    <div
      className="relative grid h-24 w-24 shrink-0 place-items-center overflow-hidden rounded-[20px] border border-indigo-100 bg-indigo-50 text-[var(--ss-primary)] shadow-sm"
      aria-label={`Placeholder visual for ${character.name}`}
    >
      <div aria-hidden="true" className="absolute -right-5 -top-5 h-16 w-16 rounded-full border border-indigo-100 bg-white/70" />
      <div aria-hidden="true" className="absolute -bottom-7 -left-4 h-16 w-16 rounded-full border border-indigo-100 bg-white/50" />
      <span className="relative text-xl font-bold tracking-tight">{initials}</span>
    </div>
  );
}

function CharacterCard({
  character,
  membership,
  onEdit,
  onAdd,
  onRemove,
  onDelete,
}: {
  character: Character;
  membership: "story" | "library";
  onEdit: () => void;
  onAdd?: () => void;
  onRemove?: () => void;
  onDelete: () => void;
}) {
  return (
    <Card className="group overflow-hidden p-5 transition duration-150 hover:-translate-y-0.5 hover:border-indigo-200 hover:shadow-[var(--ss-shadow-floating)] focus-within:border-indigo-300">
      <div className="flex gap-4">
        <CharacterAvatar character={character} />
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-start justify-between gap-2">
            <div>
              <h3 className="text-lg font-bold tracking-tight">{character.name}</h3>
              <p className="mt-1 inline-flex rounded-full bg-slate-100 px-2.5 py-1 text-[11px] font-semibold text-slate-600">
                {categoryLabels[character.category]}
              </p>
            </div>
            <span className="text-[11px] font-medium text-[var(--ss-muted)]">
              {membership === "story" ? "In this Story" : "Project Cast"}
            </span>
          </div>
          <p className="mt-3 line-clamp-2 text-sm leading-6 text-slate-700">{character.roleDescription}</p>
        </div>
      </div>

      <div className="mt-4 grid gap-3 border-t border-[var(--ss-border)] pt-4">
        <div>
          <p className="text-[11px] font-semibold uppercase tracking-[.12em] text-[var(--ss-muted)]">Visual identity</p>
          <p className="mt-1 line-clamp-2 text-xs leading-5 text-slate-600">{character.visualDescription}</p>
        </div>
        {character.personality && (
          <div>
            <p className="text-[11px] font-semibold uppercase tracking-[.12em] text-[var(--ss-muted)]">Personality</p>
            <p className="mt-1 line-clamp-2 text-xs leading-5 text-slate-600">{character.personality}</p>
          </div>
        )}
      </div>

      <div className="mt-5 flex flex-wrap items-center gap-2">
        {onAdd && <Button variant="primary" onClick={onAdd}>Add to Story</Button>}
        <Button variant="secondary" onClick={onEdit}>Edit</Button>
        {onRemove && <Button variant="secondary" onClick={onRemove}>Remove from Story</Button>}
        {onDelete && <Button variant="danger" onClick={onDelete}>Delete</Button>}
      </div>
    </Card>
  );
}

export default function CharactersPage() {
  const { storyId } = useParams<{ storyId: string }>();
  const [storyCharacters, setStoryCharacters] = useState<Character[]>([]);
  const [library, setLibrary] = useState<Character[]>([]);
  const [projectId, setProjectId] = useState("");
  const [storyTitle, setStoryTitle] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadingError, setLoadingError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [draft, setDraft] = useState<Draft>(emptyDraft);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [idea, setIdea] = useState("");
  const [aiGenerating, setAiGenerating] = useState(false);
  const [saving, setSaving] = useState(false);
  const [suggestionReady, setSuggestionReady] = useState(false);

  async function load() {
    setLoading(true);
    setLoadingError(null);
    try {
      const outline = await getOutline(storyId);
      const [story, project] = await Promise.all([
        listStoryCharacters(storyId),
        listProjectCharacters(outline.story.projectId),
      ]);
      setProjectId(outline.story.projectId);
      setStoryTitle(outline.story.title);
      setStoryCharacters(story);
      setLibrary(project);
    } catch (error) {
      setLoadingError(error instanceof ApiError ? error.message : "We couldn't load the cast.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load();
  }, [storyId]);

  const usedIds = useMemo(() => new Set(storyCharacters.map((character) => character.id)), [storyCharacters]);
  const formValid =
    draft.name.trim().length > 0 &&
    draft.name.trim().length <= 120 &&
    draft.roleDescription.trim().length > 0 &&
    draft.roleDescription.trim().length <= 1000 &&
    draft.visualDescription.trim().length > 0 &&
    draft.visualDescription.trim().length <= 2000 &&
    draft.personality.trim().length <= 1000;

  function editCharacter(character: Character) {
    setEditingId(character.id);
    setDraft({
      name: character.name,
      roleDescription: character.roleDescription,
      category: character.category,
      visualDescription: character.visualDescription,
      personality: character.personality ?? "",
    });
    setSuggestionReady(false);
    setActionError(null);
  }

  function resetForm() {
    setEditingId(null);
    setDraft(emptyDraft);
    setSuggestionReady(false);
  }

  async function saveCharacter() {
    if (!formValid || saving) return;
    setSaving(true);
    setActionError(null);
    try {
      if (editingId) {
        await updateCharacter(projectId, editingId, {
          name: draft.name.trim(),
          roleDescription: draft.roleDescription.trim(),
          category: draft.category,
          visualDescription: draft.visualDescription.trim(),
          personality: draft.personality.trim(),
        });
      } else {
        const character = await createCharacter(projectId, {
          name: draft.name.trim(),
          roleDescription: draft.roleDescription.trim(),
          category: draft.category,
          visualDescription: draft.visualDescription.trim(),
          personality: draft.personality.trim(),
        });
        await addCharacterToStory(storyId, character.id);
      }
      resetForm();
      await load();
    } catch (error) {
      setActionError(error instanceof ApiError ? error.message : "We couldn't save this character.");
    } finally {
      setSaving(false);
    }
  }

  async function generateSuggestion() {
    if (!idea.trim() || aiGenerating) return;
    setAiGenerating(true);
    setActionError(null);
    try {
      const suggestion = await generateCharacter(projectId, { idea: idea.trim(), storyId });
      setDraft({
        name: suggestion.name,
        roleDescription: suggestion.roleDescription,
        category: suggestion.category,
        visualDescription: suggestion.visualDescription,
        personality: suggestion.personality ?? "",
      });
      setEditingId(null);
      setSuggestionReady(true);
    } catch (error) {
      setActionError(error instanceof ApiError ? error.message : "Character suggestion failed.");
      setSuggestionReady(false);
    } finally {
      setAiGenerating(false);
    }
  }

  async function addToStory(characterId: string) {
    setActionError(null);
    try {
      await addCharacterToStory(storyId, characterId);
      await load();
    } catch (error) {
      setActionError(error instanceof ApiError ? error.message : "We couldn't add this character.");
    }
  }

  async function removeFromStory(character: Character) {
    if (!confirm(`Remove ${character.name} from this Story? The reusable Project character will remain in the library.`)) return;
    setActionError(null);
    try {
      await removeCharacterFromStory(storyId, character.id);
      await load();
    } catch (error) {
      setActionError(error instanceof ApiError ? error.message : "We couldn't remove this character from the Story.");
    }
  }

  async function deleteFromLibrary(character: Character) {
    if (usedIds.has(character.id)) {
      setActionError("This reusable character is still used by a Story. Remove the Story membership before deleting it.");
      return;
    }
    if (!confirm(`Delete ${character.name} from the Project character library?`)) return;
    setActionError(null);
    try {
      await deleteCharacter(projectId, character.id);
      await load();
    } catch (error) {
      setActionError(error instanceof ApiError ? error.message : "We couldn't delete this character.");
    }
  }

  if (loading) {
    return (
      <StudioShell>
        <StudioHeader backHref="/" backLabel="Stories" context="Characters" />
        <main className="mx-auto max-w-6xl px-5 py-10 lg:px-8" aria-busy="true" aria-label="Loading Characters">
          <div className="h-4 w-24 animate-pulse rounded bg-slate-200" />
          <div className="mt-4 h-10 max-w-lg animate-pulse rounded bg-slate-200" />
          <div className="mt-3 h-5 max-w-2xl animate-pulse rounded bg-slate-100" />
          <div className="mt-10 grid gap-4 md:grid-cols-2">
            {[1, 2, 3, 4].map((item) => <div key={item} className="h-64 animate-pulse rounded-[16px] bg-slate-100" />)}
          </div>
        </main>
      </StudioShell>
    );
  }

  if (loadingError) {
    return (
      <StudioShell>
        <StudioHeader backHref="/" backLabel="Stories" context="Characters" />
        <main className="mx-auto max-w-3xl px-5 py-16 lg:px-8">
          <Notice tone="error">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <span>{loadingError}</span>
              <Button variant="secondary" onClick={() => void load()}>Try again</Button>
            </div>
          </Notice>
        </main>
      </StudioShell>
    );
  }

  return (
    <StudioShell>
      <StudioHeader
        backHref={`/stories/${storyId}/outline`}
        backLabel="Story Outline"
        context={storyTitle || "Characters"}
      />

      <main aria-labelledby="characters-heading" className="mx-auto max-w-6xl px-5 py-10 lg:px-8">
        <header className="max-w-3xl">
          <div className="flex flex-wrap items-center gap-2">
            <p className="text-xs font-semibold uppercase tracking-[.16em] text-[var(--ss-primary)]">Build the cast</p>
            <span className="rounded-full bg-slate-100 px-2.5 py-1 text-[11px] font-semibold text-slate-600">Project library</span>
          </div>
          <h1 id="characters-heading" className="mt-3 text-3xl font-bold tracking-tight sm:text-4xl">Your Characters</h1>
          <p className="mt-3 text-base leading-7 text-[var(--ss-muted)]">
            Characters you create here can be reused throughout your story world, across scenes and future Stories in this Project.
          </p>
        </header>

        {actionError && (
          <div className="mt-6">
            <Notice tone="error">
              <div className="flex flex-wrap items-center justify-between gap-4">
                <span>{actionError}</span>
                {idea.trim() && !suggestionReady && (
                  <Button variant="secondary" onClick={() => void generateSuggestion()} disabled={aiGenerating}>Retry suggestion</Button>
                )}
              </div>
            </Notice>
          </div>
        )}

        <section aria-labelledby="story-cast-heading" className="mt-10">
          <div className="flex flex-wrap items-end justify-between gap-3">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[.14em] text-[var(--ss-muted)]">Current story</p>
              <h2 id="story-cast-heading" className="mt-1 text-xl font-bold">In this Story</h2>
            </div>
            <p className="text-sm text-[var(--ss-muted)]">{storyCharacters.length} {storyCharacters.length === 1 ? "character" : "characters"}</p>
          </div>

          <div className="mt-5">
            {storyCharacters.length === 0 ? (
              <EmptyState
                icon="✦"
                title="Every story needs a cast."
                description="Create the first character for this Story, or bring an existing Project character into the cast."
                action={<Button onClick={() => document.getElementById("character-form")?.focus()}>Create a Character</Button>}
              />
            ) : (
              <div className="grid gap-4 md:grid-cols-2">
                {storyCharacters.map((character) => (
                  <CharacterCard
                    key={character.id}
                    character={character}
                    membership="story"
                    onEdit={() => editCharacter(character)}
                    onRemove={() => void removeFromStory(character)}
                    onDelete={() => void deleteFromLibrary(character)}
                  />
                ))}
              </div>
            )}
          </div>
        </section>

        <section aria-labelledby="project-library-heading" className="mt-12">
          <div className="max-w-2xl">
            <p className="text-xs font-semibold uppercase tracking-[.14em] text-[var(--ss-muted)]">Reusable cast</p>
            <h2 id="project-library-heading" className="mt-1 text-xl font-bold">Project Character Library</h2>
            <p className="mt-2 text-sm leading-6 text-[var(--ss-muted)]">
              These are reusable Project characters. Adding one to this Story creates membership; it does not duplicate the character.
            </p>
          </div>

          {library.filter((character) => !usedIds.has(character.id)).length > 0 ? (
            <div className="mt-5 grid gap-4 md:grid-cols-2">
              {library.filter((character) => !usedIds.has(character.id)).map((character) => (
                <CharacterCard
                  key={character.id}
                  character={character}
                  membership="library"
                  onEdit={() => editCharacter(character)}
                  onAdd={() => void addToStory(character.id)}
                  onDelete={() => void deleteFromLibrary(character)}
                />
              ))}
            </div>
          ) : (
            <div className="mt-5 rounded-[16px] border border-dashed border-slate-300 bg-white p-6 text-sm text-[var(--ss-muted)]">
              No additional reusable characters yet. Create one below to grow the Project cast.
            </div>
          )}
        </section>

        <section aria-labelledby="character-form-heading" className="mt-12 grid gap-6 lg:grid-cols-[1.15fr_.85fr]">
          <Card className="p-6 sm:p-7">
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div>
                <p className="text-xs font-semibold uppercase tracking-[.14em] text-[var(--ss-muted)]">Character identity</p>
                <h2 id="character-form-heading" className="mt-1 text-xl font-bold">{editingId ? "Edit Character" : "Create Character"}</h2>
                <p className="mt-2 text-sm leading-6 text-[var(--ss-muted)]">
                  {editingId
                    ? "This is a shared Project character. Changes update the reusable definition wherever it is used."
                    : "Define the identity that should stay consistent whenever this character returns."}
                </p>
              </div>
              {editingId && <span className="rounded-full bg-amber-50 px-2.5 py-1 text-[11px] font-semibold text-amber-800">Shared Project character</span>}
            </div>

            <div className="mt-7 grid gap-5">
              <Field label="Name" help="A clear name makes the character easy to recognize later.">
                <Input id="character-form" value={draft.name} maxLength={120} onChange={(event) => setDraft({ ...draft, name: event.target.value })} />
              </Field>

              <Field label="Role / description" help="Who is this character in the story?">
                <Textarea value={draft.roleDescription} maxLength={1000} rows={4} onChange={(event) => setDraft({ ...draft, roleDescription: event.target.value })} />
              </Field>

              <Field label="Category" help="Choose one of the fixed V1 character categories.">
                <Select value={draft.category} onChange={(event) => setDraft({ ...draft, category: event.target.value as CharacterCategory })}>
                  {categories.map((category) => <option key={category} value={category}>{categoryLabels[category]}</option>)}
                </Select>
              </Field>

              <Field label="Visual description" help="Describe the visual identity to keep consistent in future scene work.">
                <Textarea value={draft.visualDescription} maxLength={2000} rows={5} onChange={(event) => setDraft({ ...draft, visualDescription: event.target.value })} />
              </Field>

              <Field label="Personality (optional)" help="A few traits can help guide future story and scene suggestions.">
                <Textarea value={draft.personality} maxLength={1000} rows={3} onChange={(event) => setDraft({ ...draft, personality: event.target.value })} />
              </Field>
            </div>

            <div className="mt-7 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
              {editingId && <Button variant="secondary" onClick={resetForm}>Cancel</Button>}
              <Button variant="primary" onClick={() => void saveCharacter()} disabled={!formValid || saving}>
                {saving ? "Saving…" : editingId ? "Save Character" : "Create & Add to Story"}
              </Button>
            </div>
          </Card>

          <Card className="h-fit border-violet-100 bg-[var(--ss-surface-elevated)] p-6 sm:p-7">
            <div className="flex items-center gap-2">
              <AiBadge />
              <p className="text-xs font-semibold uppercase tracking-[.14em] text-[var(--ss-ai)]">Creative collaborator</p>
            </div>
            <h2 className="mt-3 text-xl font-bold">Start with an idea</h2>
            <p className="mt-2 text-sm leading-6 text-[var(--ss-muted)]">
              Describe a character in your own words. AI will suggest an editable identity using the same V1 fields.
            </p>

            <label htmlFor="character-idea" className="mt-6 block text-sm font-semibold">Character idea</label>
            <Textarea
              id="character-idea"
              value={idea}
              rows={5}
              placeholder="A shy little fox who helps other animals find their way home."
              onChange={(event) => {
                setIdea(event.target.value);
                if (!suggestionReady) setActionError(null);
              }}
            />

            <div className="mt-4 flex items-center justify-between gap-3">
              <span className="text-xs text-[var(--ss-muted)]">Describe → Suggest → Review → Create</span>
              <Button variant="ai" onClick={() => void generateSuggestion()} disabled={!idea.trim() || aiGenerating}>
                {aiGenerating ? "Suggesting…" : suggestionReady ? "Suggest again" : "Generate Suggestion"}
              </Button>
            </div>

            {suggestionReady && (
              <Notice tone="ai">
                AI suggestion is ready in the editable form. Review or change every field before explicitly creating the Character.
              </Notice>
            )}

            <p className="mt-5 text-xs leading-5 text-[var(--ss-muted)]">
              Nothing is saved by generation. V1 uses a deterministic placeholder visual; no image is generated here.
            </p>
          </Card>
        </section>

        <footer className="sticky bottom-0 z-10 -mx-5 mt-10 border-t border-[var(--ss-border)] bg-[var(--ss-bg)]/95 px-5 py-4 backdrop-blur-sm lg:-mx-8 lg:px-8">
          <div className="mx-auto flex max-w-6xl flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-xs text-[var(--ss-muted)]">Build your cast here; Scene Setup will use the Story&apos;s selected characters.</p>
            <Link
              href={`/stories/${storyId}/scene-setup`}
              aria-disabled={storyCharacters.length === 0}
              onClick={(event) => {
                if (storyCharacters.length === 0) event.preventDefault();
              }}
              className={`inline-flex min-h-10 items-center justify-center rounded-[10px] bg-[var(--ss-primary)] px-4 py-2 text-sm font-semibold text-white shadow-sm transition duration-150 hover:bg-[var(--ss-primary-strong)] focus:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ss-primary)] focus-visible:ring-offset-2 ${storyCharacters.length === 0 ? "pointer-events-none opacity-45" : ""}`}
            >
              Continue to Scene Setup →
            </Link>
          </div>
        </footer>
      </main>
    </StudioShell>
  );
}
