# Roadmap

Statuses: `done`, `in progress`, `next`, `pending`.
The next chat starts from this file and from `AGENTS.md`, not from a walk of the repository.

The server is last.

| Stage | Status | Contents |
| --- | --- | --- |
| 1. Foundation | done | License, decision documents, AGENTS, skills, hooks, docs CI, and gitleaks |
| 2. Build and core | next | Wrapper, version catalog, build-logic, app shells, common/mvi/navigation/testing, UIKit, token showcase, Koin |
| 3. Quality | pending | detekt, Konsist, checkModuleGraph, Roborazzi, projectMap, mobile CI, Dependabot, CodeQL, Licensee |
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

## Stage 5 — documents that do not exist yet

The privacy policy is published through GitHub Pages from [docs/legal](legal/README.md).
Data safety and App Privacy answers will land in [docs/release](release/README.md).

## Stage 6 — a reminder of the boundary

Until this stage the mobile build has no account. The flag `finpulse.cloud.enabled=false` is the default.

## Decisions that do not block the start

- Exact versions of Kotlin, CMP, AGP, Room, Koin, and Ktor are chosen at Stage 2.
- ML Kit GenAI Prompt API availability — Stage 4.
- OpenAPI or a shared KMP module for the client-server contract — Stage 6.

## Log

- Stage 1 started: license, architecture, product, security, tests, skills, and CI are written into the repository.
- Stage 1 closed. The app does not build yet: Gradle arrives at Stage 2.
