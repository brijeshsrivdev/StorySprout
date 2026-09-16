"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import { ApiError, createOutlineScene, deleteOutlineScene, generateOutline, getOutline, OutlineScene, reorderOutlineScenes, StoryOutline, updateOutlineScene } from "../../../../lib/api";
import { formatDuration, getVarianceState } from "../../../../lib/outline";

const newId = () => `new-${crypto.randomUUID()}`;

function varianceState(variance: number) {
  const state = getVarianceState(variance);
  if (state === "approximately-on-target") return { label: "Approximately on target", className: "border-amber-200 bg-amber-50 text-amber-800" };
  if (state === "under-target") return { label: "Under target", className: "border-sky-200 bg-sky-50 text-sky-800" };
  return { label: "Over target", className: "border-orange-200 bg-orange-50 text-orange-800" };
}

export default function StoryOutlinePage() {
  const { storyId } = useParams<{ storyId: string }>();
  const [outline, setOutline] = useState<StoryOutline | null>(null);
  const [scenes, setScenes] = useState<OutlineScene[]>([]);
  const [deletedIds, setDeletedIds] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [saving, setSaving] = useState(false);
  const [dirty, setDirty] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saveError, setSaveError] = useState<string | null>(null);

  async function load() {
    setLoading(true); setError(null);
    try { const result = await getOutline(storyId); setOutline(result); setScenes(result.scenes); setDeletedIds([]); setDirty(false); }
    catch (e) { setError(e instanceof ApiError && e.status === 404 ? "Story not found." : "We couldn't load this outline."); }
    finally { setLoading(false); }
  }

  useEffect(() => { void load(); }, [storyId]);

  const plannedDuration = useMemo(() => scenes.reduce((total, scene) => total + (Number.isFinite(scene.durationSeconds) ? scene.durationSeconds : 0), 0), [scenes]);
  const targetDuration = outline?.targetDurationSeconds ?? 0;
  const variance = plannedDuration - targetDuration;
  const durationStatus = varianceState(variance);

  function updateLocal(id: string, patch: Partial<OutlineScene>) {
    setScenes(current => current.map(scene => scene.id === id ? { ...scene, ...patch } : scene));
    setDirty(true); setSaveError(null);
  }

  function addScene() {
    const now = new Date().toISOString();
    setScenes(current => [...current, { id: newId(), storyId, orderIndex: current.length + 1, title: "New Scene", summary: "Describe what happens in this scene.", durationSeconds: 10, createdAt: now, updatedAt: now }]);
    setDirty(true); setSaveError(null);
  }

  function removeScene(id: string) {
    if (!window.confirm("Delete this scene? This removes the scene from the outline when you save.")) return;
    setScenes(current => current.filter(scene => scene.id !== id).map((scene, index) => ({ ...scene, orderIndex: index + 1 })));
    if (!id.startsWith("new-")) setDeletedIds(current => [...current, id]);
    setDirty(true); setSaveError(null);
  }

  function moveScene(index: number, direction: -1 | 1) {
    const target = index + direction;
    if (target < 0 || target >= scenes.length) return;
    const next = [...scenes]; [next[index], next[target]] = [next[target], next[index]];
    setScenes(next.map((scene, position) => ({ ...scene, orderIndex: position + 1 })));
    setDirty(true); setSaveError(null);
  }

  async function save() {
    if (!outline || scenes.length === 0 || scenes.some(scene => !scene.title.trim() || !scene.summary.trim() || !Number.isInteger(scene.durationSeconds) || scene.durationSeconds < 1)) return;
    setSaving(true); setSaveError(null);
    try {
      for (const id of deletedIds) await deleteOutlineScene(storyId, id);
      const persisted: OutlineScene[] = [];
      for (const scene of scenes) {
        if (scene.id.startsWith("new-")) {
          persisted.push(await createOutlineScene(storyId, { title: scene.title.trim(), summary: scene.summary.trim(), durationSeconds: scene.durationSeconds }));
        } else {
          const original = outline.scenes.find(item => item.id === scene.id);
          if (!original || original.title !== scene.title.trim() || original.summary !== scene.summary.trim() || original.durationSeconds !== scene.durationSeconds) persisted.push(await updateOutlineScene(storyId, scene.id, { title: scene.title.trim(), summary: scene.summary.trim(), durationSeconds: scene.durationSeconds }));
          else persisted.push(scene);
        }
      }
      await reorderOutlineScenes(storyId, persisted.map(scene => scene.id));
      const refreshed = await getOutline(storyId);
      setOutline(refreshed); setScenes(refreshed.scenes); setDeletedIds([]); setDirty(false);
    } catch (e) {
      setSaveError(e instanceof ApiError ? e.message : "We couldn't save your changes. Please try again.");
    } finally { setSaving(false); }
  }

  async function generate() {
    setGenerating(true); setError(null);
    try { const result = await generateOutline(storyId); setOutline(result); setScenes(result.scenes); setDirty(false); }
    catch (e) { setError(e instanceof ApiError ? e.message : "Outline generation failed. Your story was not changed."); }
    finally { setGenerating(false); }
  }

  if (loading) return <main className="min-h-screen bg-slate-50 p-10"><div className="mx-auto max-w-6xl rounded-2xl border border-slate-200 bg-white p-8 text-slate-600">Loading Story Outline…</div></main>;
  if (error && !outline) return <main className="min-h-screen bg-slate-50 p-10"><div className="mx-auto max-w-3xl rounded-2xl border border-red-200 bg-red-50 p-8 text-red-700" role="alert">{error}<div className="mt-5"><Link href="/" className="font-semibold underline">Back to stories</Link></div></div></main>;
  if (!outline) return null;

  const story = outline.story;
  const hasSavedOutline = outline.scenes.length > 0;
  const invalid = scenes.length === 0 || scenes.some(scene => !scene.title.trim() || !scene.summary.trim() || !Number.isInteger(scene.durationSeconds) || scene.durationSeconds < 1);

  return <main className="min-h-screen bg-slate-50 text-slate-950">
    <div className="mx-auto max-w-6xl px-6 py-8 md:px-8">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div><Link href="/" className="text-sm font-medium text-slate-500 hover:text-slate-900">← Stories</Link><p className="mt-5 text-sm font-semibold tracking-wide text-indigo-600">STORY OUTLINE</p><h1 className="mt-1 text-3xl font-bold">{story.title}</h1><p className="mt-2 text-sm text-slate-600">{story.targetAge.replace("_", "–")} · {story.durationMinutes} min · {story.visualStyle} · {story.language === "ENGLISH" ? "English" : "Hindi"}</p></div>
        <div className="text-right text-sm"><p className="font-semibold">{saving ? "Saving…" : dirty ? "Unsaved changes" : "Saved"}</p><p className="text-slate-500">Target {formatDuration(targetDuration)}</p></div>
      </div>

      <section className="mt-8 grid gap-4 md:grid-cols-3">
        <Metric label="Story target" value={formatDuration(targetDuration)} />
        <Metric label="Planned" value={formatDuration(plannedDuration)} />
        <div className={`rounded-2xl border p-5 ${durationStatus.className}`}><p className="text-xs font-semibold uppercase tracking-wide">Variance</p><p className="mt-2 text-2xl font-bold">{variance >= 0 ? "+" : "−"}{formatDuration(Math.abs(variance))}</p><p className="mt-1 text-xs font-semibold">{durationStatus.label}</p></div>
      </section>

      <section className="mt-6 grid gap-6 lg:grid-cols-[minmax(0,1fr)_320px]">
        <div className="space-y-4">
          {error && <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{error}</div>}
          {saveError && <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{saveError}</div>}
          {scenes.length === 0 && <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-12 text-center"><h2 className="text-xl font-bold">No outline scenes</h2><p className="mt-2 text-sm text-slate-600">Generate an outline or add your first scene.</p></div>}
          {scenes.map((scene, index) => <article key={scene.id} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <div className="flex items-start gap-4"><div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-slate-100 text-sm font-bold">{index + 1}</div><div className="min-w-0 flex-1 space-y-3">
              <input aria-label={`Scene ${index + 1} title`} value={scene.title} onChange={e => updateLocal(scene.id, { title: e.target.value })} className="w-full rounded-lg border border-slate-300 px-3 py-2 font-semibold outline-none focus:border-indigo-500" />
              <textarea aria-label={`Scene ${index + 1} summary`} value={scene.summary} onChange={e => updateLocal(scene.id, { summary: e.target.value })} rows={3} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:border-indigo-500" />
              <div className="flex flex-wrap items-center gap-3"><label className="text-sm font-semibold">Planned duration <input aria-label={`Scene ${index + 1} duration`} type="number" min="1" step="1" value={scene.durationSeconds} onChange={e => updateLocal(scene.id, { durationSeconds: Number(e.target.value) })} className="ml-2 w-24 rounded-lg border border-slate-300 px-3 py-2 font-normal" /> sec</label><span className="text-xs text-slate-500">Planning estimate only</span></div>
            </div><div className="flex shrink-0 flex-col gap-2"><button type="button" aria-label={`Move scene ${index + 1} up`} disabled={index === 0} onClick={() => moveScene(index, -1)} className="rounded-lg border border-slate-200 px-2 py-1 text-xs disabled:opacity-30">↑</button><button type="button" aria-label={`Move scene ${index + 1} down`} disabled={index === scenes.length - 1} onClick={() => moveScene(index, 1)} className="rounded-lg border border-slate-200 px-2 py-1 text-xs disabled:opacity-30">↓</button><button type="button" onClick={() => removeScene(scene.id)} className="rounded-lg border border-red-200 px-2 py-1 text-xs font-semibold text-red-600">Delete</button></div></div>
          </article>)}
          <button type="button" onClick={addScene} className="w-full rounded-2xl border border-dashed border-slate-300 bg-white p-5 text-sm font-semibold text-slate-700 hover:border-indigo-300 hover:text-indigo-700">+ Add Scene</button>
        </div>

        <aside className="h-fit rounded-2xl border border-slate-200 bg-white p-5 shadow-sm lg:sticky lg:top-6">
          <h2 className="font-bold">Story reference</h2><p className="mt-3 whitespace-pre-wrap text-sm leading-6 text-slate-600">{story.draftContent || story.idea}</p>
          <div className="mt-6 space-y-2"><button type="button" onClick={generate} disabled={generating || hasSavedOutline} className="w-full rounded-xl bg-indigo-600 px-4 py-3 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-40">{generating ? "Generating…" : hasSavedOutline ? "Outline generated" : "Generate Outline"}</button><button type="button" onClick={save} disabled={saving || invalid || !dirty} className="w-full rounded-xl bg-slate-950 px-4 py-3 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-40">{saving ? "Saving…" : "Save Changes"}</button><Link href={`/stories/${storyId}/scene-setup`} onClick={event => { if (dirty || invalid) event.preventDefault(); }} className={`block w-full rounded-xl border px-4 py-3 text-center text-sm font-semibold ${dirty || invalid ? "pointer-events-none border-slate-200 text-slate-400" : "border-slate-300 text-slate-800 hover:bg-slate-50"}`}>Continue to Scene Setup</Link></div>
          {invalid && scenes.length > 0 && <p className="mt-3 text-xs font-medium text-red-600">Fix every scene title, summary, and duration before saving.</p>}
          {scenes.length === 0 && <p className="mt-3 text-xs text-slate-500">A saved outline needs at least one scene.</p>}
          <p className="mt-5 text-xs leading-5 text-slate-500">Durations are planning estimates. Exact animation timing belongs to the future Editor/Timeline/Composition stage.</p>
        </aside>
      </section>
    </div>
  </main>;
}

function Metric({ label, value }: { label: string; value: string }) { return <div className="rounded-2xl border border-slate-200 bg-white p-5"><p className="text-xs font-semibold uppercase tracking-wide text-slate-500">{label}</p><p className="mt-2 text-2xl font-bold">{value}</p></div>; }
