"use client";

import Link from "next/link";
import { FormEvent, Suspense, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { ApiError, createStory } from "../../../lib/api";

const ages = [["3_5", "Ages 3–5"], ["6_8", "Ages 6–8"], ["9_12", "Ages 9–12"]] as const;
const durations = [1, 3, 5] as const;
const styles = [["2D", "2D"], ["3D", "3D"], ["HYBRID", "Hybrid"]] as const;
const languages = [["ENGLISH", "English"], ["HINDI", "Hindi"]] as const;

export default function StorySetupPage() { return <Suspense fallback={<main className="min-h-screen bg-slate-50 p-12"><div className="mx-auto max-w-3xl rounded-3xl border border-slate-200 bg-white p-8 text-slate-600">Loading story setup…</div></main>}><StorySetupForm /></Suspense>; }

function StorySetupForm() {
  const router = useRouter(); const params = useSearchParams();
  const projectId = params.get("projectId") ?? ""; const mode = params.get("mode") === "AI" ? "AI" : "BLANK";
  const [idea, setIdea] = useState(""); const [targetAge, setTargetAge] = useState(""); const [durationMinutes, setDurationMinutes] = useState<1 | 3 | 5>(3); const [visualStyle, setVisualStyle] = useState("2D"); const [language, setLanguage] = useState("ENGLISH");
  const [saving, setSaving] = useState(false); const [error, setError] = useState<string | null>(null); const [fieldError, setFieldError] = useState<string | null>(null); const [generatedDraft, setGeneratedDraft] = useState<string | null>(null);

  async function submit(event: FormEvent) { event.preventDefault(); if (saving) return; setFieldError(null); setError(null); if (!idea.trim()) { setFieldError("Enter a story idea."); return; } if (!targetAge) { setFieldError("Select a target age."); return; } if (!projectId) { setError("This story project is missing. Return to the dashboard and start again."); return; }
    setSaving(true); try { const story = await createStory(projectId, { creationMode: mode, idea: idea.trim(), targetAge: targetAge as "3_5" | "6_8" | "9_12", durationMinutes, visualStyle: visualStyle as "2D" | "3D" | "HYBRID", language: language as "ENGLISH" | "HINDI" }); setGeneratedDraft(story.draftContent); if (mode === "BLANK" || story.generationStatus === "COMPLETED") setTimeout(() => router.replace("/"), 500); else setSaving(false); }
    catch (e) { const apiError = e instanceof ApiError ? e : null; setError(apiError?.status === 422 ? "Story generation failed. Your setup was saved. Try again when you're ready." : "We couldn't save your story. Please try again."); setSaving(false); }
  }

  return <main className="min-h-screen bg-slate-50"><div className="mx-auto max-w-3xl px-8 py-12"><Link href="/create" className="text-sm font-medium text-slate-500 hover:text-slate-900">← Start over</Link><div className="mt-10"><p className="text-sm font-semibold text-indigo-600">STORY SETUP · {mode === "AI" ? "AI" : "BLANK"}</p><h1 className="mt-2 text-4xl font-bold">Tell us about your story</h1><p className="mt-3 text-slate-600">A few choices are all you need to get started.</p></div>
    <form onSubmit={submit} className="mt-10 space-y-7 rounded-3xl border border-slate-200 bg-white p-8 shadow-sm">
      <div><label htmlFor="idea" className="text-sm font-semibold">Story idea</label><textarea id="idea" aria-describedby={fieldError ? "setup-error" : undefined} value={idea} onChange={e => setIdea(e.target.value)} placeholder="A little rabbit learns to share…" rows={4} className="mt-2 w-full rounded-xl border border-slate-300 p-4 outline-none focus:border-indigo-500" /></div>
      <div><label htmlFor="target-age" className="text-sm font-semibold">Target age</label><select id="target-age" value={targetAge} onChange={e => setTargetAge(e.target.value)} className="mt-2 w-full rounded-xl border border-slate-300 bg-white p-3"><option value="">Select an age group</option>{ages.map(([v,l]) => <option key={v} value={v}>{l}</option>)}</select></div>
      <ChoiceGroup label="Duration" values={durations.map(String)} selected={String(durationMinutes)} onSelect={v => setDurationMinutes(Number(v) as 1 | 3 | 5)} suffix=" min" />
      <ChoiceGroup label="Visual style" values={styles.map(([v]) => v)} labels={styles.map(([,l]) => l)} selected={visualStyle} onSelect={setVisualStyle} />
      <ChoiceGroup label="Language" values={languages.map(([v]) => v)} labels={languages.map(([,l]) => l)} selected={language} onSelect={setLanguage} />
      {fieldError && <p id="setup-error" role="alert" className="text-sm font-medium text-red-600">{fieldError}</p>}
      {error && <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700"><p>{error}</p>{error.includes("failed") && <button type="submit" className="mt-2 font-semibold underline">Retry generation</button>}</div>}
      {generatedDraft && mode === "AI" && <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-4"><p className="text-sm font-semibold text-emerald-800">Story draft generated</p><p className="mt-2 text-sm text-emerald-900">{generatedDraft}</p></div>}
      <button type="submit" disabled={saving} className="w-full rounded-xl bg-slate-950 px-6 py-3.5 font-semibold text-white disabled:cursor-not-allowed disabled:opacity-50">{saving ? (mode === "AI" ? "Generating…" : "Saving…") : mode === "AI" ? "Generate Story" : "Continue"}</button>
    </form>
  </div></main>;
}

function ChoiceGroup({ label, values, labels = values, selected, onSelect, suffix = "" }: { label: string; values: string[]; labels?: string[]; selected: string; onSelect: (value: string) => void; suffix?: string }) { return <fieldset><legend className="text-sm font-semibold">{label}</legend><div className="mt-2 grid grid-cols-3 gap-3">{values.map((v,i) => <button type="button" key={v} aria-pressed={selected === v} onClick={() => onSelect(v)} className={`rounded-xl border px-4 py-3 text-sm font-semibold ${selected === v ? "border-indigo-500 bg-indigo-50 text-indigo-700" : "border-slate-300 bg-white text-slate-700"}`}>{labels[i]}{suffix}</button>)}</div></fieldset>; }
