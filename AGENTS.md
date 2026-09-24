# FinPulse

Offline-first personal money tracker for Android and iOS. Logic is Kotlin Multiplatform. UI is Compose Multiplatform. Package `com.lebakin.finpulse`. Cloud is the last stage. A build with `finpulse.cloud.enabled=false` does not link the account or sync.

You may read the sources and build them to evaluate the project. You may not redistribute them or publish them to stores. See [LICENSE](LICENSE).

## Invariants

- Feature business logic lives in `commonMain`. ViewModel and State have no Compose types.
- `Loadable` is a sealed interface, not a value class.
- A feature is `:feature:<name>:api` and `:feature:<name>:impl`. Another feature's `impl` is not a dependency. `api` depends only on `:core:common` and kotlinx. Each feature has its own Gradle `group`.
- `core` does not depend on features. `core.database` is imported only from `..data..` packages.
- MVI has no effects. Only `<Screen>Reducer` changes State, and only from `reduce {}`.
- Navigation: `<Feature>Navigator` is injected into `ScreenViewModel`. A Composable does not see NavController.
- DTOs and Room entities do not leave `data`. Names follow [docs/conventions.md](docs/conventions.md).
- No secrets in git. `CancellationException` is not swallowed.
- Showcase rows (`origin = SHOWCASE`) are not synced and are deleted by the first user operation.

## Where to look

1. [docs/roadmap.md](docs/roadmap.md) — what is done and which step is next.
2. `docs/project-map.md` — from Stage 3. Until then [docs/architecture/modules.md](docs/architecture/modules.md).
3. The module `AGENTS.md`, once the module exists.
4. One skill from `.agents/skills/` for the task.

Start a repository-wide search only when the roadmap, the map, and the module card no longer answer the question.

## Seven phases

1. Understand — roadmap, map, contract. Name the module, file, and contract before editing.
2. Plan — follow an existing file or, while none exists, the glossary.
3. Implement — only names from the glossary.
4. Test — build and tests of the touched modules. From Stage 3 also detekt.
5. Review — compare the diff with the sample and the invariants.
6. Fix — three iterations for one red-build cause, then a report.
7. Report — what was done and what was not checked. Write the status in the roadmap.

Phase details: skill `finpulse-workflow`.

## Checks

- Before Stage 2 there is no Gradle: the check is `scripts/check-agent-docs.sh` and gitleaks.
- After that: the Android build of the touched modules, `compileKotlinIosSimulatorArm64` for the touched KMP modules, and their tests.
- A full `xcodebuild` and full CI run happen at the end of a stage.
- JDK is JBR 21 from Android Studio via `JAVA_HOME`. Do not commit `local.properties`. The JDK path is in [docs/conventions.md](docs/conventions.md).
- Commit messages are Conventional Commits in English.

## Skills

| Task | Directory |
| --- | --- |
| Work order | `finpulse-workflow` |
| New feature module | `feature-module` |
| Screen | `mvi-screen` |
| Room, DTOs, mappers | `data-layer` |
| Tokens and components | `uikit` |
| expect/actual and Swift | `kmp-platform` |
| Scanner, assistant, BYOK | `ai-features` |
| Threats and secrets | `security-review` |
| Diff review | `code-review` |
| Stores | `release` |

`server-service` arrives at Stage 6.

## Do not

- Do not copy class names from another project. The folder tree is fine; names come only from the glossary.
- Do not add an effect channel, auth in the offline build, or a dependency on another feature's `impl`.
- Do not hardcode colors in a feature and do not create empty folders for later.
- Do not write "done" if the build, tests, or detekt (once it exists) were not run.
- Do not put a token, key, or keystore in the repository.

## Documents

- [Architecture](docs/architecture/overview.md)
- [Product](docs/product/vision.md)
- [Security](docs/security/guidelines.md)
- [Tests](docs/testing/strategy.md)
- [Agents](docs/ai/tools.md)
