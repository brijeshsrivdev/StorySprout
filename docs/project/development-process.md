# StorySprout Development Process

## Session start protocol

Every engineering session must:

1. Read `PROJECT_BOOTSTRAP.md`.
2. Read the durable project memory files under `docs/project/`.
3. Inspect the current branch and HEAD.
4. Read the relevant specification and feature implementation documents.
5. Inspect the existing implementation and tests.
6. State the current milestone, source of truth, scope, and open questions before implementation.

## Implementation protocol

For each feature:

1. Confirm or create the specification.
2. Define acceptance criteria and explicit non-goals.
3. Identify affected domain/API/persistence/UI/renderer boundaries.
4. Write or update tests first where practical.
5. Implement the smallest vertical slice.
6. Run the relevant validation.
7. Review the diff for accidental scope expansion.
8. Update feature implementation documentation.
9. Update `docs/project/current-state.md`.
10. Record important architectural decisions in `decision-log.md`.
11. Update `session-handoff.md`.
12. Commit with a descriptive message and record the exact SHA.

## Session end protocol

The session handoff must contain:

- branch
- HEAD SHA
- completed work
- validation actually run
- validation not run
- important decisions
- open questions
- known issues
- explicitly not implemented
- exact recommended next action
- relevant files/specifications

Never claim tests, builds, CI, or deployment validation that was not actually performed.

## Recovery protocol

If the previous Project chat is unavailable:

- do not reconstruct the project from memory;
- read the repository memory and current-state documents;
- inspect HEAD and relevant code;
- reconcile documentation against implementation;
- continue from the documented next action.

The repository is the durable memory. Conversation history is supplemental only.
