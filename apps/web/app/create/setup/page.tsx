"use client";

import Link from "next/link";
import { FormEvent, Suspense, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { ApiError, createStory } from "../../../lib/api";
import {
  AiBadge,
  Button,
  Field,
  Notice,
  StudioHeader,
  StudioShell,
  Textarea,
} from "../../../components/ui";

const ages = [
  ["3_5", "Ages 3–5"],
  ["6_8", "Ages 6–8"],
  ["9_12", "Ages 9–12"],
] as const;
const durations = [1, 3, 5] as const;
const styles = [
  ["2D", "2D"],
  ["3D", "3D"],
  ["HYBRID", "Hybrid"],
] as const;
const languages = [
  ["ENGLISH", "English"],
  ["HINDI", "Hindi"],
] as const;

export default function StorySetupPage() {
  return (
    <Suspense
      fallback={
        <StudioShell>
          <main className="mx-auto max-w-4xl px-5 py-12 lg:px-8">
            <div className="rounded-[16px] border border-[var(--ss-border)] bg-white p-8 text-sm text-[var(--ss-muted)] shadow-[var(--ss-shadow-raised)]">
              Loading story setup…
            </div>
          </main>
        </StudioShell>
      }
    >
      <StorySetupForm />
    </Suspense>
  );
}

function StorySetupForm() {
  const router = useRouter();
  const params = useSearchParams();
  const projectId = params.get("projectId") ?? "";
  const mode = params.get("mode") === "AI" ? "AI" : "BLANK";

  const [idea, setIdea] = useState("");
  const [targetAge, setTargetAge] = useState("");
  const [durationMinutes, setDurationMinutes] = useState<1 | 3 | 5>(3);
  const [visualStyle, setVisualStyle] = useState("2D");
  const [language, setLanguage] = useState("ENGLISH");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldError, setFieldError] = useState<string | null>(null);
  const [generatedDraft, setGeneratedDraft] = useState<string | null>(null);
  const [reviewContinuationPath, setReviewContinuationPath] = useState<string | null>(null);

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (saving) return;

    setFieldError(null);
    setError(null);

    if (!idea.trim()) {
      setFieldError("Enter a story idea.");
      return;
    }
    if (!targetAge) {
      setFieldError("Select a target age.");
      return;
    }
    if (!projectId) {
      setError("This story project is missing. Return to the dashboard and start again.");
      return;
    }

    setSaving(true);
    try {
      const story = await createStory(projectId, {
        creationMode: mode,
        idea: idea.trim(),
        targetAge: targetAge as "3_5" | "6_8" | "9_12",
        durationMinutes,
        visualStyle: visualStyle as "2D" | "3D" | "HYBRID",
        language: language as "ENGLISH" | "HINDI",
      });

      if (mode === "BLANK") {
        router.replace(story.continuationPath);
        return;
      }
      setGeneratedDraft(story.draftContent);
      setReviewContinuationPath(story.continuationPath);
      setSaving(false);
    } catch (e) {
      const apiError = e instanceof ApiError ? e : null;
      setError(
        apiError?.status === 422
          ? "Story generation failed. Your setup was saved. Try again when you're ready."
          : "We couldn't save your story. Please try again.",
      );
      setSaving(false);
    }
  }

  return (
    <StudioShell>
      <StudioHeader
        backHref="/create"
        backLabel="Start over"
        context={mode === "AI" ? "AI-assisted story" : "Blank story"}
      />

      <main
        aria-labelledby="story-setup-heading"
        className="mx-auto max-w-5xl px-5 py-10 sm:py-14 lg:px-8"
      >
        <div className="mx-auto max-w-3xl">
          <div className="mb-10">
            <div className="flex items-center gap-2">
              <p className="text-xs font-semibold uppercase tracking-[.16em] text-[var(--ss-primary)]">
                Start your story
              </p>
              {mode === "AI" && <AiBadge />}
            </div>
            <h1
              id="story-setup-heading"
              className="mt-3 text-3xl font-bold tracking-tight sm:text-4xl"
            >
              Create your story
            </h1>
            <p className="mt-3 max-w-2xl text-[15px] leading-6 text-[var(--ss-muted)]">
              {mode === "AI"
                ? "Give StorySprout the spark. AI will turn your idea into a first draft for you to review."
                : "Start with the idea in your head, then shape the story in your own direction."}
            </p>
          </div>

          {mode === "AI" && (
            <div className="mb-6 rounded-[16px] border border-violet-100 bg-violet-50/60 px-5 py-4">
              <div className="flex flex-wrap items-center gap-3 text-sm font-semibold text-violet-900">
                <span>My idea</span>
                <span aria-hidden="true" className="text-violet-400">→</span>
                <span>StorySprout AI</span>
                <span aria-hidden="true" className="text-violet-400">→</span>
                <span>My story</span>
              </div>
              <p className="mt-1.5 text-xs leading-5 text-violet-700">
                AI proposes the first draft; your story remains yours to review and shape.
              </p>
            </div>
          )}

          <form onSubmit={submit} noValidate className="space-y-8">
            <section className="rounded-[20px] border border-[var(--ss-border)] bg-white p-6 shadow-[var(--ss-shadow-raised)] sm:p-8">
              <Field
                label="Story idea"
                help="A sentence, a character, a problem, or simply the spark you have in mind."
                error={fieldError ?? undefined}
              >
                <Textarea
                  id="idea"
                  aria-describedby={fieldError ? "story-idea-error" : "story-idea-help"}
                  aria-invalid={fieldError ? "true" : "false"}
                  value={idea}
                  onChange={(event) => {
                    setIdea(event.target.value);
                    if (fieldError) setFieldError(null);
                  }}
                  placeholder="A little rabbit finds a lost star and sets out to return it home…"
                  rows={6}
                  className="min-h-40 resize-y border-slate-200 bg-[var(--ss-surface-elevated)] px-4 py-4 text-base leading-7 shadow-none focus:bg-white"
                />
              </Field>
              <p id="story-idea-help" className="sr-only">
                Enter the idea for your story.
              </p>
              <p id="story-idea-error" className="sr-only">
                {fieldError}
              </p>
            </section>

            <section className="space-y-7">
              <ChoiceGroup
                label="Who is this story for?"
                values={ages.map(([value]) => value)}
                labels={ages.map(([, label]) => label)}
                selected={targetAge}
                onSelect={(value) => {
                  setTargetAge(value);
                  setFieldError(null);
                }}
                columns="sm:grid-cols-3"
              />

              <ChoiceGroup
                label="How long should it be?"
                values={durations.map(String)}
                selected={String(durationMinutes)}
                onSelect={(value) => setDurationMinutes(Number(value) as 1 | 3 | 5)}
                suffix=" min"
                columns="grid-cols-3"
              />

              <ChoiceGroup
                label="What should it look like?"
                values={styles.map(([value]) => value)}
                labels={styles.map(([, label]) => label)}
                selected={visualStyle}
                onSelect={setVisualStyle}
                columns="grid-cols-3"
              />

              <ChoiceGroup
                label="Language"
                values={languages.map(([value]) => value)}
                labels={languages.map(([, label]) => label)}
                selected={language}
                onSelect={setLanguage}
                columns="grid-cols-2"
              />
            </section>

            {saving && (
              <Notice tone={mode === "AI" ? "ai" : "info"}>
                <div className="flex items-center gap-3" aria-live="polite">
                  <span
                    aria-hidden="true"
                    className="h-2 w-2 animate-pulse rounded-full bg-current"
                  />
                  <span>
                    {mode === "AI"
                      ? "StorySprout is turning your idea into a first draft…"
                      : "Saving your story…"}
                  </span>
                </div>
              </Notice>
            )}

            {error && (
              <Notice tone="error">
                <div>
                  <p>{error}</p>
                  {error.includes("failed") && (
                    <button
                      type="submit"
                      disabled={saving}
                      className="mt-2 rounded-md font-semibold underline underline-offset-2 focus:outline-none focus-visible:ring-2 focus-visible:ring-red-500"
                    >
                      Retry generation
                    </button>
                  )}
                </div>
              </Notice>
            )}

            {generatedDraft && mode === "AI" && (
              <section aria-labelledby="story-draft-heading" className="rounded-[20px] border border-violet-200 bg-white p-6 shadow-[var(--ss-shadow-raised)] sm:p-8">
                <div className="flex flex-wrap items-center gap-2">
                  <AiBadge />
                  <p className="text-xs font-semibold uppercase tracking-[.14em] text-[var(--ss-ai)]">AI proposal</p>
                </div>
                <h2 id="story-draft-heading" className="mt-3 text-2xl font-bold tracking-tight">Your Story Draft</h2>
                <p className="mt-2 text-sm leading-6 text-[var(--ss-muted)]">StorySprout proposed this first draft from your idea and setup. Review it before moving into the Story Outline.</p>
                <div className="mt-6 whitespace-pre-wrap rounded-[14px] border border-violet-100 bg-violet-50/40 p-5 text-sm leading-7 text-slate-800" aria-live="polite">{generatedDraft}</div>
                <div className="mt-6 flex flex-col gap-3 border-t border-[var(--ss-border)] pt-5 sm:flex-row sm:items-center sm:justify-between">
                  <p className="text-xs leading-5 text-[var(--ss-muted)]">The AI draft is now your starting point. You can shape the story further in the next step.</p>
                  <Button type="button" onClick={() => reviewContinuationPath && router.replace(reviewContinuationPath)} disabled={!reviewContinuationPath}>Continue to Story Outline →</Button>
                </div>
              </section>
            )}

            <div className="flex flex-col items-stretch gap-3 border-t border-[var(--ss-border)] pt-7 sm:flex-row sm:items-center sm:justify-between">
              <p className="text-xs leading-5 text-[var(--ss-muted)]">
                {mode === "AI"
                  ? "Your idea and setup stay preserved if generation needs another try."
                  : "You can shape the story further in the next step."}
              </p>
              <Button
                type="submit"
                disabled={saving}
                className="w-full sm:w-auto sm:min-w-40"
              >
                {saving
                  ? mode === "AI"
                    ? "Generating…"
                    : "Saving…"
                  : mode === "AI"
                    ? "Generate Story"
                    : "Continue"}
              </Button>
            </div>
          </form>
        </div>
      </main>
    </StudioShell>
  );
}

