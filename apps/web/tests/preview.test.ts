import { describe, expect, it } from "vitest";
import {
  interpretPreviewComposition,
  previewObjectStyle,
} from "../lib/preview";

const base = {
  schemaVersion: "1.1" as const,
  projectId: "project-1",
  width: 1920 as const,
  height: 1080 as const,
  fps: 30 as const,
  durationMs: 1000,
};

function composition(scene: Record<string, unknown>) {
  return { ...base, scenes: [scene] } as never;
}

describe("Preview model", () => {
  it("uses the canonical Composition interpreter and preserves background", () => {
    const result = interpretPreviewComposition(composition({
      id: "scene-1",
      name: "Garden",
      durationMs: 1000,
      background: { assetId: "background-preset:garden_day" },
      objects: [],
      timeline: [],
    }));
    expect(result.backgroundColor).toBe("#E6F4D7");
  });

  it("preserves object ordering while omitting hidden objects", () => {
    const result = interpretPreviewComposition(composition({
      id: "scene-1",
      name: "Garden",
      durationMs: 1000,
      objects: [
        { id: "first", objectType: "PROP", assetId: "prop-preset:ball", x: 100, y: 200, scale: 1, rotation: 0, visible: true },
        { id: "hidden", objectType: "CHARACTER", assetId: "character-2", x: 300, y: 400, scale: 1, rotation: 0, visible: false },
        { id: "last", objectType: "CHARACTER", assetId: "character-1", x: 960, y: 540, scale: 2, rotation: 0, visible: true },
      ],
      timeline: [],
    }));
    expect(result.objects.map(object => object.id)).toEqual(["first", "last"]);
    expect(result.objects[1].width).toBe(360);
    expect(result.objects[1].height).toBe(440);
  });

  it("maps canonical position and scale into responsive percentages", () => {
    const style = previewObjectStyle({
      id: "character-1",
      objectType: "CHARACTER",
      assetId: "character-1",
      x: 480,
      y: 540,
      width: 180,
      height: 220,
      fill: "#E0E7FF",
      border: "#4F46E5",
      label: "character",
    });
    expect(style.left).toBe(25);
    expect(style.top).toBe(50);
    expect(style.width).toBe(9.375);
    expect(style.height).toBeCloseTo(20.37037, 5);
  });

  it("rejects Timeline content through the canonical interpreter", () => {
    expect(() => interpretPreviewComposition(composition({
      id: "scene-1",
      name: "Garden",
      durationMs: 1000,
      objects: [],
      timeline: [{ id: "clip", kind: "OBJECT", startMs: 0, durationMs: 100 }],
    }))).toThrow(/Timeline is not supported/);
  });

  it("returns a deterministic empty visual plan for an empty Composition", () => {
    const result = interpretPreviewComposition({ ...base, scenes: [] });
    expect(result).toEqual({ backgroundColor: "#EEF1F5", objects: [] });
  });
});
