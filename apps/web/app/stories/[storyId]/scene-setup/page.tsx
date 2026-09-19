/* eslint-disable react-hooks/set-state-in-effect */
"use client";

import Link from "next/link";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import {
  ApiError,
  Character,
  SceneSetup,
  SceneSetupAction,
  addSceneProp,
  createSceneSetup,
  getOutline,
  getSceneSetup,
  listStoryCharacters,
  removeSceneProp,
  saveSceneActions,
  saveSceneCharacters,
  saveSceneDialogue,
  updateSceneBackground,
} from "../../../../lib/api";
import {
  Button,
  Card,
  EmptyState,
  IconButton,
  Notice,
  SaveState,
  StudioHeader,
  StudioShell,
  Textarea,
  Select,
} from "../../../../components/ui";

const ACTIONS: SceneSetupAction["action"][] = ["IDLE", "TALK", "WALK", "RUN", "WAVE", "SIT", "JUMP"];

type Line = { speakerType: "CHARACTER" | "NARRATOR"; characterId: string | null; text: string };
type Act = { characterId: string; action: SceneSetupAction["action"] };

const backgroundMarks: Record<string, string> = {
  "sunny-forest": "SF",
  garden: "GD",
  "cozy-bedroom": "BR",
  classroom: "CL",
  village: "VL",
  "sunny-beach": "SB",
};

function move<T>(items: T[], index: number, direction: -1 | 1) {
  const target = index + direction;
  if (target < 0 || target >= items.length) return items;
  const next = [...items];
  [next[index], next[target]] = [next[target], next[index]];
  return next;
}

function sortSetup(setup: SceneSetup) {
  return {
    ...setup,
    characters: [...setup.characters].sort((a, b) => a.orderIndex - b.orderIndex),
    props: [...setup.props].sort((a, b) => a.orderIndex - b.orderIndex),
    dialogue: [...setup.dialogue].sort((a, b) => a.sequenceIndex - b.sequenceIndex),
    actions: [...setup.actions].sort((a, b) => a.sequenceIndex - b.sequenceIndex),
  };
}

function ScenePresetVisual({ name, category }: { name: string; category: string }) {
  const key = name.toLowerCase().replace(/\s+/g, "-");
  return (
    <div className="relative h-24 overflow-hidden rounded-[12px] border border-slate-200 bg-slate-100">
      <div className="absolute inset-0 bg-white" aria-hidden="true" />
      <div className="absolute bottom-0 left-0 right-0 h-8 bg-slate-200/70" aria-hidden="true" />
      <span className="absolute left-3 top-3 grid h-9 w-9 place-items-center rounded-lg bg-white/85 text-xs font-bold text-[var(--ss-primary)] shadow-sm">
        {backgroundMarks[key] ?? name.slice(0, 2).toUpperCase()}
      </span>
      <span className="absolute bottom-2 left-3 text-[10px] font-semibold uppercase tracking-[.12em] text-slate-500">{category}</span>
    </div>
  );
}

function CharacterMark({ character }: { character: Character | SceneSetup["characters"][number] }) {
  const initials = character.name.trim().split(/\s+/).slice(0, 2).map(x => x[0]?.toUpperCase()).join("") || "C";
  return (
    <span aria-hidden="true" className="grid h-11 w-11 shrink-0 place-items-center rounded-[12px] border border-indigo-100 bg-indigo-50 text-xs font-bold text-[var(--ss-primary)]">
      {initials}
    </span>
  );
}

function SectionHeader({ eyebrow, title, hint }: { eyebrow: string; title: string; hint: string }) {
  return (
    <div>
      <p className="text-[11px] font-semibold uppercase tracking-[.14em] text-[var(--ss-muted)]">{eyebrow}</p>
      <h2 className="mt-1 text-lg font-bold tracking-tight">{title}</h2>
      <p className="mt-1 text-xs leading-5 text-[var(--ss-muted)]">{hint}</p>
    </div>
  );
}

