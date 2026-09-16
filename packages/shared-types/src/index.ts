export type ProjectStatus = "DRAFT" | "READY";
export type AssetType = "CHARACTER" | "BACKGROUND" | "PROP" | "AUDIO" | "MUSIC" | "SFX";
export type RenderStatus = "QUEUED" | "RENDERING" | "COMPLETED" | "FAILED";
export type StoryTargetAge = "3_5" | "6_8" | "9_12";
export type StoryCreationMode = "AI" | "BLANK";
export type StoryGenerationStatus = "NOT_REQUESTED" | "GENERATING" | "COMPLETED" | "FAILED";
export type StoryDurationMinutes = 1 | 3 | 5;
export type StoryVisualStyle = "2D" | "3D" | "HYBRID";
export type StoryLanguage = "ENGLISH" | "HINDI";

export interface Project { id: string; name: string; status: ProjectStatus; createdAt?: string; updatedAt?: string; }
export interface Story { id: string; projectId: string; title: string; idea: string; targetAge: StoryTargetAge; durationMinutes: StoryDurationMinutes; visualStyle: StoryVisualStyle; language: StoryLanguage; creationMode: StoryCreationMode; generationStatus: StoryGenerationStatus; draftContent?: string | null; createdAt?: string; updatedAt?: string; }
export interface Asset { id: string; type: AssetType; name: string; storageUrl?: string; thumbnailUrl?: string; status?: string; }
export interface RenderJob { id: string; projectId: string; status: RenderStatus; outputUrl?: string; error?: string; }
export interface ApiResponse<T> { data: T; }
