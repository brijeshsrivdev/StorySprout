export const COMPOSITION_SCHEMA_VERSION = "1.1" as const;
export const LEGACY_COMPOSITION_SCHEMA_VERSION = "1.0" as const;
export const CANONICAL_STAGE_WIDTH = 1920 as const;
export const CANONICAL_STAGE_HEIGHT = 1080 as const;
export const CANONICAL_FPS = 30 as const;

export type SceneObjectType = "CHARACTER" | "PROP";

export interface Composition {
  schemaVersion: typeof COMPOSITION_SCHEMA_VERSION;
  projectId: string;
  width: typeof CANONICAL_STAGE_WIDTH;
  height: typeof CANONICAL_STAGE_HEIGHT;
  fps: typeof CANONICAL_FPS;
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
  objectType: SceneObjectType;
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
export interface LegacySceneObject {
  id: string;
  assetId: string;
  x: number;
  y: number;
  scale: number;
  rotation: number;
  visible: boolean;
}
export interface LegacyComposition {
  schemaVersion: typeof LEGACY_COMPOSITION_SCHEMA_VERSION;
  projectId: string;
  width: number;
  height: number;
  fps: number;
  durationMs: number;
  scenes: Array<Omit<Scene, "objects"> & { objects: LegacySceneObject[] }>;
}

export function createEmptyComposition(projectId: string): Composition {
  return { schemaVersion: COMPOSITION_SCHEMA_VERSION, projectId, width: 1920, height: 1080, fps: 30, durationMs: 0, scenes: [] };
}

/**
 * Explicit legacy migration. Schema 1.0 has no semantic object type, so callers
 * must provide a type for every object. No asset/id/name inference is permitted.
 */
export function migrateCompositionV1ToV11(
  legacy: LegacyComposition,
  objectTypesById: Readonly<Record<string, SceneObjectType>>
): Composition {
  if (legacy.schemaVersion !== LEGACY_COMPOSITION_SCHEMA_VERSION) throw new Error("Unsupported legacy Composition schema");
  if (legacy.width !== 1920 || legacy.height !== 1080 || legacy.fps !== 30) throw new Error("Legacy Composition uses unsupported stage settings");
  return {
    schemaVersion: COMPOSITION_SCHEMA_VERSION,
    projectId: legacy.projectId,
    width: 1920,
    height: 1080,
    fps: 30,
    durationMs: legacy.durationMs,
    scenes: legacy.scenes.map(scene => ({
      ...scene,
      objects: scene.objects.map(object => {
        const objectType = objectTypesById[object.id];
        if (!objectType) throw new Error(`Missing explicit object type for legacy object ${object.id}`);
        return { ...object, objectType };
      })
    }))
  };
}
