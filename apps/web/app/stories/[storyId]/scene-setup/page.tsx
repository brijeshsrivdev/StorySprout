import Link from "next/link";

export default async function SceneSetupPlaceholder({ params }: { params: Promise<{ storyId: string }> }) {
  const { storyId } = await params;
  return <main className="min-h-screen bg-slate-50 p-10 text-slate-950"><div className="mx-auto max-w-2xl rounded-3xl border border-slate-200 bg-white p-10 text-center shadow-sm"><p className="text-sm font-semibold tracking-wide text-indigo-600">NEXT STEP</p><h1 className="mt-3 text-3xl font-bold">Scene Setup</h1><p className="mt-3 text-slate-600">Scene Setup is the next product stage. Story Outline is complete for this flow.</p><Link href={`/stories/${storyId}/outline`} className="mt-7 inline-flex rounded-xl bg-slate-950 px-5 py-3 text-sm font-semibold text-white">Back to Story Outline</Link></div></main>;
}
