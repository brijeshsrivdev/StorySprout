/* eslint-disable react-hooks/set-state-in-effect */
"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import {
  ApiError,
  createOutlineScene,
  deleteOutlineScene,
  generateOutline,
  getOutline,
  OutlineScene,
  reorderOutlineScenes,
  StoryOutline,
  updateOutlineScene,
} from "../../../../lib/api";
import { formatDuration, getVarianceState } from "../../../../lib/outline";
import {
  AiBadge,
  Button,
  Card,
  EmptyState,
  IconButton,
  Notice,
  SaveState,
  StudioHeader,
  StudioShell,
} from "../../../../components/ui";

const newId = () => "new-" + crypto.randomUUID();

function varianceLabel(state: ReturnType<typeof getVarianceState>) {
  if (state === "approximately-on-target") return "Approximately on target";
  if (state === "under-target") return "Under target";
  return "Over target";
}

function varianceTone(state: ReturnType<typeof getVarianceState>) {
  if (state === "approximately-on-target") return "border-emerald-100 bg-emerald-50/70";
  if (state === "under-target") return "border-amber-100 bg-amber-50/70";
  return "border-red-100 bg-red-50/70";
}

export default function StoryOutlinePage() {
  const { storyId } = useParams<{ storyId: string }>();
  const [outline, setOutline] = useState<StoryOutline | null>(null);
  const [scenes, setScenes] = useState<OutlineScene[]>([]);
  const [deleted, setDeleted] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [saving, setSaving] = useState(false);
  const [dirty, setDirty] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [creatorShaped, setCreatorShaped] = useState(false);

  async function loadOutline(isActive: () => boolean = () => true) {
    setLoading(true);
    setError(null);
    try {
      const result = await getOutline(storyId);
      if (!isActive()) return;
      setOutline(result);
      setScenes(result.scenes);
      setDeleted([]);
      setDirty(false);
    } catch (e) {
      if (!isActive()) return;
      setError(
        e instanceof ApiError && e.status === 404
          ? "Story not found."
          : "We couldn't load this outline.",
      );
    } finally {
      if (isActive()) setLoading(false);
    }
  }

  useEffect(() => {
    // Strict Mode / re-mounts run this effect twice; a stale response must not
    // clobber locally-added scenes, so ignore all but the latest invocation.
    let active = true;
    void loadOutline(() => active);
    return () => {
      active = false;
    };
  }, [storyId]);

  const planned = useMemo(
    () =>
      scenes.reduce(
        (total, scene) =>
          total + (Number.isFinite(scene.durationSeconds) ? scene.durationSeconds : 0),
        0,
      ),
    [scenes],
  );

  if (loading) {
    return (
      <StudioShell>
        <StudioHeader backHref="/" backLabel="Stories" context="Story Outline" />
        <main className="mx-auto max-w-6xl px-5 py-10 lg:px-8">
          <div className="rounded-[18px] border border-[var(--ss-border)] bg-white p-8 shadow-[var(--ss-shadow-raised)]" aria-live="polite">
            <div className="h-3 w-28 animate-pulse rounded bg-slate-200" />
            <div className="mt-4 h-9 max-w-md animate-pulse rounded bg-slate-200" />
            <div className="mt-3 h-4 max-w-xl animate-pulse rounded bg-slate-100" />
            <div className="mt-10 grid gap-4 md:grid-cols-3">
              {[1, 2, 3].map((item) => (
                <div key={item} className="h-24 animate-pulse rounded-[16px] bg-slate-100" />
              ))}
            </div>
          </div>
        </main>
      </StudioShell>
    );
  }

  if (error && !outline) {
    return (
      <StudioShell>
        <StudioHeader backHref="/" backLabel="Stories" context="Story Outline" />
        <main className="mx-auto max-w-3xl px-5 py-16 lg:px-8">
          <Notice tone="error">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <span>{error}</span>
              <Button variant="secondary" onClick={() => void loadOutline()}>
                Try again
              </Button>
            </div>
          </Notice>
        </main>
      </StudioShell>
    );
  }

  if (!outline) return null;

  const target = outline.targetDurationSeconds;
  const variance = planned - target;
  const varianceState = getVarianceState(variance);
  const hasPersistedScenes = outline.scenes.length > 0;

  function edit(id: string, patch: Partial<OutlineScene>) {
    setScenes((current) =>
      current.map((scene) => (scene.id === id ? { ...scene, ...patch } : scene)),
    );
    setDirty(true);
    setCreatorShaped(true);
    setSaveError(null);
  }

  function add() {
    const now = new Date().toISOString();
    setScenes((current) => [
      ...current,
      {
        id: newId(),
        storyId,
        orderIndex: current.length + 1,
        title: "New Scene",
        summary: "Describe what happens.",
        durationSeconds: 10,
        createdAt: now,
        updatedAt: now,
      },
    ]);
    setDirty(true);
    setCreatorShaped(true);
    setSaveError(null);
  }

  function remove(id: string) {
    if (!confirm("Delete this scene?")) return;

    setScenes((current) =>
      current
        .filter((scene) => scene.id !== id)
        .map((scene, index) => ({ ...scene, orderIndex: index + 1 })),
    );
    if (!id.startsWith("new-")) setDeleted((current) => [...current, id]);
    setDirty(true);
    setCreatorShaped(true);
    setSaveError(null);
  }

  function move(index: number, direction: -1 | 1) {
    const targetIndex = index + direction;
    if (targetIndex < 0 || targetIndex >= scenes.length) return;

    const next = [...scenes];
    [next[index], next[targetIndex]] = [next[targetIndex], next[index]];
    setScenes(next.map((scene, position) => ({ ...scene, orderIndex: position + 1 })));
    setDirty(true);
    setCreatorShaped(true);
    setSaveError(null);
  }

  function isInvalid(scene: OutlineScene) {
    return (
      !scene.title.trim() ||
      !scene.summary.trim() ||
      !Number.isInteger(scene.durationSeconds) ||
      scene.durationSeconds < 1
    );
  }

  async function save() {
    if (!scenes.length || scenes.some(isInvalid)) {
      setSaveError("Complete every scene with a title, summary, and duration of at least 1 second.");
      return;
    }

    setSaving(true);
    setSaveError(null);

    try {
      for (const id of deleted) {
        await deleteOutlineScene(storyId, id);
      }

      const persisted: OutlineScene[] = [];
      for (const scene of scenes) {
        if (scene.id.startsWith("new-")) {
          persisted.push(
            await createOutlineScene(storyId, {
              title: scene.title.trim(),
              summary: scene.summary.trim(),
              durationSeconds: scene.durationSeconds,
            }),
          );
        } else {
          const old = outline?.scenes.find((candidate) => candidate.id === scene.id);
          const unchanged =
            old &&
            old.title === scene.title.trim() &&
            old.summary === scene.summary.trim() &&
            old.durationSeconds === scene.durationSeconds;

          persisted.push(
            unchanged
              ? scene
              : await updateOutlineScene(storyId, scene.id, {
                  title: scene.title.trim(),
                  summary: scene.summary.trim(),
                  durationSeconds: scene.durationSeconds,
                }),
          );
        }
      }

      await reorderOutlineScenes(storyId, persisted.map((scene) => scene.id));
      const result = await getOutline(storyId);
      setOutline(result);
      setScenes(result.scenes);
      setDeleted([]);
      setDirty(false);
    } catch (e) {
      setSaveError(e instanceof ApiError ? e.message : "Couldn't save changes.");
    } finally {
      setSaving(false);
    }
  }

  async function generate() {
    setGenerating(true);
    setError(null);
    try {
      const result = await generateOutline(storyId);
      setOutline(result);
      setScenes(result.scenes);
      setDirty(false);
      setCreatorShaped(false);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Outline generation failed.");
    } finally {
      setGenerating(false);
    }
  }

  return (
    <StudioShell>
      <StudioHeader
        backHref="/"
        backLabel="Stories"
        context={outline.story.title}
        actions={
          <SaveState
            state={saving ? "saving" : saveError ? "error" : dirty ? "unsaved" : "saved"}
          />
        }
      />

      <main aria-labelledby="story-outline-heading" className="mx-auto max-w-6xl px-5 py-10 lg:px-8">
        <header className="max-w-4xl">
          <div className="flex flex-wrap items-center gap-2">
            <p className="text-xs font-semibold uppercase tracking-[.16em] text-[var(--ss-primary)]">
              Shape the story
            </p>
            {outline.story.creationMode === "AI" && <AiBadge />}
          </div>
          <h1 id="story-outline-heading" className="mt-3 text-3xl font-bold tracking-tight sm:text-4xl">
            {outline.story.title}
          </h1>
          <div className="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-sm text-[var(--ss-muted)]">
            <span>{outline.story.targetAge.replace("_", "–")}</span>
            <span>{outline.story.durationMinutes} min target</span>
            <span>{outline.story.visualStyle}</span>
            <span>{outline.story.language === "ENGLISH" ? "English" : "Hindi"}</span>
          </div>
        </header>

        <section className="mt-8 grid gap-4 lg:grid-cols-[1fr_1.35fr]">
          <Card className="p-6">
            <p className="text-xs font-semibold uppercase tracking-[.14em] text-[var(--ss-muted)]">
              Story reference
            </p>
            <p className="mt-3 text-sm leading-6 text-slate-700">
              {outline.story.draftContent || outline.story.idea}
            </p>
          </Card>

          <div className="grid gap-3 sm:grid-cols-3">
            <Metric label="Target" value={formatDuration(target)} detail="Planning reference" />
            <Metric
              label="Planned"
              value={formatDuration(planned)}
              detail={scenes.length + " " + (scenes.length === 1 ? "scene" : "scenes")}
            />
            <div className={"rounded-[16px] border p-5 " + varianceTone(varianceState)}>
              <p className="text-xs font-semibold uppercase tracking-[.14em] text-[var(--ss-muted)]">
                Variance
              </p>
              <p className="mt-2 text-2xl font-bold tracking-tight">
                {variance >= 0 ? "+" : "−"}{formatDuration(Math.abs(variance))}
              </p>
              <p className="mt-1 text-xs font-semibold">{varianceLabel(varianceState)}</p>
            </div>
          </div>
        </section>

        {(error || saveError) && (
          <div className="mt-6">
            <Notice tone="error">
              <div className="flex flex-wrap items-center justify-between gap-4">
                <span>{error || saveError}</span>
                {error && (
                  <Button
                    variant="secondary"
                    onClick={() => void generate()}
                    disabled={generating || hasPersistedScenes}
                  >
                    Try again
                  </Button>
                )}
              </div>
            </Notice>
          </div>
        )}

        {scenes.length > 0 && (
          <div className="mt-6">
            <Notice tone={creatorShaped ? "info" : "ai"}>
              <div>
                <p className="font-semibold">{creatorShaped ? "Creator-shaped outline" : "AI-generated starting point"}</p>
                <p className="mt-1 text-sm">{creatorShaped ? "Your edits are now the working story plan. Keep shaping the scenes before continuing." : "These scenes were proposed by AI. Review and shape them before treating the outline as your final story plan."}</p>
              </div>
            </Notice>
          </div>
        )}

        <section aria-labelledby="scenes-heading" className="mt-10">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[.14em] text-[var(--ss-muted)]">
                Narrative flow
              </p>
              <h2 id="scenes-heading" className="mt-1 text-xl font-bold tracking-tight">
                Storyboard
              </h2>
            </div>
            <div className="flex items-center gap-3">
              {scenes.length > 0 && (
                <p className="text-sm text-[var(--ss-muted)]">
                  {scenes.length} {scenes.length === 1 ? "scene" : "scenes"} in sequence
                </p>
              )}
              <Button variant="secondary" onClick={add}>
                + Add Scene
              </Button>
            </div>
          </div>

          {scenes.length === 0 ? (
            <div className="mt-5">
              <EmptyState
                icon="✦"
                title="Your story is ready for its first beats."
                description="Turn the story into an ordered sequence of scenes. You can review and edit every beat before moving into scene preparation."
                action={
                  <div className="flex flex-wrap items-center justify-center gap-3">
                    <Button variant="ai" onClick={() => void generate()} disabled={generating}>
                      {generating ? "Generating outline…" : "Generate Outline"}
                    </Button>
                    <Button variant="secondary" onClick={add} disabled={generating}>
                      + Add Scene
                    </Button>
                  </div>
                }
              />
            </div>
          ) : (
            <div className="relative mt-5 space-y-4">
              <div aria-hidden="true" className="absolute bottom-8 left-6 top-8 hidden w-px bg-slate-200 sm:block" />
              {scenes.map((scene, index) => {
                const invalid = isInvalid(scene);
                return (
                  <Card key={scene.id} className={"relative p-5 sm:p-6 " + (invalid ? "border-red-200" : "")}>
                    <div className="flex gap-4 sm:gap-6">
                      <div className="relative z-10 grid h-12 w-12 shrink-0 place-items-center rounded-full border border-[var(--ss-border)] bg-[var(--ss-surface-elevated)] text-sm font-bold text-[var(--ss-primary)] shadow-sm">
                        {index + 1}
                      </div>

                      <div className="min-w-0 flex-1">
                        <div className="grid gap-4 lg:grid-cols-[1fr_auto]">
                          <div>
                            <label htmlFor={"scene-title-" + scene.id} className="sr-only">
                              Scene {index + 1} title
                            </label>
                            <input
                              id={"scene-title-" + scene.id}
                              aria-label={"Scene " + (index + 1) + " title"}
                              value={scene.title}
                              onChange={(event) => edit(scene.id, { title: event.target.value })}
                              className="w-full rounded-[10px] border border-transparent bg-transparent px-0 py-1 text-lg font-bold tracking-tight outline-none transition focus:border-[var(--ss-primary)] focus:bg-white focus:px-3 focus:ring-2 focus:ring-indigo-100"
                            />
                            <label htmlFor={"scene-summary-" + scene.id} className="sr-only">
                              Scene {index + 1} summary
                            </label>
                            <textarea
                              id={"scene-summary-" + scene.id}
                              aria-label={"Scene " + (index + 1) + " summary"}
                              value={scene.summary}
                              onChange={(event) => edit(scene.id, { summary: event.target.value })}
                              rows={3}
                              className="mt-2 w-full resize-y rounded-[10px] border border-transparent bg-transparent px-0 py-1 text-sm leading-6 text-[var(--ss-muted)] outline-none transition focus:border-[var(--ss-primary)] focus:bg-white focus:px-3 focus:ring-2 focus:ring-indigo-100"
                            />
                          </div>

                          <div className="flex items-start gap-2 lg:pl-4">
                            <label htmlFor={"scene-duration-" + scene.id} className="flex items-center gap-2 rounded-[10px] border border-[var(--ss-border)] bg-[var(--ss-surface-elevated)] px-3 py-2 text-xs font-semibold text-[var(--ss-muted)]">
                              <span>Planned</span>
                              <input
                                id={"scene-duration-" + scene.id}
                                aria-label={"Scene " + (index + 1) + " duration"}
                                type="number"
                                min="1"
                                step="1"
                                value={scene.durationSeconds}
                                onChange={(event) =>
                                  edit(scene.id, { durationSeconds: Number(event.target.value) })
                                }
                                className="w-20 bg-transparent text-right text-sm font-bold text-[var(--ss-text)] outline-none"
                              />
                              <span>sec</span>
                            </label>
                          </div>
                        </div>

                        {invalid && (
                          <p role="alert" className="mt-2 text-xs font-medium text-[var(--ss-error)]">
                            Add a title and summary, and use a whole-number duration of at least 1 second.
                          </p>
                        )}

                        <div className="mt-4 flex flex-wrap items-center gap-2 border-t border-[var(--ss-border)] pt-4">
                          <IconButton
                            onClick={() => move(index, -1)}
                            disabled={index === 0}
                            aria-label={"Move scene " + (index + 1) + " up"}
                            title="Move scene up"
                          >
                            ↑
                          </IconButton>
                          <IconButton
                            onClick={() => move(index, 1)}
                            disabled={index === scenes.length - 1}
                            aria-label={"Move scene " + (index + 1) + " down"}
                            title="Move scene down"
                          >
                            ↓
                          </IconButton>
                          <Button variant="danger" onClick={() => remove(scene.id)}>
                            Delete scene
                          </Button>
                          <span className="ml-auto text-xs text-[var(--ss-muted)]">
                            Scene {index + 1} · {formatDuration(scene.durationSeconds)}
                          </span>
                        </div>
                      </div>
                    </div>
                  </Card>
                );
              })}

              <button
                type="button"
                onClick={add}
                className="w-full rounded-[16px] border border-dashed border-slate-300 bg-white px-5 py-5 text-sm font-semibold text-[var(--ss-primary)] transition duration-150 hover:border-indigo-300 hover:bg-indigo-50/30 focus:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ss-primary)] focus-visible:ring-offset-2"
              >
                + Add Scene
              </button>
            </div>
          )}
        </section>

        <footer className="sticky bottom-0 z-10 -mx-5 mt-8 border-t border-[var(--ss-border)] bg-[var(--ss-bg)]/95 px-5 py-4 backdrop-blur-sm lg:-mx-8 lg:px-8">
          <div className="mx-auto flex max-w-6xl flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex flex-wrap items-center gap-2">
              {scenes.length > 0 && (
                <Button
                  variant="secondary"
                  onClick={() => void generate()}
                  disabled={generating || hasPersistedScenes}
                  aria-describedby={hasPersistedScenes ? "generation-note" : undefined}
                >
                  {generating ? "Generating…" : "Generate Outline"}
                </Button>
              )}
              {hasPersistedScenes && (
                <span id="generation-note" className="text-xs text-[var(--ss-muted)]">
                  Existing scenes are protected from regeneration.
                </span>
              )}
            </div>

            <div className="flex flex-col-reverse gap-2 sm:flex-row">
              <Button
                variant="secondary"
                onClick={() => void save()}
                disabled={saving || !dirty || !scenes.length}
              >
                {saving ? "Saving…" : "Save Changes"}
              </Button>
              <Link
                href={"/stories/" + storyId + "/characters"}
                aria-disabled={dirty || !scenes.length}
                onClick={(event) => {
                  if (dirty || !scenes.length) event.preventDefault();
                }}
                className={"inline-flex min-h-10 items-center justify-center rounded-[10px] bg-[var(--ss-primary)] px-4 py-2 text-sm font-semibold text-white shadow-sm transition duration-150 hover:bg-[var(--ss-primary-strong)] focus:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ss-primary)] focus-visible:ring-offset-2 " + (dirty || !scenes.length ? "pointer-events-none opacity-45" : "")}
              >
                Continue to Characters →
              </Link>
            </div>
          </div>
        </footer>
      </main>
    </StudioShell>
  );
}

function Metric({ label, value, detail }: { label: string; value: string; detail: string }) {
  return (
    <div className="rounded-[16px] border border-[var(--ss-border)] bg-white p-5 shadow-[var(--ss-shadow-raised)]">
      <p className="text-xs font-semibold uppercase tracking-[.14em] text-[var(--ss-muted)]">{label}</p>
      <p className="mt-2 text-2xl font-bold tracking-tight">{value}</p>
      <p className="mt-1 text-xs text-[var(--ss-muted)]">{detail}</p>
    </div>
  );
}
