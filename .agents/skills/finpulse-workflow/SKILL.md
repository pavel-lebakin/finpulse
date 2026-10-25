---
name: finpulse-workflow
description: Use at the start of any FinPulse task to pick the next roadmap step, load one skill, and follow the seven-phase workflow.
---

# Workflow

Read this at the start of a task, before searching the repository.

## Entry

1. [docs/roadmap.md](../../../docs/roadmap.md) — take the row whose status is `next` or `in progress`. Do not start the next stage while the current one is not `done`.
2. [AGENTS.md](../../../AGENTS.md) — invariants.
3. One skill from the table below. Do not open the others just in case.

## Which skill to open

| Situation | Skill |
| --- | --- |
| New feature Gradle module | `feature-module` |
| Screen, State, Action, Reducer | `mvi-screen` |
| Room, DTO, repository, showcase data | `data-layer` |
| Theme, token, component, catalog | `uikit` |
| Android/iOS implementation, Swift, expect/actual | `kmp-platform` |
| Scanner, assistant, Gemini key, prompt | `ai-features` |
| Secret, storage, network, threat model, dependency license | `security-review` |
| Diff review before a commit | `code-review` |
| Signing, store, policy | `release` |
| Server process under `server/` | `server-service` |

Phases: [references/phases.md](references/phases.md). Build and versions already checked: [references/build.md](references/build.md). Open the second file before Gradle, not instead of the roadmap.

A library the task needs whose license fails `finpulse.licensee` is a stop. Ask the user before dropping it, replacing it, or editing the allowlist. The rule is in `security-review`.
