# StorySprout AI Project Bootstrap

This repository is the durable engineering source of truth for StorySprout.

## Required reading for every new AI engineering session

Before changing code, read in this order:

1. `docs/project/engineering-memory.md`
2. `docs/project/current-state.md`
3. `docs/project/development-process.md`
4. `docs/project/decision-log.md`
5. `docs/project/session-handoff.md`
6. Relevant specification under `docs/specifications/`
7. Relevant feature implementation document under `docs/features/`

Then inspect the current Git branch and HEAD and verify that the documentation still matches the code.

## Operating rules

- Do not infer completed work from conversation history.
- Treat repository code, tests, specifications, and implementation docs as authoritative.
- Do not reimplement an existing feature because a previous chat session is unavailable.
- Before implementation, identify the current source of truth and existing boundaries.
- Use Specification-Driven Development (SDD) and Test-Driven Development (TDD).
- Keep Composition 1.1 as the canonical editable visual state unless an explicit architectural decision changes it.
- Preserve documented API, persistence, and renderer boundaries.
- Do not silently expand scope.
- When a milestone is completed, update the durable project memory and session handoff before ending the session.
- Every implementation session must leave an exact commit SHA and a concise next action.

## Recovery principle

ChatGPT conversations and Project chats are temporary context. This repository must contain enough durable context for a new AI session to recover the project state without access to an earlier conversation.
