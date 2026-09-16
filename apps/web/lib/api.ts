export type Project = { id: string; name: string; status: "DRAFT" | "READY"; createdAt: string; updatedAt: string };
export type Story = { id: string; projectId: string; title: string; idea: string; targetAge: "3_5" | "6_8" | "9_12"; durationMinutes: 1 | 3 | 5; visualStyle: "2D" | "3D" | "HYBRID"; language: "ENGLISH" | "HINDI"; creationMode: "AI" | "BLANK"; generationStatus: "NOT_REQUESTED" | "GENERATING" | "COMPLETED" | "FAILED"; draftContent: string | null; createdAt: string; updatedAt: string; continuationPath: string };

type ApiEnvelope<T> = { data: T };
export class ApiError extends Error { constructor(public status: number, public code: string, message: string, public fields: { field: string; message: string }[] = []) { super(message); } }

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api/v1${path}`, { ...init, headers: { "Content-Type": "application/json", ...(init?.headers ?? {}) } });
  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    const error = body.error;
    throw new ApiError(response.status, error?.code ?? "REQUEST_FAILED", error?.message ?? "Request failed", error?.fields ?? []);
  }
  return (body as ApiEnvelope<T>).data;
}

export const listProjects = () => request<Project[]>("/projects");
export const createProject = (name = "Untitled Story") => request<Project>("/projects", { method: "POST", body: JSON.stringify({ name }) });
export const createStory = (projectId: string, input: Omit<Story, "id" | "projectId" | "title" | "generationStatus" | "draftContent" | "createdAt" | "updatedAt" | "continuationPath">) => request<Story>(`/projects/${projectId}/stories`, { method: "POST", body: JSON.stringify(input) });
