import { spawn } from "node:child_process";
import { interpretScene, type Composition } from "@storysprout/editor-model";

export interface RenderRequest {
  renderJobId: string;
  compositionId: string;
  compositionVersion: number;
  composition: Composition;
}

function escapeDrawText(value: string): string {
  return value.replaceAll("\\", "\\\\").replaceAll(":", "\\:").replaceAll(",", "\\,").replaceAll("'", "\\'");
}

export function validateRenderRequest(request: RenderRequest): void {
  const composition = request.composition;
  if (composition.schemaVersion !== "1.1") throw new Error("Unsupported Composition schema");
  if (composition.width !== 1920 || composition.height !== 1080 || composition.fps !== 30) {
    throw new Error("V1 renderer requires 1920x1080 at 30 fps");
  }
  if (composition.durationMs <= 0) throw new Error("Composition duration must be positive");
  if (composition.scenes.length !== 1) throw new Error("V1 renderer supports exactly one scene");
  if (composition.scenes[0].durationMs <= 0) throw new Error("Scene duration must be positive");
  if (composition.scenes[0].timeline.length > 0) throw new Error("Timeline is not supported by V1 renderer");
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
