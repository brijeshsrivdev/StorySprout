# Architecture overview

```text
AI providers
    ↓ suggestions / assets
Project state
    ↓ user edits
Editor
    ↓ serialize
Composition JSON (canonical source of truth)
    ↓ consume
Renderer
```

The web app owns the editing experience. The API owns persistence and orchestration. The renderer is isolated from UI state and consumes a versioned composition contract.

Media binaries belong in object storage. PostgreSQL stores application metadata. Local development uses MinIO; production storage can be substituted later behind a storage abstraction.
