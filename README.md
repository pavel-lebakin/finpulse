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

The app does not build yet. Stage 1 records the architecture and the agent
workflow. The next stage adds the Gradle build. Current status:
[docs/roadmap.md](docs/roadmap.md).

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
