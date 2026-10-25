---
name: server-service
description: Use when adding a FinPulse server process, its route, or the separate Gradle build under server/.
---

# Server service

The server is a separate Gradle build in `server/`. It is not a module of the mobile project, and a build with `finpulse.cloud.enabled=false` does not link it.

## Build

`server/settings.gradle.kts` includes only the services that exist. Plugins come from `build-logic`. Versions come from [gradle/libs.versions.toml](../../../gradle/libs.versions.toml). Apply `finpulse.jvm.library` and the Gradle `application` plugin. Do not apply the Compose application plugin.

Check from the repository root:

`scripts/check.sh -p server :api-gateway:test :api-gateway:detekt :api-gateway:licensee`

## api-gateway

Package `com.lebakin.finpulse.server.gateway`.

`Application.gateway()` is the Ktor module. `GET /health` returns status 200 and the text `ok`. `main` in `Gateway.kt` binds `127.0.0.1` and port `8080`. No other route. The process does not store data.

Names: [docs/conventions.md](../../../docs/conventions.md). Add a row there before creating the next service. Do not add an empty service for later.
