export type SceneObjectType = "CHARACTER" | "PROP";
export type CompositionObject = { id:string; objectType:SceneObjectType; assetId:string; x:number; y:number; scale:number; rotation:number; visible:boolean };
export type CompositionScene = { id:string; name:string; durationMs:number; background?:{assetId:string}; objects:CompositionObject[]; timeline:unknown[] };
export type Composition = { schemaVersion:"1.1"; projectId:string; width:1920; height:1080; fps:30; durationMs:number; scenes:CompositionScene[] };
export type EditorContext = {
  storyId:string; storyTitle:string; outlineSceneId:string; sceneOrder:number; sceneTitle:string; sceneSummary:string; plannedDurationSeconds:number;
  sceneSetup:{ backgroundPresetKey:string|null; characters:{id:string;name:string;category:string;orderIndex:number}[]; props:{id:string;propPresetKey:string;orderIndex:number}[]; dialogue:unknown[]; actions:unknown[] };
  characters:{id:string;projectId:string;name:string;roleDescription:string;category:string;visualDescription:string;personality:string|null}[];
  backgroundOptions:{key:string;name:string;category:string}[];
  propOptions:{key:string;name:string;category:string}[];
  composition:{id:string;projectId:string;outlineSceneId:string;compositionJson:Composition;version:number;createdAt:string;updatedAt:string};
  initializationStatus:"INITIALIZED"|"EXISTING";
};
export const getEditorContext=(storyId:string,sceneId:string)=>request<EditorContext>(`/stories/${storyId}/outline-scenes/${sceneId}/editor`);
export const saveComposition=(storyId:string,sceneId:string,version:number,composition:Composition)=>request<EditorContext["composition"]>(`/stories/${storyId}/outline-scenes/${sceneId}/composition`,{method:"PUT",body:JSON.stringify({version,composition})});
function request<T>(path:string,init?:RequestInit):Promise<T>{return fetch(`/api/v1${path}`,{...init,headers:{"Content-Type":"application/json",...(init?.headers??{})}}).then(async r=>{const b=await r.json().catch(()=>({}));if(!r.ok){const e=b.error;throw new EditorApiError(r.status,e?.code??"REQUEST_FAILED",e?.message??"Request failed");}return b.data as T;});}
export class EditorApiError extends Error { constructor(public status:number,public code:string,message:string){super(message);} }