function ChoiceGroup({
  label,
  values,
  labels = values,
  selected,
  onSelect,
  suffix = "",
  columns = "grid-cols-3",
}: {
  label: string;
  values: string[];
  labels?: string[];
  selected: string;
  onSelect: (value: string) => void;
  suffix?: string;
  columns?: string;
}) {
  return (
    <fieldset>
      <legend className="text-base font-semibold tracking-tight">{label}</legend>
      <div className={`mt-3 grid gap-3 ${columns}`}>
        {values.map((value, index) => {
          const isSelected = selected === value;
          return (
            <button
              type="button"
              key={value}
              aria-pressed={isSelected}
              onClick={() => onSelect(value)}
              className={`min-h-12 rounded-[10px] border px-4 py-3 text-left text-sm font-semibold transition duration-150 focus:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ss-primary)] focus-visible:ring-offset-2 ${
                isSelected
                  ? "border-[var(--ss-primary)] bg-indigo-50 text-indigo-800 shadow-sm ring-1 ring-indigo-200"
                  : "border-[var(--ss-border-strong)] bg-white text-[var(--ss-text)] hover:border-slate-400 hover:bg-[var(--ss-surface-elevated)]"
              }`}
            >
              <span className="flex items-center justify-between gap-3">
                <span>{labels[index]}{suffix}</span>
                {isSelected && (
                  <span aria-hidden="true" className="text-[var(--ss-primary)]">✓</span>
                )}
              </span>
            </button>
          );
        })}
      </div>
    </fieldset>
  );
}
