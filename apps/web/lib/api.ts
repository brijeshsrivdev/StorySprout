export type Project = { id: string; name: string; status: "DRAFT" | "READY"; createdAt: string; updatedAt: string };
export type Story = { id: string; projectId: string; title: string; idea: string; targetAge: "3_5" | "6_8" | "9_12"; durationMinutes: 1 | 3 | 5; visualStyle: "2D" | "3D" | "HYBRID"; language: "ENGLISH" | "HINDI"; creationMode: "AI" | "BLANK"; generationStatus: "NOT_REQUESTED" | "GENERATING" | "COMPLETED" | "FAILED"; draftContent: string | null; createdAt: string; updatedAt: string; continuationPath: string };
export type OutlineScene = { id: string; storyId: string; orderIndex: number; title: string; summary: string; durationSeconds: number; createdAt: string; updatedAt: string };
export type StoryOutline = { story: Story; scenes: OutlineScene[]; targetDurationSeconds: number; plannedDurationSeconds: number; varianceSeconds: number };
export type CharacterCategory = "CHILD" | "ADULT" | "ANIMAL" | "FANTASY" | "OBJECT" | "OTHER";
export type Character = { id: string; projectId: string; name: string; roleDescription: string; category: CharacterCategory; visualDescription: string; personality: string | null; createdAt: string; updatedAt: string };
export type CharacterSuggestion = Omit<Character, "id" | "projectId" | "createdAt" | "updatedAt">;

type ApiEnvelope<T> = { data: T };
export class ApiError extends Error { constructor(public status: number, public code: string, message: string, public fields: { field: string; message: string }[] = []) { super(message); } }
async function request<T>(path: string, init?: RequestInit): Promise<T> { const response = await fetch(`/api/v1${path}`, { ...init, headers: { "Content-Type": "application/json", ...(init?.headers ?? {}) } }); const body = await response.json().catch(() => ({})); if (!response.ok) { const error = body.error; throw new ApiError(response.status,error?.code ?? "REQUEST_FAILED",error?.message ?? "Request failed",error?.fields ?? []); } return (body as ApiEnvelope<T>).data; }
export const listProjects=()=>request<Project[]>("/projects");
export const listStories=(projectId:string)=>request<Story[]>(`/projects/${projectId}/stories`);
export const getStory=(projectId:string,storyId:string)=>request<Story>(`/projects/${projectId}/stories/${storyId}`);
export const createProject=(name="Untitled Story")=>request<Project>("/projects",{method:"POST",body:JSON.stringify({name})});
export const createStory=(projectId:string,input:Omit<Story,"id"|"projectId"|"title"|"generationStatus"|"draftContent"|"createdAt"|"updatedAt"|"continuationPath">)=>request<Story>(`/projects/${projectId}/stories`,{method:"POST",body:JSON.stringify(input)});
export const getOutline=(storyId:string)=>request<StoryOutline>(`/stories/${storyId}/outline-scenes`);
export const generateOutline=(storyId:string)=>request<StoryOutline>(`/stories/${storyId}/outline/generate`,{method:"POST"});
export const createOutlineScene=(storyId:string,input:{title:string;summary:string;durationSeconds:number})=>request<OutlineScene>(`/stories/${storyId}/outline-scenes`,{method:"POST",body:JSON.stringify(input)});
export const updateOutlineScene=(storyId:string,sceneId:string,input:{title:string;summary:string;durationSeconds:number})=>request<OutlineScene>(`/stories/${storyId}/outline-scenes/${sceneId}`,{method:"PATCH",body:JSON.stringify(input)});
export const deleteOutlineScene=(storyId:string,sceneId:string)=>request<void>(`/stories/${storyId}/outline-scenes/${sceneId}`,{method:"DELETE"});
export const reorderOutlineScenes=(storyId:string,sceneIds:string[])=>request<OutlineScene[]>(`/stories/${storyId}/outline-scenes/reorder`,{method:"PATCH",body:JSON.stringify({sceneIds})});
export const listProjectCharacters=(projectId:string)=>request<Character[]>(`/projects/${projectId}/characters`);
export const listStoryCharacters=(storyId:string)=>request<Character[]>(`/stories/${storyId}/characters`);
export const createCharacter=(projectId:string,input:Omit<CharacterSuggestion,"id">)=>request<Character>(`/projects/${projectId}/characters`,{method:"POST",body:JSON.stringify(input)});
export const updateCharacter=(projectId:string,characterId:string,input:Omit<CharacterSuggestion,"id">)=>request<Character>(`/projects/${projectId}/characters/${characterId}`,{method:"PATCH",body:JSON.stringify(input)});
export const deleteCharacter=(projectId:string,characterId:string)=>request<void>(`/projects/${projectId}/characters/${characterId}`,{method:"DELETE"});
export const addCharacterToStory=(storyId:string,characterId:string)=>request<void>(`/stories/${storyId}/characters/${characterId}`,{method:"POST"});
export const removeCharacterFromStory=(storyId:string,characterId:string)=>request<void>(`/stories/${storyId}/characters/${characterId}`,{method:"DELETE"});
export const generateCharacter=(projectId:string,input:{idea:string;storyId?:string})=>request<CharacterSuggestion>(`/projects/${projectId}/characters/generate`,{method:"POST",body:JSON.stringify(input)});
