# Server

The server is the last stage (Stage 6). Until then the app is meant to be fully offline. First the store without a server, then the cloud.

The server is a separate Gradle build in `server/`, not a module of the mobile project. The stack is Ktor.

Services, added one at a time:

- `api-gateway` — `Application.gateway()` answers `GET /health` with status 200 and the text `ok`. `main` binds `127.0.0.1:8080`. No other route. The process stores nothing.
- `auth-service`
- `ledger-sync-service`
- `ai-proxy-service`

Data and infrastructure:

- PostgreSQL: `:database` is a library, not a process. `openPostgres(primary, replica)` opens one JDBC connection for `Postgres.write` and one for `Postgres.read`. The same host, port, and database is rejected before connecting. The password is a connection property and is omitted from `toString`. The module does not start PostgreSQL, does not create tables, and is not called by `:api-gateway`. The driver is `org.postgresql:postgresql` 42.7.14 (BSD-2-Clause). Licensee 1.14.1 does not map its published URL, so that coordinate is a reviewed exception.
- Redis: rate limit, refresh-token families, cache.
- Kafka in KRaft mode: events, outbox, DLQ, idempotency.
- Photo storage: Cloudflare R2, or MinIO locally. The client receives a short-lived signed URL and talks to storage directly. JVM services do not proxy the file.
- Public static files are cached at the edge. Private receipts are served with `Cache-Control: private, no-store`.

Cloudflare, described later in `docs/server/cloudflare.md` at Stage 6:

- DNS is proxied, the origin is closed with a Tunnel, no open ports.
- TLS Full (strict), Origin CA, HSTS.
- WAF, rate limiting, bot protection.
- R2 and CDN.

The client does not pin the leaf certificate: Cloudflare rotates it. Network is HTTPS only. Cleartext is forbidden.

On the client, the flag `finpulse.cloud.enabled` switches a source set in `:shared`. When the flag is off, `account` and sync are not linked. The API contract between client and server (OpenAPI or a shared KMP module) is chosen at Stage 6, not earlier.

Cloud references: ASVS, OWASP API Top 10, OWASP LLM Top 10. The threat model is updated at that stage, not once at the end.
