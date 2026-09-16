import { z } from "zod";

export const compositionSchema = z.object({
  schemaVersion: z.literal("1.0"),
  projectId: z.string().min(1),
  width: z.number().int().positive(),
  height: z.number().int().positive(),
  fps: z.number().positive(),
  durationMs: z.number().int().nonnegative(),
  scenes: z.array(z.object({
    id: z.string().min(1),
    name: z.string().min(1),
    durationMs: z.number().int().nonnegative(),
    background: z.object({ assetId: z.string().min(1) }).optional(),
    objects: z.array(z.object({
      id: z.string().min(1), assetId: z.string().min(1), x: z.number(), y: z.number(),
      scale: z.number().positive(), rotation: z.number(), visible: z.boolean()
    })),
    timeline: z.array(z.object({
      id: z.string().min(1), kind: z.enum(["OBJECT", "DIALOGUE", "AUDIO", "MUSIC", "SFX"]),
      startMs: z.number().int().nonnegative(), durationMs: z.number().int().nonnegative(), sourceId: z.string().optional()
    }))
  }))
});

export type CompositionInput = z.infer<typeof compositionSchema>;
