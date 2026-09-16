"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { listProjects, listStories, Project, Story } from "../lib/api";

export default function Dashboard() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [stories, setStories] = useState<Record<string, Story[]>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listProjects()
      .then(async nextProjects => {
        setProjects(nextProjects);
        const entries = await Promise.all(nextProjects.map(async project => [project.id, await listStories(project.id)] as const));
        setStories(Object.fromEntries(entries));
      })
      .catch(() => setError("We couldn't load your stories. Please try again."))
      .finally(() => setLoading(false));
  }, []);

  return <main className="min-h-screen bg-slate-50 text-slate-950">
    <div className="mx-auto max-w-6xl px-8 py-10">
      <header className="mb-10 flex items-center justify-between">
        <div><p className="text-sm font-semibold tracking-wide text-indigo-600">STORYSPROUT</p><h1 className="mt-2 text-4xl font-bold tracking-tight">Your stories</h1><p className="mt-2 text-slate-600">Create playful stories for little audiences.</p></div>
        <Link href="/create" className="rounded-xl bg-slate-950 px-5 py-3 text-sm font-semibold text-white shadow-sm hover:bg-slate-800">Create Story</Link>
      </header>
      {loading && <div className="rounded-2xl border border-slate-200 bg-white p-8 text-slate-600">Loading stories…</div>}
      {error && !loading && <div role="alert" className="rounded-2xl border border-red-200 bg-red-50 p-6 text-red-700">{error}</div>}
      {!loading && !error && projects.length === 0 && <section className="rounded-3xl border border-dashed border-slate-300 bg-white px-8 py-20 text-center"><div className="mx-auto max-w-md"><div className="text-5xl">🌱</div><h2 className="mt-5 text-2xl font-bold">No stories yet</h2><p className="mt-2 text-slate-600">Start with an idea and grow it into a story.</p><Link href="/create" className="mt-7 inline-flex rounded-xl bg-indigo-600 px-5 py-3 font-semibold text-white hover:bg-indigo-700">Create Story</Link></div></section>}
      {!loading && !error && projects.length > 0 && <section className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">{projects.map(project => <article key={project.id} className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"><div className="flex items-start justify-between gap-4"><h2 className="font-semibold">{project.name}</h2><span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-600">{project.status}</span></div><div className="mt-5 space-y-2">{(stories[project.id] ?? []).map(story => <Link key={story.id} href={story.continuationPath} className="block rounded-xl border border-slate-200 p-3 hover:border-indigo-300 hover:bg-indigo-50"><p className="font-semibold">{story.title}</p><p className="mt-1 text-xs text-slate-500">{story.durationMinutes} min · {story.visualStyle} · {story.targetAge.replace("_", "–")}</p></Link>)}{(stories[project.id] ?? []).length === 0 && <p className="text-sm text-slate-500">No stories in this project yet.</p>}</div><p className="mt-5 text-xs text-slate-500">Updated {new Date(project.updatedAt).toLocaleString()}</p></article>)}</section>}
    </div>
  </main>;
}
