# Seven phases

## 1. Understand

Read the roadmap and the module map. Name these in the reply, before the first edit:

- the module and its Gradle path;
- the file that will change, or the file that does not exist yet;
- the contract: who calls it and what it returns.

If the repository has no code yet, take the contract from `docs/`, not from an imagined class.

## 2. Plan

Find one existing sample file. While there are no samples, the sample is [docs/conventions.md](../../../../docs/conventions.md). Name the path in the reply. Do not claim "following the pattern" without showing which file.

## 3. Implement

Change only what the named contract needs. Check names against the glossary. Do not put platform code in `commonMain`, except `expect` for a short function.

## 4. Test

Commands are in [docs/testing/strategy.md](../../../../docs/testing/strategy.md). At Stage 1 that is `scripts/check-agent-docs.sh` and gitleaks. From Stage 2, build and test the touched modules, not the whole monorepo for every small change.

## 5. Review

Open the `code-review` skill if the diff is more than a couple of lines. Check the invariants in the root `AGENTS.md`.

## 6. Fix

One red-build cause gets at most three edits. After the third failure, stop and write: the command, the first error, and which three edits were already tried. Do not switch to another hypothesis in silence.

## 7. Report

Two blocks: what changed (files) and what was not checked. Change a milestone status in `docs/roadmap.md` when the milestone is actually closed, not when the first file is written.
