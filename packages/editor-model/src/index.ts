export const COMPOSITION_SCHEMA_VERSION = "1.0" as const;

export interface Composition {
  schemaVersion: typeof COMPOSITION_SCHEMA_VERSION;
  projectId: string;
  width: number;
  height: number;
  fps: number;
  durationMs: number;
  scenes: Scene[];
}

export interface Scene {
  id: string;
  name: string;
  durationMs: number;
  background?: SceneBackground;
  objects: SceneObject[];
  timeline: TimelineClip[];
}

export interface SceneBackground { assetId: string; }

export interface SceneObject {
  id: string;
  assetId: string;
  x: number;
  y: number;
  scale: number;
  rotation: number;
  visible: boolean;
}

export interface TimelineClip {
  id: string;
  kind: "OBJECT" | "DIALOGUE" | "AUDIO" | "MUSIC" | "SFX";
  startMs: number;
  durationMs: number;
  sourceId?: string;
}

export function createEmptyComposition(projectId: string): Composition {
  return {
    schemaVersion: COMPOSITION_SCHEMA_VERSION,
    projectId,
    width: 1920,
    height: 1080,
    fps: 30,
    durationMs: 0,
    scenes: []
  };
}
