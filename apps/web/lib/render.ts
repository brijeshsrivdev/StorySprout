export type RenderJobStatus = "REQUESTED" | "QUEUED" | "RENDERING" | "COMPLETED" | "FAILED";

export type RenderJob = {
  id: string;
  storyId: string;
  outlineSceneId: string;
  compositionId: string;
  compositionVersion: number;
  status: RenderJobStatus;
  progress: number;
  attempt: number;
  failureCode: string | null;
  failureMessage: string | null;
  artifactUrl: string | null;
  artifactSize: number | null;
  createdAt: string;
  updatedAt: string;
};

class RenderApiError extends Error {
  constructor(public status: number, message: string) { super(message); }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch("/api/v1" + path, {
    ...init,
    headers: { "Content-Type": "application/json", ...(init?.headers ?? {}) }
  });
  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new RenderApiError(response.status, body?.error?.message ?? "Render request failed");
  }
  return body.data as T;
}

export const requestRender = (storyId: string, sceneId: string) =>
  request<RenderJob>(`/stories/${storyId}/outline-scenes/${sceneId}/render-jobs`, { method: "POST" });

export const getRenderJob = (storyId: string, sceneId: string, jobId: string) =>
  request<RenderJob>(`/stories/${storyId}/outline-scenes/${sceneId}/render-jobs/${jobId}`);

export const retryRender = (storyId: string, sceneId: string, jobId: string) =>
  request<RenderJob>(`/stories/${storyId}/outline-scenes/${sceneId}/render-jobs/${jobId}/retry`, { method: "POST" });

export { RenderApiError };
