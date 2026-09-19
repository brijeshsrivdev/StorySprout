import { describe, expect, it } from "vitest";
import { parseComposition } from "../src/index";

const valid = {
  schemaVersion: "1.1",
  projectId: "project-1",
  width: 1920,
  height: 1080,
  fps: 30,
  durationMs: 1000,
  scenes: [{
    id: "scene-1",
    name: "Garden",
    durationMs: 1000,
    background: { assetId: "background-preset:garden_day" },
    timeline: [],
    objects: [{
      id: "character-1",
      objectType: "CHARACTER",
      assetId: "character-1",
      x: 480,
      y: 540,
      scale: 1,
      rotation: 0,
      visible: true,
    }],
  }],
};

describe("Composition 1.1 runtime validation", () => {
  it("accepts a valid renderer Composition", () => {
    expect(parseComposition(valid).schemaVersion).toBe("1.1");
  });

  it("rejects missing semantic object type", () => {
    const invalid = structuredClone(valid);
    delete (invalid.scenes[0].objects[0] as Record<string, unknown>).objectType;
    expect(() => parseComposition(invalid)).toThrow();
  });

  it("rejects out-of-bounds positions and unsupported rotation", () => {
    const invalid = structuredClone(valid);
    invalid.scenes[0].objects[0].x = 1921;
    expect(() => parseComposition(invalid)).toThrow();
    invalid.scenes[0].objects[0].x = 480;
    invalid.scenes[0].objects[0].rotation = 15;
    expect(() => parseComposition(invalid)).toThrow();
  });

  it("rejects Timeline content and unsupported stage settings", () => {
    const invalid = structuredClone(valid);
    invalid.scenes[0].timeline = [{ id: "clip", kind: "OBJECT", startMs: 0, durationMs: 100 }];
    expect(() => parseComposition(invalid)).toThrow();
    invalid.scenes[0].timeline = [];
    invalid.width = 1280;
    expect(() => parseComposition(invalid)).toThrow();
  });

  it("rejects malformed background and duplicate extra scenes", () => {
    const invalid = structuredClone(valid);
    invalid.scenes[0].background = { assetId: "remote-image" };
    expect(() => parseComposition(invalid)).toThrow();
    invalid.scenes[0].background = valid.scenes[0].background;
    invalid.scenes.push(structuredClone(valid.scenes[0]));
    expect(() => parseComposition(invalid)).toThrow();
  });
});
