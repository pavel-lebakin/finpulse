# FinPulse

Offline-first money tracker for Android and iOS. Shared logic is Kotlin
Multiplatform. The UI is Compose Multiplatform. A cloud sync tier is added
only after the store build works without a server.

Package and application id: `com.lebakin.finpulse`.
Component catalog: `com.lebakin.finpulse.catalog`.

This repository is **source-available**. You may read it and compile it
locally to evaluate the project. You may not redistribute it or publish it
to an app store. See [LICENSE](LICENSE).

## Status

Stage 2 builds. Android debug APKs, the desktop catalog, and the iOS simulator
Kotlin framework compile. Current status: [docs/roadmap.md](docs/roadmap.md).

Local JDK is JBR 21 from Android Studio, via `JAVA_HOME`. `sdk.dir` goes in
gitignored `local.properties`. See [docs/conventions.md](docs/conventions.md).

## Docs

- [Architecture](docs/architecture/overview.md)
- [Naming](docs/conventions.md)
- [Product](docs/product/vision.md)
- [Security](docs/security/guidelines.md)
- [Testing](docs/testing/strategy.md)
- [Agent tools](docs/ai/tools.md)
- [Agent entry point](AGENTS.md)

## Secrets

Signing material, API keys, and tokens are not stored in git. Use
gitignored files, the environment, or GitHub Actions secrets.
