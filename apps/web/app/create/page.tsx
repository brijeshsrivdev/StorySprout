"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { createProject } from "../../lib/api";
import { BrandMark } from "../../components/ui";

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
  return <main className="min-h-screen bg-[var(--ss-bg)] text-[var(--ss-text)]"><div className="mx-auto max-w-5xl px-5 py-6 lg:px-8"><div className="flex items-center justify-between"><Link href="/"><BrandMark /></Link><Link href="/" className="rounded-lg px-3 py-2 text-sm font-medium text-slate-500 hover:bg-white hover:text-slate-900 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500">Back to studio</Link></div><div className="mx-auto max-w-3xl py-12 text-center sm:py-16"><p className="text-xs font-semibold uppercase tracking-[.18em] text-indigo-600">New Story</p><h1 className="mt-3 text-4xl font-bold tracking-tight sm:text-5xl">Choose how your story begins.</h1><p className="mx-auto mt-4 max-w-2xl text-[15px] leading-6 text-slate-600">Two deliberate ways into the same creative studio. Choose how much of the first spark you want to shape yourself.</p></div>
    <div className="mx-auto -mt-2 grid max-w-4xl gap-5 md:grid-cols-2">
      {([["AI", "Start with AI", "Bring an idea and get a first story draft to review and shape.", "Quick creative jumpstart"], ["BLANK", "Start Blank", "Begin with a clean story and direct the creative choices yourself.", "Full creative control"]] as const).map(([value, title, description, label]) => <button key={value} type="button" aria-pressed={mode === value} onClick={() => setMode(value)} className={`group min-h-64 rounded-[20px] border p-7 text-left transition duration-150 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 ${mode === value ? "border-indigo-400 bg-indigo-50/70 shadow-md ring-1 ring-indigo-200" : "border-slate-200 bg-white shadow-[var(--ss-shadow-raised)] hover:-translate-y-0.5 hover:border-slate-300 hover:shadow-md"}`}><div className="flex items-start justify-between gap-4"><span className={`grid h-11 w-11 place-items-center rounded-xl text-lg font-bold ${mode === value ? "bg-indigo-600 text-white" : "bg-slate-100 text-slate-700"}`}>{value === "AI" ? "✦" : "＋"}</span>{value === "AI" && <span className="rounded-full bg-violet-50 px-2.5 py-1 text-[11px] font-semibold text-violet-700 ring-1 ring-inset ring-violet-100">✦ AI-assisted</span>}</div><h2 className="mt-8 text-2xl font-bold tracking-tight">{title}</h2><p className="mt-3 text-sm leading-6 text-slate-600">{description}</p><p className="mt-5 text-xs font-semibold uppercase tracking-[.12em] text-slate-400">{label}</p><div className="mt-6 text-sm font-semibold text-indigo-600">{mode === value ? "Selected ✓" : "Choose this path →"}</div></button>)}
    </div>
    {error && <p role="alert" className="mt-6 text-sm font-medium text-red-600">{error}</p>}
    <div className="mx-auto mt-10 max-w-4xl text-center"><p className="mb-3 text-xs text-slate-500">You can review and change your choices in the next step.</p><button type="button" onClick={continueFlow} disabled={!mode || saving} className="min-h-11 rounded-[10px] bg-indigo-600 px-7 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-indigo-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 disabled:cursor-not-allowed disabled:opacity-45">{saving ? "Opening your studio…" : "Continue →"}</button></div>
  </div></main>;
}
