import assert from "node:assert/strict";
import { execFileSync } from "node:child_process";
import test from "node:test";
import { renderComposition, validateRenderRequest, type RenderRequest } from "./render.js";

const request: RenderRequest = {
  renderJobId: "job-1",
  compositionId: "composition-1",
  compositionVersion: 7,
  composition: {
    schemaVersion: "1.1",
    projectId: "project-1",
    width: 1920,
    height: 1080,
    fps: 30,
    durationMs: 250,
    scenes: [{
      id: "scene-1",
      name: "Garden",
      durationMs: 250,
      background: { assetId: "background-preset:garden_day" },
      timeline: [],
      objects: [
        { id: "character-1", objectType: "CHARACTER", assetId: "12345678-character", x: 480, y: 520, scale: 1, rotation: 0, visible: true },
        { id: "prop-1", objectType: "PROP", assetId: "prop-preset:ball", x: 900, y: 760, scale: 1, rotation: 0, visible: true }
      ]
    }]
  }
};

test("renderer validates supported Composition 1.1", () => {
  assert.doesNotThrow(() => validateRenderRequest(request));
});

test("renderer produces deterministic 1080p 16:9 MP4 output", async () => {
  const first = await renderComposition(request);
  const second = await renderComposition(request);
  assert.equal(first.subarray(4, 8).toString(), "ftyp");
  assert.equal(first.equals(second), true);
  const probe = execFileSync(
    "ffprobe",
    ["-v", "error", "-select_streams", "v:0", "-show_entries", "stream=width,height,r_frame_rate", "-of", "csv=p=0", "pipe:0"],
    { input: first }
  );
  assert.match(probe.toString().trim(), /1920,1080,30\/1/);
  const codec = execFileSync(
    "ffprobe",
    ["-v", "error", "-select_streams", "v:0", "-show_entries", "stream=codec_name,pix_fmt", "-of", "default=noprint_wrappers=1", "pipe:0"],
    { input: first }
  ).toString();
  assert.match(codec, /codec_name=h264/);
  assert.match(codec, /pix_fmt=yuv420p/);
});

test("renderer rejects malformed external Composition payloads", () => {
  const invalid = structuredClone(request) as unknown as Record<string, unknown>;
  const composition = invalid.composition as Record<string, unknown>;
  const scene = (composition.scenes as Array<Record<string, unknown>>)[0];
  scene.objects = [{
    id: "o",
    assetId: "character-1",
    x: 9999,
    y: 100,
    scale: 1,
    rotation: 0,
    visible: true
  }];
  assert.throws(() => validateRenderRequest(invalid), /Composition/);
});

test("renderer rejects missing render metadata", () => {
  const invalid = structuredClone(request) as unknown as Record<string, unknown>;
  delete invalid.renderJobId;
  assert.throws(() => validateRenderRequest(invalid), /RenderJob id/);
});

test("renderer rejects Timeline content", () => {
  const invalid = {
    ...request,
    composition: {
      ...request.composition,
      scenes: [{
        ...request.composition.scenes[0],
        timeline: [{ id: "clip", kind: "OBJECT" as const, startMs: 0, durationMs: 100 }]
      }]
    }
  };
  assert.throws(() => validateRenderRequest(invalid), /Timeline/);
});
