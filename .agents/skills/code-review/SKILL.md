---
name: code-review
description: Use before finishing a FinPulse change to compare the diff with the glossary, module boundaries, and the cited sample.
---

# Diff review

Read the whole diff. For each new call, name which project file the shape came from. While there is no code, the shape comes from [docs/conventions.md](../../../docs/conventions.md). Mark a parameter that is not in the sample, and confirm it with an architecture document.

## Look at every diff

- No other feature's `impl` in the dependencies.
- State changes only through reducer functions inside `reduce {}`.
- DTOs and entities did not leak into `domain`, `presentation`, or `api`.
- A Composable has no navigation and no data loading.
- Names match the glossary.
- No secrets and no generated build directories.

Next: [references/diff.md](references/diff.md).
