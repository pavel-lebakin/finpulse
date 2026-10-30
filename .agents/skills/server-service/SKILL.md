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

## auth-service

Package `com.lebakin.finpulse.server.auth`. Apply the application plugin.

`Application.auth(accounts)` answers `POST /sessions`. `signIn` returns `SignIn.Accepted` only when the email matches one supplied account and the password matches its `PasswordVerifier`. `passwordVerifier` uses PBKDF2-HMAC-SHA256, 600000 iterations, a 16-byte salt, and a 32-byte hash, then clears the password array. Anything else is `SignIn.Rejected`. The email and the password are printable ASCII without spaces, quotes, or backslashes, and the password is 8 to 128 characters. The route returns 204 or 401 with an empty body. `main` binds `127.0.0.1:8081` and passes an empty account list. The password, the email, the salt, and the hash stay out of `toString` and out of exception text. The route does not issue a refresh token. The process does not call Redis or PostgreSQL, and does not store an account. `:api-gateway` does not depend on it.

`refreshToken` returns 32 random bytes. `refreshFamily(id, accountId, token)` stores only the SHA-256 of that token. `rotate(token, family)` returns `Refresh.Rotated` when the hash matches the current token: the family id stays the same, the presented hash joins a remembered list of at most 8, and the result carries a new token. A hash that matches a remembered token returns `Refresh.Reused` and that family is revoked. A revoked family, a mismatch, or an unknown token is `Refresh.Rejected`. `rotate` clears the presented array. The token and the hashes stay out of `toString` and out of exception text. `POST /sessions` does not call `rotate`. `main` does not create a family. The process does not call Redis.

Check: `scripts/check.sh -p server :auth-service:test :auth-service:detekt :auth-service:licensee`.

## database

Package `com.lebakin.finpulse.server.database`. This is a library, not a process. Do not apply the application plugin.

`openPostgres(primary, replica)` returns `Postgres`. `write` uses the primary connection. `read` uses the replica connection. The same host, port, and database is rejected before connecting. The password stays out of the JDBC URL and out of `toString`. The module creates no tables and does not start PostgreSQL. `:api-gateway` does not depend on it.

Check: `scripts/check.sh -p server :database:test :database:detekt :database:licensee`.

The replica test needs a local `postgres` binary. `FINPULSE_POSTGRES` points at that executable. The fallbacks are `PATH`, `/Library/PostgreSQL/17/bin/postgres`, Homebrew `postgresql@17`, and `/usr/lib/postgresql/17/bin/postgres`.

## cache

Package `com.lebakin.finpulse.server.cache`. This is a library, not a process. Do not apply the application plugin.

`openRedis(endpoint)` returns `Redis`. `set` writes one value. `get` reads it, and a missing key returns null. The password stays out of `toString` and out of exception text. The module does not start Redis, does not store a refresh token, and does not apply a rate limit. `:api-gateway` does not depend on it.

Check: `scripts/check.sh -p server :cache:test :cache:detekt :cache:licensee`.

The test needs a local `redis-server` binary. `FINPULSE_REDIS` points at that executable. The fallbacks are `PATH`, `/opt/homebrew/bin/redis-server`, `/opt/homebrew/opt/redis/bin/redis-server`, `/usr/local/bin/redis-server`, and `/usr/bin/redis-server`.

## queue

Package `com.lebakin.finpulse.server.queue`. This is a library, not a process. Do not apply the application plugin.

`openKafka(endpoint)` returns `Kafka`. `publish` writes one record. `take` reads the latest record, and an empty topic returns null. The password stays out of `toString` and out of exception text. The module does not start Kafka, does not write an outbox, a dead-letter record, or an idempotency key. `:api-gateway` does not depend on it.

Check: `scripts/check.sh -p server :queue:test :queue:detekt :queue:licensee`.

The test needs a local `kafka-server-start` binary and a sibling `kafka-storage`. `FINPULSE_KAFKA` points at `kafka-server-start`. The fallbacks are `PATH`, `/opt/homebrew/bin/kafka-server-start`, Homebrew `kafka` under `opt/kafka`, `/usr/local/bin/kafka-server-start`, and `/usr/bin/kafka-server-start`. The broker the test starts is KRaft.

## storage

Package `com.lebakin.finpulse.server.storage`. This is a library, not a process. Do not apply the application plugin.

`openR2(endpoint)` returns `R2`. `put` writes one object with `Cache-Control: private, no-store`. `sign` returns a GET URL that expires in 60 seconds. The library does not return object bytes. The password stays out of `toString` and out of exception text. The module does not start MinIO and does not store a receipt. `:api-gateway` does not depend on it.

Check: `scripts/check.sh -p server :storage:test :storage:detekt :storage:licensee`.

The test needs a local `minio` binary. `FINPULSE_MINIO` points at that executable. The fallbacks are `PATH`, `/opt/homebrew/bin/minio`, `/usr/local/bin/minio`, and `/usr/bin/minio`. The binary is the community server. An AIStor build that denies S3 without a license does not satisfy the test. Community release `RELEASE.2025-09-07T16-13-09Z` does.

## edge

Package `com.lebakin.finpulse.server.edge`. This is a library, not a process. Do not apply the application plugin.

`openEdge(tunnel, shield)` returns `Edge` only for a proxied hostname, origin `127.0.0.1:8080`, TLS `full-strict`, HSTS, WAF, bot protection, and a rate limit of 100 requests in 60 seconds. `cacheControl(PublicFile)` is `public, max-age=86400`. `cacheControl(Receipt)` is `private, no-store`. The tunnel token and the Origin CA stay out of `toString` and out of exception text. `Edge` does not keep them. The module does not start cloudflared, does not call Cloudflare, and does not store a receipt. `:api-gateway` does not depend on it. The layout is [docs/server/cloudflare.md](../../../docs/server/cloudflare.md).

Check: `scripts/check.sh -p server :edge:test :edge:detekt :edge:licensee`.
