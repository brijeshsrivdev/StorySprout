# Rendering architecture

The renderer starts as a runnable health/readiness service only.

Future rendering flow:

1. API validates a project composition.
2. API creates a render job.
3. A worker receives the job.
4. Renderer loads Composition JSON and referenced assets.
5. Renderer produces scenes and audio, then an MP4.
6. Output is uploaded to object storage.
7. Render job status is updated.

FFmpeg, queues and rendering implementation are intentionally deferred from this foundation.
