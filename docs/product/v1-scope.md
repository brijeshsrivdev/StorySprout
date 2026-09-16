# StorySprout V1 scope

The first product slice is a browser-based kids video creation workflow: story setup, outline, characters, scenes, editor, timeline, preview, and render.

This repository foundation deliberately stops before product functionality. The next stages should be incremental and validated before adding the next layer.

## Source of truth

AI output is never the canonical project state. AI may propose story content or generate assets. The creator accepts/edits those results. The editor serializes the project into the canonical Composition JSON. The renderer consumes that Composition JSON.
