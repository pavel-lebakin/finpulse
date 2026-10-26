# Testing strategy

Stage 1 has no Gradle project. The check for that stage is `scripts/check-agent-docs.sh` and gitleaks. Both commands exist locally and in CI. From Stage 2, every commit that touches code must build.

## What to run on a commit

For the touched modules:

- the Android build of those modules;
- `compileKotlinIosSimulatorArm64` for the touched KMP modules;
- the tests of those modules;
- detekt, starting at Stage 3.

A full `xcodebuild` happens at the end of a stage, not on every commit. Run Gradle tasks through `scripts/check.sh`: chat gets the result, and the full log stays in `build/agent/last.log`. Which tasks to take is in the `finpulse-workflow` skill, file `references/build.md`.

[`.github/workflows/mobile.yml`](../../.github/workflows/mobile.yml) runs on every push and pull request. The Android job assembles `:androidApp` and `:catalog:android` debug and runs `checkProjectMap`. The iOS job runs `compileKotlinIosSimulatorArm64` for each Kotlin Multiplatform module. That job does not run `xcodebuild`.

[`.github/dependabot.yml`](../../.github/dependabot.yml) asks GitHub to open weekly version-update pull requests for Gradle (`/` and `/build-logic`) and for GitHub Actions. `scripts/check-dependabot.sh` checks that file. The `docs-and-secrets` workflow runs the script. Dependabot itself runs on GitHub after the file is on the default branch.

[`.github/workflows/codeql.yml`](../../.github/workflows/codeql.yml) analyzes `java-kotlin` on every push and pull request. Kotlin needs a build, so the job compiles with `./gradlew --no-daemon` after CodeQL init and does not compile the iOS targets. `scripts/check-codeql.sh` checks that workflow. The `docs-and-secrets` workflow runs the script. The analysis itself runs on GitHub.

Licensee 1.14.1 is applied by `finpulse.licensee` on every module convention. The `licensee` task fails the build when a runtime dependency uses a license outside Apache-2.0, MIT, BSD-2-Clause, and BSD-3-Clause. Reviewed exceptions are the on-device ML Kit text recognizer and the artifacts it pulls, the Gemini Nano prompt API (`genai-prompt` 1.0.0-beta5, `genai-common` 1.0.0-beta5, `genai-schema` 1.0.0-alpha2, `play-services-basement` 18.9.0, `reactive-streams` 1.0.3), plus `org.slf4j:slf4j-api:2.0.19` (MIT; Licensee 1.14.1 does not map its published URL) and `org.postgresql:postgresql:42.7.14` (BSD-2-Clause; Licensee 1.14.1 does not map `https://jdbc.postgresql.org/about/license.html`). Each coordinate is an `allowDependency` with a reason, and `scripts/check-licensee.sh` rejects any other exception. `scripts/check-licensee.sh` checks that config. The `docs-and-secrets` workflow runs the script. The iOS job in [`.github/workflows/mobile.yml`](../../.github/workflows/mobile.yml) runs `licensee`, because that runner can resolve the Android, JVM, and Kotlin/Native graphs. The Android KMP library plugin is not `com.android.library`, so Licensee skips that Kotlin target; the Android application runtime classpath still includes it.

JDK: JBR 21 from Android Studio. The path is in the [glossary](../conventions.md). Do not commit `JAVA_HOME` or `local.properties`.

## Kinds of tests

| Kind | Why | When |
| --- | --- | --- |
| Reducer and use case | Pure State and domain functions, without a device | Together with the first screen and repository |
| DI graph | Koin assembles in `:shared` | Stage 2 |
| Konsist | The reducer is called only from `reduce {}`, plus layer and package boundaries | Stage 3 |
| `checkModuleGraph` | `impl` does not see another feature's `impl`, `api` does not see features | Stage 3 |
| `checkProjectMap` | `docs/project-map.md` matches the Gradle graph and module cards | Stage 3 |
| Roborazzi | UIKit component states | Stage 3 |
| Room migrations | The schema is exported and the move from the old version passes | Stage 4 |
| Receipt parser | The same text produces the same draft | Stage 4 |
| Model reply schema | Invalid JSON does not become a transaction | Stage 4 |

## What not to do

- Do not mark a stage done because one module compiled, if a contract between modules changed.
- Do not disable a test so a commit can pass. If a check is temporarily impossible, say in the stage report what was not run.
- Do not claim a UIKit layout matches the design unless `:core:uikit:jvmTest` compared the Roborazzi images.
