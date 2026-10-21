# Build

Read this before the first Gradle command. The versions below were checked on 2026-10-28. Do not look them up again. Check a new library with one request to `maven-metadata.xml`, not a web survey.

## Versions

File: [gradle/libs.versions.toml](../../../../gradle/libs.versions.toml).

Kotlin 2.4.21, AGP 9.3.1, Gradle 9.5.0, Compose Multiplatform 1.12.2, coroutines 1.11.0, serialization 1.12.0, Koin 4.2.2, lifecycle 2.11.0, navigation3-ui 1.2.0-rc01, activity-compose 1.13.0, detekt 2.0.0-alpha.6 (`dev.detekt`). Stable 1.23.8 does not parse Kotlin 2.4. Konsist 0.18.1 (`com.lemonappdev:konsist`) uses the same Kotlin 2.4.21. Roborazzi 1.77.0 (`roborazzi-compose-desktop`) captures `:core:uikit` component states on the JVM. Record with `-Proborazzi.record=true`; the plain `jvmTest` task verifies.

`compileSdk` 37.1, `minSdk` 28, `targetSdk` 36. JDK is JBR 21. The path is in [docs/conventions.md](../../../../docs/conventions.md).

Licensee 1.14.1 (`app.cash.licensee:licensee-gradle-plugin`) checks runtime licenses. The task is `licensee`. Checked against maven-metadata.xml on 2026-10-29.

Room 2.8.5 (`androidx.room:room-runtime`, `room-compiler`, plugin `androidx.room`) with `androidx.sqlite:sqlite-bundled` 2.6.2. KSP 2.3.12 (`com.google.devtools.ksp`) compiles Kotlin 2.4.21. DataStore preferences core 1.3.0 and Okio 3.17.0. `kotlinx-datetime` 0.7.1. Checked against maven-metadata.xml on 2026-10-29.

Ktor client 3.6.0 (`io.ktor:ktor-client-core`, `ktor-client-okhttp`, `ktor-client-darwin`). Checked against maven-metadata.xml on 2026-10-29. Release 3.6.1 has no iOS artifacts. The server build stays at Stage 6.

AboutLibraries 15.3.0 (`com.mikepenz:aboutlibraries-core`, `aboutlibraries-compose-m3`, plugin `com.mikepenz.aboutlibraries.plugin`). Checked against maven-metadata.xml on 2026-10-29. The license is Apache-2.0. `:shared` exports the notices. The settings screen draws them.

ML Kit GenAI Prompt API `com.google.mlkit:genai-prompt` 1.0.0-beta5. Checked against maven-metadata.xml on 2026-10-29. The license is ML Kit Terms of Service, a reviewed Licensee exception.

Koin screen binding is `io.insert-koin:koin-core-viewmodel` and `io.insert-koin:koin-compose-viewmodel` 4.2.2. State collection is `org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose` 2.11.0. Checked against maven-metadata.xml on 2026-10-29.

## Command

`scripts/check.sh` with full task names. Success prints one line, `OK`. Failure prints the `What went wrong` block and the `e:` lines. Full log: `build/agent/last.log`. Do not copy it into chat.

For a KMP module: `:module:jvmTest` and `:module:compileKotlinIosSimulatorArm64`. Host Android tests are off. Tests live in `commonTest` and run through `jvm()`. For `:androidApp` and `:catalog:android`: `assembleDebug`. For `:catalog:desktop`: `compileKotlin`. Module detekt: `:module:detekt`. All modules at once: `detekt`. Konsist: `:tooling:architecture-tests:test`. Config on top of the default is `config/detekt/detekt.yml`, when that file exists. Type resolution is off: the plain task reads all of `src/**/*.kt`, including iOS.

A full `xcodebuild` runs at the end of a stage, not for every module:

`xcodebuild -project iosApp/iosApp.xcodeproj -target iosApp -sdk iphonesimulator -configuration Debug CODE_SIGNING_ALLOWED=NO ARCHS=arm64 ONLY_ACTIVE_ARCH=YES build`

Without `ARCHS=arm64`, Compose resource sync picks `x86_64` and fails.

## Traps already solved

- The plugin is `com.android.kotlin.multiplatform.library`. The block is named `android`, not `androidLibrary`. Do not apply `org.jetbrains.kotlin.android`.
- `finpulse.kmp.compose` applies serialization, Compose, and the Compose compiler. A second Kotlin plugin `alias` in the module loads another classloader and breaks the build.
- `findProperty("finpulse.namespace")` is read from the module `gradle.properties`. `providers.gradleProperty` does not see it.
- `project(":catalog:shared").name = "catalogShared"`. The path is `:catalog:catalogShared`, the accessor is `projects.catalog.catalogShared`. The directory on disk is `catalog/shared`.
- `Destination` extends `NavKey`, so `navigation3-ui` is `api` on `:core:navigation`. On iOS, `SavedStateConfiguration` needs `polymorphic(NavKey::class)`.
- `compileSdk { version = release(37) { minorApiLevel = 1 } }`. Platform 37.1 is already installed locally. Do not delete SDK directories and do not write `package.xml` by hand.
- `withHostTest` warnings and deprecated `compose.*` warnings are noisy and do not fail the build. Do not fix them in passing.
