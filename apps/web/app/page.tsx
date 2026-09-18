"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { listProjects, listStories, Project, Story } from "../lib/api";
import { BrandMark, Button, EmptyState, StudioShell } from "../components/ui";

export default function Dashboard() {
  const [projects,setProjects]=useState<Project[]>([]);
  const [stories,setStories]=useState<Record<string,Story[]>>({});
  const [loading,setLoading]=useState(true);
  const [error,setError]=useState<string|null>(null);

  useEffect(()=>{ listProjects().then(async ps=>{
    setProjects(ps);
    const entries=await Promise.all(ps.map(async p=>[p.id,await listStories(p.id).catch(()=>[])] as const));
    setStories(Object.fromEntries(entries));
  }).catch(()=>setError("We couldn't load your studio. Please try again.")).finally(()=>setLoading(false)); },[]);

  return <StudioShell>
    <div className="mx-auto max-w-7xl px-5 py-6 lg:px-8 lg:py-8">
      <header className="mb-10 flex items-center justify-between gap-4">
        <BrandMark/>
        <Link href="/create"><Button as any> </Button></Link>
      </header>
      <section className="mb-10 flex flex-col gap-5 rounded-[22px] border border-slate-200 bg-white px-7 py-8 shadow-[var(--ss-shadow-raised)] sm:px-10 sm:py-10 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[.16em] text-indigo-600">Your Studio</p>
          <h1 className="mt-2 text-4xl font-bold tracking-tight">What are you making next?</h1>
          <p className="mt-3 max-w-2xl text-[15px] leading-6 text-slate-600">Build a story, grow your cast, stage the scene, and direct it your way.</p>
        </div>
        <Link href="/create"><Button>Create Story</Button></Link>
      </section>

      {loading && <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">{[1,2,3].map(i=><div key={i} className="h-56 animate-pulse rounded-[16px] bg-slate-200/70"/>)}</div>}
      {error && !loading && <div role="alert" className="rounded-[14px] border border-red-200 bg-red-50 p-5 text-sm text-red-800">{error}</div>}
      {!loading && !error && projects.length===0 && <EmptyState title="Your first story starts here" description="Begin with an idea and grow it into something your audience can return to." action={<Link href="/create"><Button>Create your first story</Button></Link>}/>}
      {!loading && !error && projects.length>0 && <section className="space-y-8">
        {projects.map(project=><div key={project.id}>
          <div className="mb-4 flex items-center justify-between"><div><p className="text-xs font-semibold uppercase tracking-[.14em] text-slate-500">Project</p><h2 className="mt-1 text-xl font-bold">{project.name}</h2></div><span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">{project.status}</span></div>
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {(stories[project.id]??[]).map((story,idx)=><Link key={story.id} href={story.continuationPath} className="group rounded-[16px] border border-slate-200 bg-white p-5 shadow-[var(--ss-shadow-raised)] transition duration-150 hover:-translate-y-0.5 hover:border-indigo-200 hover:shadow-md focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500">
              <div className="flex aspect-[16/9] items-end rounded-[12px] border border-slate-200 bg-gradient-to-br from-slate-100 via-white to-indigo-50 p-4">
                <div><p className="text-xs font-semibold uppercase tracking-[.12em] text-indigo-600">Story {idx+1}</p><h3 className="mt-1 text-lg font-bold">{story.title}</h3></div>
              </div>
              <div className="mt-4 flex items-center justify-between gap-3"><p className="text-sm text-slate-600">{story.durationMinutes} min · {story.visualStyle} · {story.targetAge.replace("_","–")}</p><span className="text-sm font-semibold text-indigo-600 transition group-hover:translate-x-0.5">Continue →</span></div>
            </Link>)}
            {(stories[project.id]??[]).length===0 && <EmptyState title="No stories yet" description="Create the first story in this project." action={<Link href="/create"><Button>Create Story</Button></Link>}/>}
          </div>
        </div>)}
      </section>}
    </div>
  </StudioShell>;
}
