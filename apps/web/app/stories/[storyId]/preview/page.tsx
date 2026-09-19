/* eslint-disable react-hooks/set-state-in-effect */
"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { getEditorContext, type Composition } from "../../../../lib/editor";
import { Button, PageIntro, Panel } from "../../../../components/ui";
import { interpretPreviewComposition, previewObjectStyle } from "../../../../lib/preview";

export default function PreviewPage({ params, searchParams }: {
  params: Promise<{ storyId: string }>;
  searchParams: Promise<{ scene?: string }>;
}) {
  const [storyId, setStoryId] = useState("");
  const [sceneId, setSceneId] = useState("");
  const [storyTitle, setStoryTitle] = useState("");
  const [sceneTitle, setSceneTitle] = useState("");
  const [composition, setComposition] = useState<Composition | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([params, searchParams]).then(async ([p, q]) => {
      setStoryId(p.storyId);
      setSceneId(q.scene ?? "");
      try {
        if (!q.scene) throw new Error("Scene is required");
        const context = await getEditorContext(p.storyId, q.scene);
        setStoryTitle(context.storyTitle);
        setSceneTitle(context.sceneTitle);
        setComposition(context.composition.compositionJson);
      } catch (e) {
        setError(e instanceof Error ? e.message : "Unable to load Preview");
      }
    });
  }, [params, searchParams]);

  const scene = composition?.scenes?.[0];
  let plan: ReturnType<typeof interpretPreviewComposition> | null = null;
  let interpretationError = "";
  if (composition) {
    try {
      plan = interpretPreviewComposition(composition);
    } catch (e) {
      interpretationError = e instanceof Error ? e.message : "Composition cannot be previewed";
    }
  }
  const displayError = error || interpretationError;

  return (
    <main className="min-h-screen bg-[var(--ss-bg)] text-[var(--ss-text)]">
      <div className="mx-auto max-w-6xl px-5 py-8 lg:px-8">
        <div className="flex items-center justify-between gap-4">
          <Link href={`/stories/${storyId}/editor?scene=${sceneId}`} className="text-sm font-semibold text-indigo-700 hover:text-indigo-900">
            ← Back to Editor
          </Link>
          {!composition && !displayError && <span className="text-xs font-semibold text-[var(--ss-muted)]">Loading preview…</span>}
        </div>

        <PageIntro
          eyebrow="Preview"
          title={storyTitle ? `${storyTitle} · ${sceneTitle}` : "Composition Preview"}
          description="This is a preview of your current saved Composition. Preview is static in V1: no timing or animation is introduced."
        />

        {displayError ? (
          <Panel className="mt-8 border-red-200 bg-red-50 p-6">
            <div role="alert">
              <h2 className="font-bold text-red-900">Preview unavailable</h2>
              <p className="mt-2 text-sm text-red-800">{displayError}</p>
              <Link href={`/stories/${storyId}/editor?scene=${sceneId}`} className="mt-4 inline-block">
                <Button variant="secondary">Return to Editor</Button>
              </Link>
            </div>
          </Panel>
        ) : !plan || !scene ? (
          <Panel className="mt-8 p-8">
            <p className="text-sm text-[var(--ss-muted)]">Loading your saved Composition…</p>
          </Panel>
        ) : (
          <Panel className="mt-8 p-4 sm:p-6">
            <div className="mb-4 flex flex-wrap items-center justify-between gap-2 text-xs text-[var(--ss-muted)]">
              <span>Saved Composition · {composition?.schemaVersion}</span>
              <span>1920 × 1080 · {composition?.fps} fps · static preview</span>
            </div>
            <div
              className="relative mx-auto aspect-video w-full max-w-5xl overflow-hidden rounded-[18px] border border-[var(--ss-border)]"
              style={{ background: plan.backgroundColor }}
              aria-label="Story Composition preview"
            >
              {plan.objects.map(object => (
                <div
                  key={object.id}
                  className="absolute flex -translate-x-1/2 -translate-y-1/2 items-center justify-center rounded-2xl border-2 px-3 text-center text-xs font-bold shadow-sm"
                  style={{
                    left: `${previewObjectStyle(object).left}%`,
                    top: `${previewObjectStyle(object).top}%`,
                    width: `${previewObjectStyle(object).width}%`,
                    height: `${previewObjectStyle(object).height}%`,
                    background: object.fill,
                    borderColor: object.border
                  }}
                  aria-label={`${object.objectType} ${object.label}`}
                >
                  {object.label}
                </div>
              ))}
              {!plan.objects.length && (
                <div className="absolute inset-0 grid place-items-center text-center">
                  <div>
                    <p className="text-xs font-bold uppercase tracking-[.14em] text-slate-500">Empty composition</p>
                    <p className="mt-2 text-sm text-slate-600">Add a character or prop in the Editor.</p>
                  </div>
                </div>
              )}
            </div>
          </Panel>
        )}
      </div>
    </main>
  );
}
