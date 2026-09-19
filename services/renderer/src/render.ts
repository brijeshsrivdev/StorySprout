import { spawn } from "node:child_process";
import { interpretScene, type Composition } from "@storysprout/editor-model";
import { parseComposition } from "@storysprout/validation";

export interface RenderRequest {
  renderJobId: string;
  compositionId: string;
  compositionVersion: number;
  composition: Composition;
}

function escapeDrawText(value: string): string {
  return value.replaceAll("\\", "\\\\").replaceAll(":", "\\:").replaceAll(",", "\\,").replaceAll("'", "\\'");
}

export function validateRenderRequest(request: unknown): asserts request is RenderRequest {
  if (!request || typeof request !== "object" || !("composition" in request)) {
    throw new Error("Invalid render request");
  }
  const record = request as Record<string, unknown>;
  if (typeof record.renderJobId !== "string" || !record.renderJobId.trim()) {
    throw new Error("RenderJob id is required");
  }
  if (typeof record.compositionId !== "string" || !record.compositionId.trim()) {
    throw new Error("Composition id is required");
  }
  if (typeof record.compositionVersion !== "number" || !Number.isInteger(record.compositionVersion) || record.compositionVersion < 1) {
    throw new Error("Composition version is invalid");
  }
  parseComposition(record.composition);
}

export function buildFilterGraph(composition: Composition): string {
  validateRenderRequest({
    renderJobId: "test",
    compositionId: "test",
    compositionVersion: 1,
    composition
  });
  const scene = composition.scenes[0];
  const plan = interpretScene(scene);
  const filters: string[] = [];

  for (const object of plan.objects) {
    const x = Math.round(object.x - object.width / 2);
    const y = Math.round(object.y - object.height / 2);
    filters.push(`drawbox=x=${x}:y=${y}:w=${Math.round(object.width)}:h=${Math.round(object.height)}:c=${object.fill}:t=fill`);
    filters.push(`drawbox=x=${x}:y=${y}:w=${Math.round(object.width)}:h=${Math.round(object.height)}:c=${object.border}:t=8`);
    filters.push(`drawtext=fontfile=/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf:text='${escapeDrawText(object.label)}':x=${x}+20:y=${y}+(${Math.round(object.height)}-text_h)/2:fontsize=28:fontcolor=#171717:box=1:boxcolor=white@0.75:boxborderw=8`);
  }
  return filters.join(",");
}

export async function renderComposition(request: RenderRequest): Promise<Buffer> {
  validateRenderRequest(request);
  const scene = request.composition.scenes[0];
  const plan = interpretScene(scene);
  const durationSeconds = request.composition.durationMs / 1000;
  const filter = buildFilterGraph(request.composition);
  const args = [
    "-hide_banner", "-loglevel", "error",
    "-f", "lavfi", "-i", `color=c=${plan.backgroundColor}:s=1920x1080:r=30`,
    "-vf", filter,
    "-t", String(durationSeconds),
    "-an",
    "-c:v", "libx264",
    "-pix_fmt", "yuv420p",
    "-movflags", "+frag_keyframe+empty_moov",
    "-f", "mp4",
    "pipe:1"
  ];

  return new Promise((resolve, reject) => {
    const process = spawn(process.env.FFMPEG_PATH ?? "ffmpeg", args, { stdio: ["ignore", "pipe", "pipe"] });
    const chunks: Buffer[] = [];
    let stderr = "";
    process.stdout.on("data", chunk => chunks.push(Buffer.from(chunk)));
    process.stderr.on("data", chunk => { stderr += chunk.toString(); });
    process.on("error", reject);
    process.on("close", code => {
      if (code === 0) resolve(Buffer.concat(chunks));
      else reject(new Error(`FFmpeg exited with code ${code}: ${stderr.trim() || "unknown renderer error"}`));
    });
  });
}