export default function SceneSetupPage() {
  const { storyId } = useParams<{ storyId: string }>();
  const searchParams = useSearchParams();
  const router = useRouter();
  const requestedScene = searchParams.get("scene");

  const [outline, setOutline] = useState<Awaited<ReturnType<typeof getOutline>> | null>(null);
  const [setup, setSetup] = useState<SceneSetup | null>(null);
  const [chars, setChars] = useState<Character[]>([]);
  const [sceneId, setSceneId] = useState("");
  const [selected, setSelected] = useState<string[]>([]);
  const [background, setBackground] = useState<string | null>(null);
  const [dialogue, setDialogue] = useState<Line[]>([]);
  const [actions, setActions] = useState<Act[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [dirty, setDirty] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  async function getOrCreateSetup(id: string) {
    try {
      return await getSceneSetup(storyId, id);
    } catch (e) {
      if (e instanceof ApiError && e.status === 404) return createSceneSetup(storyId, id);
      throw e;
    }
  }

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const result = await getOutline(storyId);
      const id = requestedScene || result.scenes[0]?.id;
      if (!id) throw new Error("This Story has no Outline Scene yet.");
      const [storyCharacters, sceneSetup] = await Promise.all([
        listStoryCharacters(storyId),
        getOrCreateSetup(id),
      ]);
      const normalized = sortSetup(sceneSetup);
      setOutline(result);
      setSceneId(id);
      setChars(storyCharacters);
      setSetup(normalized);
      setSelected(normalized.characters.map(x => x.id));
      setBackground(normalized.backgroundPresetKey);
      setDialogue(normalized.dialogue.map(x => ({ speakerType: x.speakerType, characterId: x.characterId, text: x.text })));
      setActions(normalized.actions.map(x => ({ characterId: x.characterId, action: x.action })));
      setDirty(false);
      setNotice(null);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : e instanceof Error ? e.message : "We couldn't load Scene Setup.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load();
  }, [storyId, requestedScene]);

  const scenes = outline?.scenes ?? [];
  const sceneIndex = scenes.findIndex(s => s.id === sceneId);
  const scene = sceneIndex >= 0 ? scenes[sceneIndex] : scenes[0];
  const charMap = useMemo(() => new Map(chars.map(c => [c.id, c])), [chars]);
  const hasChanges = dirty;

  function change<T>(setter: (value: T) => void, value: T) {
    setter(value);
    setDirty(true);
    setNotice(null);
  }

  async function save() {
    if (!setup || !scene || saving) return;
    if (dialogue.some(x => !x.text.trim() || x.text.trim().length > 2000)) {
      setNotice("Dialogue text is required and limited to 2000 characters.");
      return;
    }
    if (dialogue.some(x => x.speakerType === "CHARACTER" && (!x.characterId || !selected.includes(x.characterId)))) {
      setNotice("Character dialogue must use a selected Story Character.");
      return;
    }
    if (dialogue.some(x => x.speakerType === "NARRATOR" && x.characterId)) {
      setNotice("Narrator lines cannot reference a Character.");
      return;
    }
    if (actions.some(x => !selected.includes(x.characterId))) {
      setNotice("Actions must reference selected Story Characters.");
      return;
    }

    setSaving(true);
    setNotice(null);
    try {
      let saved = await updateSceneBackground(storyId, scene.id, background);
      saved = await saveSceneCharacters(storyId, scene.id, selected);
      saved = await saveSceneDialogue(storyId, scene.id, dialogue);
      saved = await saveSceneActions(storyId, scene.id, actions);
      setSetup(sortSetup(saved));
      setDirty(false);
      setNotice("Scene setup saved.");
    } catch (e) {
      setNotice(e instanceof ApiError ? e.message : "Save failed. Your changes are still here; try again.");
    } finally {
      setSaving(false);
    }
  }

  async function addProp(key: string) {
    try {
      const updated = await addSceneProp(storyId, sceneId, key);
      setSetup(sortSetup(updated));
      setNotice("Prop added.");
    } catch (e) {
      setNotice(e instanceof ApiError ? e.message : "Couldn't add this prop.");
    }
  }

  async function removeProp(id: string) {
    try {
      const updated = await removeSceneProp(storyId, sceneId, id);
      setSetup(sortSetup(updated));
      setNotice("Prop removed.");
    } catch (e) {
      setNotice(e instanceof ApiError ? e.message : "Couldn't remove this prop.");
    }
  }

  if (loading) {
    return (
      <StudioShell>
        <StudioHeader backHref="/" backLabel="Stories" context="Scene Setup" />
        <main className="mx-auto max-w-6xl px-5 py-10 lg:px-8" aria-busy="true" aria-label="Loading Scene Setup">
          <div className="h-4 w-28 animate-pulse rounded bg-slate-200" />
          <div className="mt-4 h-10 max-w-xl animate-pulse rounded bg-slate-200" />
          <div className="mt-3 h-5 max-w-3xl animate-pulse rounded bg-slate-100" />
          <div className="mt-10 grid gap-5 lg:grid-cols-2">
            {[1,2,3,4,5].map(i => <div key={i} className="h-56 animate-pulse rounded-[16px] bg-slate-100" />)}
          </div>
        </main>
      </StudioShell>
    );
  }

  if (error || !setup || !scene) {
    return (
      <StudioShell>
        <StudioHeader backHref={`/stories/${storyId}/characters`} backLabel="Characters" context="Scene Setup" />
        <main className="mx-auto max-w-3xl px-5 py-16 lg:px-8">
          <Notice tone="error">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <span>{error ?? "Scene Setup is unavailable."}</span>
              <Button variant="secondary" onClick={() => void load()}>Try again</Button>
            </div>
          </Notice>
        </main>
      </StudioShell>
    );
  }

  const previous = sceneIndex > 0 ? scenes[sceneIndex - 1] : null;
  const next = sceneIndex >= 0 && sceneIndex < scenes.length - 1 ? scenes[sceneIndex + 1] : null;

  return (
    <StudioShell>
      <StudioHeader
        backHref={`/stories/${storyId}/characters`}
        backLabel="Characters"
        context={outline?.story.title}
        actions={<SaveState state={saving ? "saving" : notice && !dirty ? "saved" : hasChanges ? "unsaved" : "saved"} />}
      />

      <main aria-labelledby="scene-setup-heading" className="mx-auto max-w-7xl px-5 py-8 lg:px-8">
        <div className="mb-3 flex flex-wrap items-center gap-2 text-xs font-semibold text-[var(--ss-muted)]">
          <span className="text-slate-400">Project</span><span aria-hidden="true">→</span><Link href={`/stories/${storyId}/outline`} className="hover:text-[var(--ss-text)]">Story</Link><span aria-hidden="true">→</span><span className="text-[var(--ss-text)]">Scene</span>
        </div>
        <div className="mb-6 flex flex-wrap items-center gap-2 text-xs font-semibold text-[var(--ss-muted)]">
          <Link href={`/stories/${storyId}/outline`} className="hover:text-[var(--ss-text)]">Outline</Link>
          <span aria-hidden="true">→</span>
          <Link href={`/stories/${storyId}/characters`} className="hover:text-[var(--ss-text)]">Characters</Link>
          <span aria-hidden="true" className="text-[var(--ss-primary)]">→ Scene Setup</span>
          <span aria-hidden="true">→ Editor</span>
        </div>

        <section className="rounded-[20px] border border-[var(--ss-border)] bg-white p-6 shadow-[var(--ss-shadow-raised)] sm:p-7">
          <div className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
            <div className="max-w-4xl">
              <p className="text-xs font-semibold uppercase tracking-[.16em] text-[var(--ss-primary)]">Scene {scene.orderIndex} · Staging board</p>
              <h1 id="scene-setup-heading" className="mt-2 text-3xl font-bold tracking-tight sm:text-4xl">{scene.title}</h1>
              <p className="mt-3 max-w-3xl text-[15px] leading-7 text-[var(--ss-muted)]">{scene.summary}</p>
              <p className="mt-4 inline-flex rounded-full bg-slate-100 px-3 py-1.5 text-xs font-semibold text-slate-700">
                Planned duration · {scene.durationSeconds}s · from Outline
              </p>
            </div>
            <div className="flex flex-wrap gap-2">
              <Button variant="secondary" disabled={!previous} onClick={() => previous && router.push(`/stories/${storyId}/scene-setup?scene=${previous.id}`)}>← Previous</Button>
              <Button variant="secondary" disabled={!next} onClick={() => next && router.push(`/stories/${storyId}/scene-setup?scene=${next.id}`)}>Next →</Button>
            </div>
          </div>
          {notice && <div className="mt-5"><Notice tone={notice === "Scene setup saved." ? "success" : "error"}>{notice}</Notice></div>}
        </section>

        <section aria-labelledby="staging-heading" className="mt-8">
          <div className="flex flex-wrap items-end justify-between gap-3">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[.16em] text-[var(--ss-primary)]">Stage the scene</p>
              <h2 id="staging-heading" className="mt-1 text-2xl font-bold tracking-tight">What belongs in this moment?</h2>
            </div>
            <p className="text-sm text-[var(--ss-muted)]">{selected.length} character{selected.length === 1 ? "" : "s"} · {setup.props.length} prop{setup.props.length === 1 ? "" : "s"}</p>
          </div>

          <div className="mt-5 grid gap-5 xl:grid-cols-[1.35fr_.85fr]">
            <div className="space-y-5">
              <Card className="p-5 sm:p-6">
                <SectionHeader eyebrow="Setting" title="Background" hint="Choose from the controlled preset library. This is a scene choice, not generated media." />
                <div className="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-3">
                  {setup.backgroundOptions.map(p => {
                    const active = background === p.key;
                    return (
                      <button
                        type="button"
                        key={p.key}
                        aria-pressed={active}
                        onClick={() => change(setBackground, p.key)}
                        className={`rounded-[14px] border p-2 text-left transition duration-150 hover:-translate-y-0.5 focus:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ss-primary)] focus-visible:ring-offset-2 ${active ? "border-[var(--ss-primary)] bg-indigo-50 ring-2 ring-indigo-100" : "border-[var(--ss-border)] bg-white hover:border-indigo-200"}`}
                      >
                        <ScenePresetVisual name={p.name} category={p.category} />
                        <span className="mt-2 block text-sm font-semibold">{p.name}</span>
                        <span className="mt-0.5 block text-xs text-[var(--ss-muted)]">{active ? "Selected" : "Available"}</span>
                      </button>
                    );
                  })}
                </div>
                {background && <Button variant="ghost" className="mt-3 text-red-700 hover:bg-red-50" onClick={() => change(setBackground, null)}>Clear background</Button>}
              </Card>

              <Card className="p-5 sm:p-6">
                <SectionHeader eyebrow="Cast" title="Characters in this scene" hint="Only Story Characters are available. They come from the reusable Project Character Library; each Character can appear once in this scene." />
                <div className="mt-5 grid gap-3 sm:grid-cols-2">
                  {chars.map(character => {
                    const active = selected.includes(character.id);
                    return (
                      <button
                        type="button"
                        key={character.id}
                        aria-pressed={active}
                        onClick={() => change(setSelected, active ? selected.filter(id => id !== character.id) : [...selected, character.id])}
                        className={`flex min-w-0 items-center gap-3 rounded-[14px] border p-3 text-left transition duration-150 hover:-translate-y-0.5 focus:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ss-primary)] focus-visible:ring-offset-2 ${active ? "border-[var(--ss-primary)] bg-indigo-50 ring-1 ring-indigo-100" : "border-[var(--ss-border)] bg-white hover:border-indigo-200"}`}
                      >
                        <CharacterMark character={character} />
                        <span className="min-w-0 flex-1">
                          <span className="block truncate text-sm font-semibold">{character.name}</span>
                          <span className="mt-0.5 block truncate text-xs text-[var(--ss-muted)]">{character.roleDescription}</span>
                        </span>
                        <span className="shrink-0 text-xs font-semibold">{active ? "In scene" : "Add"}</span>
                      </button>
                    );
                  })}
                </div>
                {!chars.length && (
                  <EmptyState title="No Story Characters yet" description="Add characters to this Story before staging them in a scene." />
                )}
              </Card>

              <Card className="p-5 sm:p-6">
                <SectionHeader eyebrow="Set dressing" title="Props" hint="Scene-local ingredients. Add the same preset more than once when the scene needs it." />
                <div className="mt-5 flex flex-wrap gap-2">
                  {setup.propOptions.map(prop => (
                    <Button key={prop.key} variant="secondary" onClick={() => void addProp(prop.key)}>+ {prop.name}</Button>
                  ))}
                </div>
                {setup.props.length ? (
                  <div className="mt-5 grid gap-2 sm:grid-cols-2">
                    {setup.props.map(prop => (
                      <div key={prop.id} className="flex items-center gap-3 rounded-[12px] border border-[var(--ss-border)] bg-[var(--ss-surface-elevated)] px-3 py-2.5">
                        <span className="grid h-9 w-9 place-items-center rounded-lg bg-slate-100 text-xs font-bold text-slate-600">{prop.orderIndex}</span>
                        <span className="min-w-0 flex-1">
                          <span className="block text-sm font-semibold">{setup.propOptions.find(x => x.key === prop.propPresetKey)?.name ?? prop.propPresetKey}</span>
                          <span className="text-[11px] text-[var(--ss-muted)]">Scene instance</span>
                        </span>
                        <IconButton aria-label={`Remove ${prop.propPresetKey} prop`} title="Remove prop" onClick={() => void removeProp(prop.id)}>×</IconButton>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="mt-5 rounded-[12px] border border-dashed border-slate-300 bg-[var(--ss-surface-elevated)] p-5 text-sm text-[var(--ss-muted)]">No props yet. Add a few scene ingredients above.</p>
                )}
              </Card>
            </div>

            <div className="space-y-5">
              <Card className="p-5 sm:p-6">
                <SectionHeader eyebrow="Story beats" title="Dialogue" hint="Manual dialogue only. Character and Narrator are the two supported speaker types." />
                <div className="mt-5 space-y-3">
                  {dialogue.map((line, index) => (
                    <div key={index} className="rounded-[14px] border border-[var(--ss-border)] bg-[var(--ss-surface-elevated)] p-3">
                      <div className="flex flex-wrap gap-2">
                        <Select aria-label={`Dialogue ${index + 1} speaker type`} value={line.speakerType} onChange={e => change(setDialogue, dialogue.map((x,i) => i === index ? { ...x, speakerType: e.target.value as Line["speakerType"], characterId: e.target.value === "NARRATOR" ? null : x.characterId } : x))} className="w-auto min-w-28" >
                          <option value="NARRATOR">Narrator</option>
                          <option value="CHARACTER">Character</option>
                        </Select>
                        {line.speakerType === "CHARACTER" && (
                          <Select aria-label={`Dialogue ${index + 1} character`} value={line.characterId ?? ""} onChange={e => change(setDialogue, dialogue.map((x,i) => i === index ? { ...x, characterId: e.target.value || null } : x))} className="min-w-0 flex-1">
                            <option value="">Select Character</option>
                            {selected.map(id => <option key={id} value={id}>{charMap.get(id)?.name ?? "Character"}</option>)}
                          </Select>
                        )}
                        <div className="ml-auto flex gap-1">
                          <IconButton aria-label={`Move dialogue ${index + 1} up`} title="Move up" disabled={index === 0} onClick={() => change(setDialogue, move(dialogue, index, -1))}>↑</IconButton>
                          <IconButton aria-label={`Move dialogue ${index + 1} down`} title="Move down" disabled={index === dialogue.length - 1} onClick={() => change(setDialogue, move(dialogue, index, 1))}>↓</IconButton>
                          <IconButton aria-label={`Delete dialogue ${index + 1}`} title="Delete line" onClick={() => change(setDialogue, dialogue.filter((_,i) => i !== index))}>×</IconButton>
                        </div>
                      </div>
                      <Textarea aria-label={`Dialogue ${index + 1} text`} value={line.text} onChange={e => change(setDialogue, dialogue.map((x,i) => i === index ? { ...x, text: e.target.value } : x))} rows={3} placeholder={line.speakerType === "NARRATOR" ? "Narration…" : "What does this Character say?"} className="mt-3" />
                    </div>
                  ))}
                </div>
                <div className="mt-4 flex items-center justify-between gap-3">
                  {!dialogue.length ? <p className="text-sm text-[var(--ss-muted)]">No dialogue yet. Stage the conversation when you&apos;re ready.</p> : <p className="text-xs text-[var(--ss-muted)]">{dialogue.length} line{dialogue.length === 1 ? "" : "s"}</p>}
                  <Button variant="secondary" onClick={() => change(setDialogue, [...dialogue, { speakerType: "NARRATOR", characterId: null, text: "" }])}>+ Add line</Button>
                </div>
              </Card>

              <Card className="p-5 sm:p-6">
                <SectionHeader eyebrow="Intent" title="Action intent" hint="Actions describe what a Character should do in the scene. They are intent, not animation." />
                <div className="mt-5 space-y-3">
                  {actions.map((item, index) => (
                    <div key={index} className="rounded-[14px] border border-[var(--ss-border)] bg-[var(--ss-surface-elevated)] p-3">
                      <div className="grid gap-2 sm:grid-cols-[1fr_auto_auto]">
                        <Select aria-label={`Action ${index + 1} character`} value={item.characterId} onChange={e => change(setActions, actions.map((x,i) => i === index ? { ...x, characterId: e.target.value } : x))}>
                          {selected.map(id => <option key={id} value={id}>{charMap.get(id)?.name ?? "Character"}</option>)}
                        </Select>
                        <Select aria-label={`Action ${index + 1} action`} value={item.action} onChange={e => change(setActions, actions.map((x,i) => i === index ? { ...x, action: e.target.value as Act["action"] } : x))}>
                          {ACTIONS.map(action => <option key={action} value={action}>{action}</option>)}
                        </Select>
                        <div className="flex gap-1">
                          <IconButton aria-label={`Move action ${index + 1} up`} title="Move up" disabled={index === 0} onClick={() => change(setActions, move(actions, index, -1))}>↑</IconButton>
                          <IconButton aria-label={`Move action ${index + 1} down`} title="Move down" disabled={index === actions.length - 1} onClick={() => change(setActions, move(actions, index, 1))}>↓</IconButton>
                          <IconButton aria-label={`Delete action ${index + 1}`} title="Delete action" onClick={() => change(setActions, actions.filter((_,i) => i !== index))}>×</IconButton>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
                <div className="mt-4 flex items-center justify-between gap-3">
                  {!actions.length ? <p className="text-sm text-[var(--ss-muted)]">No action intent yet.</p> : <p className="text-xs text-[var(--ss-muted)]">{actions.length} action{actions.length === 1 ? "" : "s"}</p>}
                  <Button variant="secondary" onClick={() => selected[0] ? change(setActions, [...actions, { characterId: selected[0], action: "IDLE" }]) : setNotice("Select a Character before adding an action.")}>+ Add action</Button>
                </div>
              </Card>
            </div>
          </div>
        </section>

        <footer className="sticky bottom-0 z-10 -mx-5 mt-8 border-t border-[var(--ss-border)] bg-[var(--ss-bg)]/95 px-5 py-4 backdrop-blur-sm lg:-mx-8 lg:px-8">
          <div className="mx-auto flex max-w-7xl flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-xs text-[var(--ss-muted)]">Stage the ingredients here, then direct the scene in the Editor.</p>
            <div className="flex flex-col-reverse gap-2 sm:flex-row">
              <Button variant="secondary" onClick={() => void save()} disabled={saving || !dirty}>{saving ? "Saving…" : "Save Changes"}</Button>
              <Link href={`/stories/${storyId}/editor?scene=${scene.id}`} className="inline-flex min-h-10 items-center justify-center rounded-[10px] bg-[var(--ss-primary)] px-4 py-2 text-sm font-semibold text-white shadow-sm transition duration-150 hover:bg-[var(--ss-primary-strong)] focus:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ss-primary)] focus-visible:ring-offset-2">Open Editor →</Link>
            </div>
          </div>
        </footer>
      </main>
    </StudioShell>
  );
}
