# Agent tools

Agents read a short entry file and open one skill for the task. Do not load the whole repository into context.

## Where the instructions live

- `AGENTS.md` — invariants, search order, the seven phases.
- `AGENTS.md` inside a module — purpose, public contract, platform parts, business rules. Those cards appear together with the module.
- `.agents/skills/<name>/SKILL.md` — a skill. Cursor and Codex read this directory.
- `.claude/skills` — a symlink to `../.agents/skills`. `CLAUDE.md` contains only `@AGENTS.md`.
- `docs/project-map.md` — written by `./gradlew projectMap`. `./gradlew checkProjectMap` fails when the file is stale.

A skill is shorter than 300 lines. Details live in `references/` next to it. `scripts/check-agent-docs.sh` checks frontmatter (`name`, `description`), links, and sizes. The script runs in CI.

`server-service` covers a JVM process in the separate `server/` build. The first process is `api-gateway`. `:auth-service` checks one password and returns no session. `rotate` replaces one refresh family in memory and does not store it. `:database` opens the primary and the replica. `:cache` opens Redis. `:queue` opens Kafka. `:storage` opens R2. `:edge` checks the Cloudflare policy. None of those libraries is a process.

## How to save context

1. First `docs/roadmap.md`, then the project map or the [modules](../architecture/modules.md), then the module card.
2. Hand a wide repository search to a subagent when the map no longer answers the question.
3. Run Gradle on the touched modules. The full log goes to a file.
4. One task, one chat. Write the task status in `docs/roadmap.md`.
5. `.cursorignore` keeps the index from reading build directories.

## Seven phases

1. Understand. Read the roadmap and the module contract. Do not edit code until the module, the file, and the contract are named.
2. Plan. Find a sample in the repository and follow it. If there is no sample yet, follow the glossary and an architecture document, and say which document you are following.
3. Implement. Names only from the [glossary](../conventions.md).
4. Test. Build and tests of the touched modules. Commands are in the [testing strategy](../testing/strategy.md).
5. Review. Compare the diff with the sample and with the invariants in `AGENTS.md`.
6. Fix. On a red build, at most three iterations for one cause. Then stop and report: the command, the error, and what already changed.
7. Report. What was done, and what was not checked. Status goes in the roadmap.

The word "done" is not used without the checks that were required.
