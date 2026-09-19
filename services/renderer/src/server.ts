import { createServer } from "node:http";
import { renderComposition } from "./render.js";

const port = Number(process.env.PORT ?? 4000);

async function readJson(req: import("node:http").IncomingMessage): Promise<unknown> {
  const chunks: Buffer[] = [];
  for await (const chunk of req) chunks.push(Buffer.from(chunk));
  return JSON.parse(Buffer.concat(chunks).toString("utf8")) as unknown;
}

const server = createServer(async (req, res) => {
  if (req.url === "/health" || req.url === "/ready") {
    res.writeHead(200, { "content-type": "application/json" });
    res.end(JSON.stringify({ status: "ok" }));
    return;
  }
  if (req.method === "POST" && req.url === "/render") {
    try {
      const request = await readJson(req);
      const artifact = await renderComposition(request);
      res.writeHead(200, { "content-type": "video/mp4", "content-length": String(artifact.length) });
      res.end(artifact);
    } catch (error) {
      const message = error instanceof Error ? error.message : "Renderer failed";
      res.writeHead(422, { "content-type": "application/json" });
      res.end(JSON.stringify({ error: "render_failed", message }));
    }
    return;
  }
  res.writeHead(404, { "content-type": "application/json" });
  res.end(JSON.stringify({ error: "not_found" }));
});

server.listen(port, () => console.log(`StorySprout renderer listening on ${port}`));
