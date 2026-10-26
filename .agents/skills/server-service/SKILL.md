---
name: server-service
description: Use when adding a FinPulse server process, its route, or the separate Gradle build under server/.
---

# Server service

The server is a separate Gradle build in `server/`. It is not a module of the mobile project, and a build with `finpulse.cloud.enabled=false` does not link it.

## Build

`server/settings.gradle.kts` includes only the projects that exist. Plugins come from `build-logic`. Versions come from [gradle/libs.versions.toml](../../../gradle/libs.versions.toml). Apply `finpulse.jvm.library`. A process also applies the Gradle `application` plugin. Do not apply the Compose application plugin.

Check from the repository root:

`scripts/check.sh -p server :api-gateway:test :api-gateway:detekt :api-gateway:licensee`

## api-gateway

Package `com.lebakin.finpulse.server.gateway`.

`Application.gateway()` is the Ktor module. `GET /health` returns status 200 and the text `ok`. `main` in `Gateway.kt` binds `127.0.0.1` and port `8080`. No other route. The process does not store data.

Names: [docs/conventions.md](../../../docs/conventions.md). Add a row there before creating the next service. Do not add an empty service for later.

## database

Package `com.lebakin.finpulse.server.database`. This is a library, not a process. Do not apply the application plugin.

`openPostgres(primary, replica)` returns `Postgres`. `write` uses the primary connection. `read` uses the replica connection. The same host, port, and database is rejected before connecting. The password stays out of the JDBC URL and out of `toString`. The module creates no tables and does not start PostgreSQL. `:api-gateway` does not depend on it.

Check: `scripts/check.sh -p server :database:test :database:detekt :database:licensee`.

The replica test needs a local `postgres` binary. `FINPULSE_POSTGRES` points at that executable. The fallbacks are `PATH`, `/Library/PostgreSQL/17/bin/postgres`, Homebrew `postgresql@17`, and `/usr/lib/postgresql/17/bin/postgres`.
