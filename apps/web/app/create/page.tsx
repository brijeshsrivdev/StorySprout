"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { createProject } from "../../lib/api";

export default function CreateStoryPage() {
  const router = useRouter();
  const [mode, setMode] = useState<"AI" | "BLANK" | null>(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  async function continueFlow() {
    if (!mode || saving) return;
    setSaving(true); setError(null);
    try { const project = await createProject(); router.replace(`/create/setup?projectId=${project.id}&mode=${mode}`); }
    catch { setError("We couldn't start your story. Please try again."); setSaving(false); }
  }
  return <main className="min-h-screen bg-slate-50"><div className="mx-auto max-w-4xl px-8 py-12"><Link href="/" className="text-sm font-medium text-slate-500 hover:text-slate-900">← Dashboard</Link><div className="mt-10"><p className="text-sm font-semibold text-indigo-600">NEW STORY</p><h1 className="mt-2 text-4xl font-bold">How would you like to start?</h1><p className="mt-3 text-slate-600">Choose a starting point. You can shape the story as you go.</p></div>
    <div className="mt-10 grid gap-5 md:grid-cols-2">
      {([["AI", "Start with AI", "Turn your idea into a first story draft."], ["BLANK", "Blank Story", "Start with a clean story setup."]] as const).map(([value, title, description]) => <button key={value} type="button" aria-pressed={mode === value} onClick={() => setMode(value)} className={`rounded-3xl border p-7 text-left transition ${mode === value ? "border-indigo-500 bg-indigo-50 ring-2 ring-indigo-100" : "border-slate-200 bg-white hover:border-slate-300"}`}><span className="text-3xl">{value === "AI" ? "✨" : "📖"}</span><h2 className="mt-5 text-xl font-bold">{title}</h2><p className="mt-2 text-slate-600">{description}</p><span className="mt-6 block text-sm font-semibold text-indigo-600">{mode === value ? "Selected" : "Select"}</span></button>)}
    </div>
    {error && <p role="alert" className="mt-6 text-sm font-medium text-red-600">{error}</p>}
    <div className="mt-10 flex justify-end"><button type="button" onClick={continueFlow} disabled={!mode || saving} className="rounded-xl bg-slate-950 px-6 py-3 font-semibold text-white disabled:cursor-not-allowed disabled:opacity-40">{saving ? "Creating…" : "Continue"}</button></div>
  </div></main>;
}
