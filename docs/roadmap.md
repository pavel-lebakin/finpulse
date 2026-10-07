# Roadmap

Statuses: `done`, `in progress`, `next`, `pending`.
The next chat starts from this file and from `AGENTS.md`, not from a walk of the repository.

The server is last.

| Stage | Status | Contents |
| --- | --- | --- |
| 1. Foundation | done | License, decision documents, AGENTS, skills, hooks, docs CI, and gitleaks |
| 2. Build and core | done | Wrapper, version catalog, build-logic, app shells, common/mvi/navigation/testing, UIKit, token showcase, Koin |
| 3. Quality | in progress | detekt, Konsist, checkModuleGraph, Roborazzi, projectMap, mobile CI, Dependabot, CodeQL, Licensee |
| 4.1 Room and ledger | pending | Room, DataStore, transactions, and showcase history |
| 4.2 Onboarding | pending | Four screens, showcase banner, clear, show again |
| 4.3 Insights | pending | Dashboard and charts |
| 4.4 Scanner | pending | Camera, gallery, samples, OCR, rule-based parser, review screen |
| 4.5 AI platform | pending | security, network, ai, on-device, Swift bridge, BYOK, consent, settings, licenses |
| 4.6 Receipt parsing by a model | pending | Engine chain in the scanner |
| 4.7 Assistant | pending | Drafts from a phrase and answers through a read-only use case |
| 5. Stores | pending | R8, signing, policy, Data safety, App Privacy, xcprivacy, assets, release workflow, final README |
| 6. Cloud | pending | Ktor services, Postgres, Redis, Kafka, R2, Cloudflare, sync behind a flag, threat model |

Stage 4 in the original plan is grouped as 4.1–4.7. The table splits the rows so the next chat takes one milestone.

## Stage 2 — what actually builds

- Gradle wrapper, version catalog, convention plugins, typesafe accessors. Versions are checked against the docs on this step, not ahead of time.
- `:androidApp`, `:shared`, and `iosApp` run on both platforms.
- `:core:common`, `:core:mvi`, `:core:navigation`, `:core:testing` with tests.
- `:core:uikit` and `:catalog` with the token configurator.
- The Koin root and a graph test.

Navigation 3 or navigation-compose 2.x is chosen here.

## Stage 3 — what actually builds

Take one item at a time. Do not open the whole architecture: module boundaries are already in the root `AGENTS.md`, and the check is in [docs/testing/strategy.md](testing/strategy.md).

- detekt on the touched modules. Done: `finpulse.detekt`, task `:module:detekt`. Do not weaken a rule so a commit can pass.
- Konsist. Done: `:tooling:architecture-tests`, Konsist 0.18.1. Extensions of `*Reducer` on `*State` only from `reduce {}`. `Loadable` is a sealed interface. `*Dto` and `*Entity` stay in a `data` package or in `com.lebakin.finpulse.core.database`.
- `checkModuleGraph` by the rules in [docs/architecture/modules.md](architecture/modules.md).
- Roborazzi for `:core:uikit` component states.
- `projectMap` writes `docs/project-map.md` from the graph and the module `AGENTS.md` cards. `checkProjectMap` fails when the file is stale.
- CI runs `scripts/check.sh` for the Android and iOS compile. Done: `.github/workflows/mobile.yml` assembles the Android debug apps, compiles `iosSimulatorArm64`, and runs `checkProjectMap`. A full `xcodebuild` runs at the end of the stage.
- Dependabot, CodeQL, and Licensee — configs and a check that they run. Do not change feature code. Done: each has a config and a `scripts/check-*.sh` job in `docs-and-secrets`. Licensee also runs as the `licensee` task on the mobile iOS job.

## Stage 5 — documents that do not exist yet

The privacy policy is published through GitHub Pages from [docs/legal](legal/README.md).
Data safety and App Privacy answers will land in [docs/release](release/README.md).

## Stage 6 — a reminder of the boundary

Until this stage the mobile build has no account. The flag `finpulse.cloud.enabled=false` is the default.

## Decisions that do not block the start

- Build versions are recorded in `gradle/libs.versions.toml`. Room and Ktor wait until their stage. Do not look them up again.
- ML Kit GenAI Prompt API availability — Stage 4.
- OpenAPI or a shared KMP module for the client-server contract — Stage 6.

## Log

- Stage 1 started: license, architecture, product, security, tests, skills, and CI are written into the repository.
- Stage 1 closed. The app does not build yet: Gradle arrives at Stage 2.
- Stage 2 closed. Build: Kotlin 2.4.21, AGP 9.3.1, Gradle 9.5.0, Compose Multiplatform 1.12.2, Koin 4.2.2. Navigation is Navigation 3 (`navigation3-ui` 1.2.0-rc01). `compileSdk` 37.1, `minSdk` 28, `targetSdk` 36. Cloud is off: `finpulse.cloud.enabled=false`. Agent notes are in the `finpulse-workflow` skill. The check is `scripts/check.sh`.
- Stage 3 started. detekt 2.0.0-alpha.6 is wired, and the `:module:detekt` task is green. The next item is Konsist.
- Stage 3, Konsist. Module `:tooling:architecture-tests`, Konsist 0.18.1. The next item is `checkModuleGraph`.
- Stage 3, `checkModuleGraph`. The root task checks feature, core, and catalog dependency boundaries. The next item is Roborazzi.
- Stage 3, Roborazzi. `:core:uikit` `jvmTest` captures component states with Roborazzi 1.77.0. The next item is `projectMap`.
- Stage 3, `projectMap`. `./gradlew projectMap` writes `docs/project-map.md`. `checkProjectMap` fails when the file is stale. The next item is mobile CI, which should run `checkProjectMap`.
- Stage 3, mobile CI. `.github/workflows/mobile.yml` runs the Android debug assemble, `compileKotlinIosSimulatorArm64`, and `checkProjectMap`. The next item is Dependabot.
- Stage 3, Dependabot. `.github/dependabot.yml` schedules weekly Gradle updates for `/` and `/build-logic`, and GitHub Actions updates. `scripts/check-dependabot.sh` checks the file. The next item is CodeQL.
- Stage 3, CodeQL. `.github/workflows/codeql.yml` analyzes `java-kotlin` with a manual Gradle build. `scripts/check-codeql.sh` checks the workflow. The next item is Licensee.
- Stage 3, Licensee. `finpulse.licensee` allows Apache-2.0, MIT, BSD-2-Clause, and BSD-3-Clause. `scripts/check-licensee.sh` checks the config, and the mobile iOS job runs `licensee`. The next item is the Stage 3 close: a full `xcodebuild`.
