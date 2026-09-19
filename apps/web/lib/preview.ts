import { interpretScene, type Composition, type RenderVisual } from "@storysprout/editor-model";

export const PREVIEW_WIDTH = 1920 as const;
export const PREVIEW_HEIGHT = 1080 as const;

export type PreviewState =
  | { kind: "loading" }
  | { kind: "ready"; composition: Composition; visuals: ReturnType<typeof interpretScene> }
  | { kind: "empty"; composition: Composition }
  | { kind: "error"; message: string };

export function interpretPreviewComposition(composition: Composition) {
  if (!composition.scenes.length) {
    return { backgroundColor: "#EEF1F5", objects: [] as RenderVisual[] };
  }
  return interpretScene(composition.scenes[0]);
}

export function previewObjectStyle(object: RenderVisual) {
  return {
    left: (object.x / PREVIEW_WIDTH) * 100,
    top: (object.y / PREVIEW_HEIGHT) * 100,
    width: (object.width / PREVIEW_WIDTH) * 100,
    height: (object.height / PREVIEW_HEIGHT) * 100,
  };
}
