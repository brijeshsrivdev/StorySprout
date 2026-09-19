import { z } from "zod";

const sceneObjectSchema = z.object({
  id: z.string().min(1),
  objectType: z.enum(["CHARACTER", "PROP"]),
  assetId: z.string().min(1),
  x: z.number().finite().min(0).max(1920),
  y: z.number().finite().min(0).max(1080),
  scale: z.number().finite().min(0.25).max(3),
  rotation: z.literal(0),
  visible: z.boolean(),
});

const timelineClipSchema = z.object({
  id: z.string().min(1),
  kind: z.enum(["OBJECT", "DIALOGUE", "AUDIO", "MUSIC", "SFX"]),
  startMs: z.number().int().nonnegative(),
  durationMs: z.number().int().nonnegative(),
  sourceId: z.string().min(1).optional(),
});

const sceneSchema = z.object({
  id: z.string().min(1),
  name: z.string().min(1),
  durationMs: z.number().int().positive(),
  background: z.object({ assetId: z.string().startsWith("background-preset:") }).optional(),
  objects: z.array(sceneObjectSchema),
  timeline: z.array(timelineClipSchema).length(0),
});

export const compositionSchema = z.object({
  schemaVersion: z.literal("1.1"),
  projectId: z.string().min(1),
  width: z.literal(1920),
  height: z.literal(1080),
  fps: z.literal(30),
  durationMs: z.number().int().positive(),
  scenes: z.array(sceneSchema).length(1),
});

export type CompositionInput = z.infer<typeof compositionSchema>;

export function parseComposition(value: unknown): CompositionInput {
  return compositionSchema.parse(value);
}
