# Testing strategy

Stage 1 has no Gradle project. The check for that stage is `scripts/check-agent-docs.sh` and gitleaks. Both commands exist locally and in CI. From Stage 2, every commit that touches code must build.

## What to run on a commit

For the touched modules:

- the Android build of those modules;
- `compileKotlinIosSimulatorArm64` for the touched KMP modules;
- the tests of those modules;
- detekt, starting at Stage 3.

A full `xcodebuild` and a full CI run happen at the end of a stage, not on every commit. Write a long log to a file. Put the result and the error tail in chat.

JDK: JBR 21 from Android Studio. The path is in the [glossary](../conventions.md). Do not commit `JAVA_HOME` or `local.properties`.

## Kinds of tests

| Kind | Why | When |
| --- | --- | --- |
| Reducer and use case | Pure State and domain functions, without a device | Together with the first screen and repository |
| DI graph | Koin assembles in `:shared` | Stage 2 |
| Konsist | The reducer is called only from `reduce {}`, plus layer and package boundaries | Stage 3 |
| `checkModuleGraph` | `impl` does not see another feature's `impl`, `api` does not see features | Stage 3 |
| Roborazzi | UIKit component states | Stage 3 |
| Room migrations | The schema is exported and the move from the old version passes | Stage 4 |
| Receipt parser | The same text produces the same draft | Stage 4 |
| Model reply schema | Invalid JSON does not become a transaction | Stage 4 |

## What not to do

- Do not mark a stage done because one module compiled, if a contract between modules changed.
- Do not disable a test so a commit can pass. If a check is temporarily impossible, say in the stage report what was not run.
- Do not replace a UI screenshot, before Roborazzi, with a claim that the layout matches the design.
