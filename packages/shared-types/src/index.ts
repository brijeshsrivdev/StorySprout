export type ProjectStatus = "DRAFT" | "READY";
export type AssetType = "CHARACTER" | "BACKGROUND" | "PROP" | "AUDIO" | "MUSIC" | "SFX";
export type RenderStatus = "QUEUED" | "RENDERING" | "COMPLETED" | "FAILED";

export interface Project { id: string; name: string; status: ProjectStatus; }
export interface Asset { id: string; type: AssetType; name: string; storageUrl?: string; thumbnailUrl?: string; status?: string; }
export interface RenderJob { id: string; projectId: string; status: RenderStatus; outputUrl?: string; error?: string; }
export interface ApiResponse<T> { data: T; }
