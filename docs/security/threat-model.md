# Threat model

Revision: Stage 6.4, 2026-10-30. The tables say what code already does and what is still only a library or a plan. This file is updated at every stage. A revision is appended. The tables are not replaced by a paragraph.

## The system in this revision

Android and iOS share one offline ledger. The default build has `finpulse.cloud.enabled=false`, so `:feature:account` is not linked. Receipt photos stay in memory. Receipt text leaves the device only after consent and a saved Gemini key. The user confirms a transaction before it is written.

`server/` is a separate Gradle build. `:api-gateway` answers `GET /health` on `127.0.0.1:8080` and stores nothing. `:auth-service` answers `POST /sessions` on `127.0.0.1:8081`. When a Redis store is passed, a matching password returns 200 and one refresh token. Without that store the route returns 401. `rotate` replaces one refresh family in memory and revokes it when a remembered token is presented again. `saveFamily` writes that hash family to Redis and `loadFamily` reads it. `main` registers no account, creates no family, and does not open Redis. `:database`, `:cache`, `:queue`, `:storage`, and `:edge` are libraries. The gateway does not call them. The mobile graph does not include them. No receipt is uploaded.

## Assets

| Asset | Where it lives | In this revision |
| --- | --- | --- |
| Transactions and categories | Room on the device | Present since Stage 4.1. Not uploaded |
| Receipt photos | Memory during capture and recognition. Not stored in Room | Present since Stage 4.4. `:storage` can write one private object. No service stores a receipt |
| BYOK key | Android: AES-GCM in Keystore, ciphertext in DataStore under `noBackupFilesDir`. iOS: Keychain `WhenUnlockedThisDeviceOnly` | Present since Stage 4.5. A Keychain read that is not a success counts as no key |
| Showcase history | Room, `origin = SHOWCASE` | Present since Stage 4.1. `enqueue` refuses it |
| Release signing | Gitignored `keystore.properties` and GitHub Actions secrets | Present since Stage 5 |
| Cloud session | A refresh token returned by `POST /sessions`, and the hash record `saveFamily` writes to Redis | The route issues one token when a Redis store is passed. The token is not the Redis value. The response is `Cache-Control: private, no-store`. The token and its hash stay out of `toString` and out of exception text. Nothing is written to Keystore or the Keychain |
| Sync outbox | `finpulse-outbox.db` in `:feature:account` | Present only when `finpulse.cloud.enabled=true`. Not sent |
| Server credentials | Caller arguments to Postgres, Redis, Kafka, R2, and Edge | Present in the libraries. Omitted from `toString` and from exception text. Not in git |

Showcase is not the user's financial data and is not synced.

## Trust boundaries

- The repository is public to read. Treat everything in git as available to an outsider.
- Receipt text and the model reply are untrusted, even when OCR ran locally.
- The server does not trust the client. The client does not trust model text. `:auth-service` accepts or rejects one password. `rotate` replaces the current refresh token and revokes that family when a remembered token is presented again. `saveFamily` writes the hashes, not the token. The route issues one refresh token when a Redis store is passed. The gateway has no data route.
- Cloudflare terminates TLS for the client. The leaf certificate is not pinned.
- The offline source set does not link the account module. The server build is not in the mobile graph.
- A replica that repeats the primary, a hostname that is not proxied, and an origin other than `127.0.0.1:8080` are rejected before a connection or a policy object is returned.
- Passwords, the tunnel token, and the Origin CA are caller-supplied. `Edge` does not keep the token or the certificate.

## Threats and where we close them

| Threat | What we do | Stage | Closed by code |
| --- | --- | --- | --- |
| A secret in git | gitignore, gitleaks in the hook and in CI, secrets only in the environment | Stage 1, then on every commit | Yes |
| A dependency with a forbidden license, or a swapped action | Licensee, SHA-pinned actions, Dependabot, CodeQL | Stage 3 | Yes |
| Another module reads the database directly | `core.database` imported only from `data` packages, Konsist | Stage 3–4 | Yes. The outbox entities stay in `:feature:account` |
| The BYOK key in a backup or a log | Keystore / Keychain, no PII in release logs | Stage 4 | Yes |
| A transaction replaced by model text or OCR | Reply schema, the user confirms the write | Stage 4 | Yes |
| Cleartext and extra data in logs | Cleartext forbidden, log redaction | Stage 4 | Yes on the device. The server libraries do not require TLS to PostgreSQL, Redis, Kafka, or object storage |
| Publishing without describing data collection | Policy, Data safety, App Privacy, `PrivacyInfo.xcprivacy` | Stage 5 | Yes for the offline app and the optional Gemini send. The policy does not describe an account |
| Refresh-token theft, reuse | Rotation and token families in Redis | Stage 6 | `saveFamily` writes the hash family to Redis and `loadFamily` reads it. A remembered token still revokes that family after a reload. `POST /sessions` returns one token when a Redis store is passed and writes only the hash. `main` does not open Redis |
| Direct access to the origin and API brute force | Tunnel, WAF, rate limit | Stage 6 | The check only. `:edge` accepts that policy and rejects any other shape. No tunnel process runs |
| A receipt file swapped through the server | A short-lived signed URL, `Cache-Control: private, no-store` for private objects | Stage 6 | The library only. `:storage` sets that header and a GET URL that expires in 60 seconds. Nothing uploads a receipt |
| Prompt injection through receipt text | Untrusted input, a schema, no execution of free text | Stage 4 and Stage 6 | Yes on the device. There is no server model |
| A showcase row enters sync | `enqueue` returns false and does not insert | Stage 6 | Yes in the cloud build. The offline build has no outbox |
| The account module in the offline binary | `accountModule` only from `cloudMain` | Stage 6 | Yes. The default flag is false |
| A server secret in a log or an exception | Omitted from `toString` and from exception text | Stage 6 | Yes for the library types and for `:auth-service` |

## What code does not protect yet

The device ledger, the BYOK secret, and on-device receipt parsing are in the app. The server libraries and the local outbox are not an account. This document is not a MASVS or ASVS audit of a deployed system. Nothing here is deployed.

Still open: a stored account, a client sign-in screen, a refresh presented to the route, TLS from the libraries to PostgreSQL, Redis, Kafka, and object storage, a running tunnel, a receipt upload, and `ledger-sync-service` and `ai-proxy-service`. Sync does not call the network. `:auth-service` issues one refresh session from `POST /sessions` when a Redis store is passed. It does not rotate that session over HTTP, and no other route authenticates a client.

## How to update

At the end of a stage, append a revision: the date, what appeared in the system, which table row is closed by code, and what remains. Do not replace the table with a paragraph that says security was considered.

## Revision 2026-10-29 — Stage 4.4

The scanner holds a receipt image in memory only long enough to read it. The image is not inserted into Room and is not sent to a server. The review screen writes a transaction only after Confirm, through `LedgerWriter`. That write is the first user operation, so showcase rows are still deleted with it.

iOS reads the image with Vision. Android reads Latin text with ML Kit on the device. The image is not written to Room and is not sent by our code. ML Kit `text-recognition` 16.0.1 and the Play Services artifacts it pulls are a reviewed license exception. Samples are parsed on both platforms without a photo.

Still open: the on-device model, BYOK, network, and receipt storage in the cloud.

## Revision 2026-10-29 — Stage 4.5 security

`:core:security` can store one secret. Android generates an AES-GCM key in Android Keystore (`finpulse_byok`) and writes only ciphertext to DataStore. That file is under `noBackupFilesDir`, so backup does not copy it. The keystore key is not extractable and is not created with `security-crypto`. iOS stores the secret in the Keychain as a generic password with `kSecAttrAccessibleWhenUnlockedThisDeviceOnly`. `observeSaved` reports a boolean. Callers that need the value use `read()` and must not put it in screen State or logs.

The JVM `openSecretStore` keeps the value in memory so graph tests can run. It is not a product store.

Still open: network and cleartext policy, the on-device model, the consent and settings screens that collect the key, prompt checks, and receipt storage in the cloud. The store is not called from a screen yet.

## Revision 2026-10-29 — Stage 4.5 network

`:core:network` sends one HTTPS POST. `HttpsClient` rejects any other scheme, and a URL with userinfo, before the engine is called. Redirects stay off, so a 3xx cannot continue onto cleartext. A transport failure is `NoConnectionException`. Any other status is `ServerStatusException` without the response body. The client does not pin a certificate and does not log the body or headers. Android declares `INTERNET` and sets `usesCleartextTraffic` to false with a network security config that applies to every build, including debug. iOS sets `NSAllowsArbitraryLoads` to false. No feature calls the client yet. The offline build still has no account.

`org.slf4j:slf4j-api:2.0.19` is a reviewed Licensee exception. OkHttp pulls it for the JVM and Android engines. The license is MIT. Licensee 1.14.1 does not map `https://opensource.org/license/mit` to that SPDX id.

Still open: the on-device model, the consent and settings screens that collect the key, prompt checks, and receipt storage in the cloud. The secret store is not called from a screen yet. The HTTPS client is not called from a screen yet.

## Revision 2026-10-29 — Stage 4.5 ai

`:core:ai` keeps receipt text between data markers. A marker that appears inside the receipt is broken, so the block still has one end. The instruction text does not tell the model to follow commands from that block. `checkReceiptReply` accepts only one JSON object with the five receipt keys. A repeated key is rejected. The amount is a positive integer of at most nine digits, the currency is USD, EUR, GBP, or RUB, the date is a real calendar day, and the category is one of four labels. Any other reply, including invalid JSON, is `ReceiptCheck.Rejected`. The accepted value is a `ReceiptReply`. It is not written as a transaction. No feature calls the module yet.

Still open: the on-device model, the consent and settings screens that collect the key, and receipt storage in the cloud. The secret store is not called from a screen yet. The HTTPS client is not called from a screen yet. The prompt helper is not called from a screen yet.

## Revision 2026-10-29 — Stage 4.5 on-device

`:core:ai` can ask Gemini Nano on Android when ML Kit reports the prompt engine as available. `openOnDeviceModel` returns null when the engine is missing, still downloading, or only downloadable. This step does not download the model and does not send the prompt to a server. iOS has no engine yet. The JVM has none. `complete` returns text. An empty reply or a model failure is `OnDeviceFailed`. That text is not written as a transaction. The prompt and the reply are not logged. `com.google.mlkit:genai-prompt:1.0.0-beta5` and `genai-common:1.0.0-beta5` use the ML Kit Terms of Service. `genai-schema:1.0.0-alpha2` and `play-services-basement:18.9.0` use the Android Software Development Kit License. `org.reactivestreams:reactive-streams:1.0.3` is CC0-1.0, pulled through `kotlinx-coroutines-reactive`. Those five coordinates are reviewed Licensee exceptions.

Still open: the Swift bridge, the consent and settings screens that collect the key, and receipt storage in the cloud. The secret store is not called from a screen yet. The HTTPS client is not called from a screen yet. The prompt helper is not called from a screen yet. The on-device model is not called from a screen yet.

## Revision 2026-10-29 — Stage 4.5 Swift bridge

`:core:ai` accepts an `OnDeviceEngine` from Swift. `iosApp` registers Apple Foundation Models on iOS 26 and later, before Koin starts. The iOS platform module binds that engine. `openOnDeviceModel` returns a model only when `ready` is true. An unavailable Apple Intelligence model, including one that is not downloaded yet, returns null. This step does not download the model and does not send the prompt to a server. `complete` returns text. An empty reply or a model failure is `OnDeviceFailed`. That text is not written as a transaction. The prompt and the reply are not logged. The JVM still has no engine.

Still open: the consent and settings screens that collect the key, and receipt storage in the cloud. The secret store is not called from a screen yet. The HTTPS client is not called from a screen yet. The prompt helper is not called from a screen yet. The on-device model is not called from a screen yet.

## Revision 2026-10-29 — Stage 4.5 BYOK

`:core:ai` can call Gemini when the secret store already holds one key. `openByokModel` returns null when the key is missing, blank, or not a single printable ASCII token of at most 512 characters, and then the network is not called. `complete` sends the prompt as JSON to `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent`. The key is the `x-goog-api-key` header only. It is not written into the URL, the body, an exception message, or a log. A deleted key fails with `ByokFailed` before the next post. An empty or unreadable reply is `ByokFailed` and is not a transaction. Connection and HTTP status errors stay `NoConnectionException` and `ServerStatusException`. This step does not save a key and does not show the consent screen. No feature calls the model yet.

Still open: the consent and settings screens that collect the key, and receipt storage in the cloud. The secret store is not called from a screen yet. The on-device model is not called from a screen yet. The BYOK model is not called from a screen yet.

## Revision 2026-10-29 — Stage 4.5 consent

`:core:preferences` stores one boolean, `sending_granted`, in the same preferences file. It stays false until the user agrees. `:feature:settings` shows that choice before a Gemini key can be saved. The screen says receipt text would be sent to Google Gemini and would leave the device. Agree sets the flag. Decline clears it. Neither action reads or writes the secret store, and neither calls the network. `openByokModel` returns null when the flag is false, before it reads a key. `complete` checks the flag again and throws `ByokFailed` without a post when the user has declined. The key field is still absent. The dashboard opens this screen.

Still open: the settings screen that collects the key, and receipt storage in the cloud. The secret store is not called from a screen yet. The on-device model is not called from a screen yet. The BYOK model is not called from a screen yet.

## Revision 2026-10-29 — Stage 4.5 settings

`:feature:settings` collects a Gemini key after consent. The field is shown only when `sending_granted` is true. Save writes the key through `SecretStore` when it is one printable ASCII token of at most 512 characters. The key is not placed in screen State, a message, or a log. The screen then shows that a key is saved and offers Delete. Delete clears the secret. Without consent, or with a blank, spaced, or over-long key, `save` is not called. The screen does not call the network and does not read the secret back. The dashboard opens this screen.

Still open: receipt storage in the cloud. The on-device model is not called from a screen yet. The BYOK model is not called from a screen yet. The HTTPS client is not called from a screen yet.

## Revision 2026-10-29 — Stage 4.5 licenses

`:shared` exports one AboutLibraries file from the app classpath. The plugin runs offline and does not fetch license text. `:feature:settings` reads that file through `LicenseReader` and draws it with AboutLibraries. The screen does not collect a key, does not read the secret store, and does not call the network. The dashboard opens it.

Still open: receipt storage in the cloud. The on-device model is not called from a screen yet. The BYOK model is not called from a screen yet. The HTTPS client is not called from a screen yet.

## Revision 2026-10-29 — Stage 4.6

`:feature:scanner` parses camera and gallery text through an engine chain. A ready on-device model is asked first. The prompt is `receiptPrompt`. The reply is accepted only by `checkReceiptReply`. An accepted `ReceiptReply` is a draft on the review screen, not a transaction. A missing engine, a model failure, or a rejected reply falls through to the rule-based parser. Samples stay on that parser and do not call a model. BYOK is opened only when the rules cannot read the text. `openByokModel` still returns null without consent or an acceptable key, and then no request is made. A successful local parse does not read the key and does not call the network. Connection and HTTP status errors from a BYOK call propagate. The prompt, the reply, and the key are not logged. Confirm still writes through `LedgerWriter`.

Still open: the assistant, and receipt storage in the cloud.

## Revision 2026-10-29 — Stage 4.7

`:feature:assistant` turns a phrase into a draft or into one read-only answer. The month-spend question is the intent `MonthSpend` in our code. It calls `LedgerReader.observeTransactions` and sums this month's expenses in the currency of the newest transaction. The prompt does not name that intent, a query, or any other question. Any other question gets a fixed reply and does not open a model. A phrase the rules can read becomes a draft and does not open BYOK. A phrase with a digit that the rules cannot read goes through `phrasePrompt`: on-device first, then BYOK. The reply is accepted only by `checkReceiptReply`. An accepted reply is a draft on the confirm screen, not a transaction. Confirm writes through `LedgerWriter`. A missing consent or key does not call the network. The phrase, the reply, and the key are not logged.

Still open: receipt storage in the cloud.

## Revision 2026-10-29 — Stage 5

The release row is closed in the table above. `:androidApp` release is minified with R8. The signing material stays out of git: `keystore.properties` is gitignored, and `.github/workflows/release-android.yml` only names `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD`. The privacy policy in `docs/legal/index.html` says the ledger stays on the device, the receipt photo is not stored, and receipt text leaves the device only after consent and a saved Gemini key. The same split is written for Play Data safety and App Privacy. `PrivacyInfo.xcprivacy` records that optional user content. `ITSAppUsesNonExemptEncryption` is false because the iOS app uses HTTPS and the Keychain. Android AES-GCM stays in the Android source set.

Still open: receipt storage in the cloud.

## Revision 2026-10-29 — Stage 6 gateway

`server/` is a separate Gradle build. `:api-gateway` answers `GET /health` with status 200 and the text `ok`. `main` listens on `127.0.0.1:8080` only. Any other path is 404. The process stores nothing, has no account, and is not linked from the mobile app. `finpulse.cloud.enabled` stays false. The health body is not a ledger and not a receipt.

Still open: auth, PostgreSQL, Redis, Kafka, receipt storage, the tunnel, and the account module in a cloud build. The refresh-token, origin, and receipt-file rows in the table above stay open.

## Revision 2026-10-30 — Stage 6 PostgreSQL

`:database` in the server build opens two JDBC connections. `write` uses the primary. `read` uses the replica. The same host, port, and database is rejected before a connection. The password is not placed in the JDBC URL or in `toString`. The module creates no tables and stores no account, ledger, or receipt. `:api-gateway` does not call it. The mobile app does not link it. `finpulse.cloud.enabled` stays false. A test starts a local primary and a streaming replica on `127.0.0.1` and checks that a primary write is visible through `read`, and that `read` is in recovery. This step does not force TLS to PostgreSQL.

`org.postgresql:postgresql:42.7.14` is BSD-2-Clause. Licensee 1.14.1 does not map `https://jdbc.postgresql.org/about/license.html`, so that coordinate is a reviewed exception.

Still open: auth, Redis, Kafka, receipt storage, the tunnel, TLS to PostgreSQL, and the account module in a cloud build. The refresh-token, origin, and receipt-file rows in the table above stay open.

## Revision 2026-10-30 — Stage 6 Redis

`:cache` in the server build opens one Redis connection. `set` writes a value and `get` reads it. A missing key returns null. The password is not placed in `toString` or in an exception message. The module does not start Redis, does not store a refresh token or a rate-limit counter, and does not store an account, ledger, or receipt. `:api-gateway` does not call it. The mobile app does not link it. `finpulse.cloud.enabled` stays false. A test starts `redis-server` on `127.0.0.1` with the password in a temporary config file, not on the process command line. This step does not force TLS to Redis. No Redis client library is on the classpath.

Still open: auth, Kafka, receipt storage, the tunnel, TLS to PostgreSQL, TLS to Redis, and the account module in a cloud build. The refresh-token, origin, and receipt-file rows in the table above stay open.

## Revision 2026-10-30 — Stage 6 Kafka

`:queue` in the server build opens one Kafka client. `publish` writes one record and `take` reads the latest record. An empty topic returns null. The password is not placed in `toString` or in an exception message. The module does not start Kafka, does not write an outbox, a dead-letter record, or an idempotency key, and does not store an account, ledger, or receipt. `:api-gateway` does not call it. The mobile app does not link it. `finpulse.cloud.enabled` stays false. A test starts one KRaft broker on `127.0.0.1` with SASL/PLAIN. The password is in a temporary config file, not on the process command line. This step does not force TLS to Kafka. The client is `org.apache.kafka:kafka-clients` 4.4.0, Apache-2.0.

Still open: auth, receipt storage, the tunnel, TLS to PostgreSQL, TLS to Redis, TLS to Kafka, and the account module in a cloud build. The refresh-token, origin, and receipt-file rows in the table above stay open.

## Revision 2026-10-30 — Stage 6 R2

`:storage` in the server build opens one S3 client. `put` writes one object with `Cache-Control: private, no-store`. `sign` returns a GET URL that expires in 60 seconds. The library does not return object bytes. The password is not placed in `toString` or in an exception message. The module does not start MinIO, does not store a receipt, and does not store an account or a ledger. `:api-gateway` does not call it. The mobile app does not link it. `finpulse.cloud.enabled` stays false. A test starts MinIO on `127.0.0.1`. The password is in a temporary env file, not on the process command line. This step does not force TLS to storage. No S3 client library is on the classpath.

The receipt-file row is met by this library. No service uploads a receipt yet.

Still open: auth, a receipt upload, the tunnel, TLS to PostgreSQL, TLS to Redis, TLS to Kafka, TLS to storage, and the account module in a cloud build. The refresh-token and origin rows in the table above stay open.

## Revision 2026-10-30 — Stage 6 Cloudflare

`:edge` in the server build accepts a policy only when DNS is proxied, the origin is `127.0.0.1:8080`, TLS is `full-strict`, and HSTS, WAF, and bot protection are on. The rate limit is 100 requests in 60 seconds. `cacheControl(PublicFile)` is `public, max-age=86400`. `cacheControl(Receipt)` is `private, no-store`. The tunnel token and the Origin CA are not placed in `toString` or in an exception message, and `Edge` does not keep them. The module does not start cloudflared, does not call Cloudflare, does not store a receipt, and does not store an account or a ledger. `:api-gateway` does not call it. The mobile app does not link it. `finpulse.cloud.enabled` stays false. The leaf certificate stays unpinned.

The origin row is met by this library. No tunnel process is started.

Still open: auth, a receipt upload, a running tunnel, TLS to PostgreSQL, TLS to Redis, TLS to Kafka, TLS to storage, and the account module in a cloud build. The refresh-token row in the table above stays open.

## Revision 2026-10-30 — Stage 6 sync

`:feature:account` keeps a Room outbox in `finpulse-outbox.db`, separate from the ledger database. `enqueue` returns false for `SHOWCASE` and does not insert that change. `merge` keeps the higher category revision. A transaction whose revision or body differs stays local and is a `SyncConflict`. `:shared` compiles `offlineMain` while `finpulse.cloud.enabled` is false, and that source set does not link `accountModule`. `cloudMain` includes it only when the flag is true. The default stays false. The outbox classes are not in `:core:database`. No screen calls sync, and sync does not call the network. There is still no account sign-in.

Still open: auth, a receipt upload, a running tunnel, TLS to PostgreSQL, TLS to Redis, TLS to Kafka, TLS to storage, and sign-in. The refresh-token row in the table above stays open.

## Revision 2026-10-30 — Stage 6 threat model

The tables at the top now match this revision. Rows closed by code are the secret-in-git check, Licensee and the pinned actions, database import rules, the BYOK store, confirmed writes, mobile cleartext, the store privacy answers, showcase refusal, the offline build, and server secrets kept out of `toString` and exception text. The origin row and the receipt-file row are checks inside `:edge` and `:storage`. No tunnel runs, and no receipt is uploaded. The refresh-token row stays open. Prompt injection is closed on the device. There is no server model.

`finpulse.cloud.enabled` stays false. Stage 6 stops at this record. Sign-in, token rotation, datastore TLS, a running tunnel, a receipt upload, and the auth, ledger-sync, and ai-proxy services are not in this revision.

## Revision 2026-10-30 — Stage 6.1 sign-in

`:auth-service` answers `POST /sessions` on `127.0.0.1:8081`. `signIn` returns `Accepted` only when the email matches one account passed to `auth` and the password matches a PBKDF2-HMAC-SHA256 verifier: 600000 iterations, a 16-byte salt, and a 32-byte hash. The route returns 204 or 401 with an empty body. `main` passes an empty account list, so that process accepts no sign-in. `passwordVerifier` clears the password array. The password, the email, the salt, and the hash are not placed in `toString` or in an exception message. The process does not issue a refresh token, does not call Redis or PostgreSQL, does not store an account, and is not called by `:api-gateway`. The mobile app does not link it. `finpulse.cloud.enabled` stays false.

A body that is not the JSON object returns 401 without hashing. An unknown email still runs the hash against a placeholder verifier and stays 401. A short password returns 401 without hashing.

Still open: a stored account, a client sign-in screen, refresh-token rotation, TLS to PostgreSQL, TLS to Redis, TLS to Kafka, TLS to storage, a running tunnel, a receipt upload, and `ledger-sync-service` and `ai-proxy-service`. The refresh-token row stays open.

## Revision 2026-10-30 — Stage 6.2 refresh rotation

`rotate` in `:auth-service` returns `Rotated` only when the SHA-256 of the presented token matches the current hash of that `RefreshFamily`. The family id stays the same. The presented hash joins a remembered list of at most 8, and the result carries a new 32-byte token. Presenting a remembered token returns `Reused` and the family is revoked, so the current token then fails too. A revoked family, a mismatch, or an unknown token is `Rejected`. `rotate` clears the presented array. `refreshFamily` stores the hash, not the token. The token and the hashes are not placed in `toString` or in an exception message. `POST /sessions` does not call `rotate`. `main` creates no family. The process does not call Redis or PostgreSQL, does not store an account, and is not called by `:api-gateway`. The mobile app does not link it. `finpulse.cloud.enabled` stays false.

Still open: a stored account, a client sign-in screen, a refresh family stored in Redis, a session issued by the route, TLS to PostgreSQL, TLS to Redis, TLS to Kafka, TLS to storage, a running tunnel, a receipt upload, and `ledger-sync-service` and `ai-proxy-service`. The refresh-token row is a check in memory. Redis still holds no family.

## Revision 2026-10-30 — iOS Keychain on an unsigned process

`KeychainSecretStore` treats a `SecItemCopyMatching` status other than success as no secret. `observeSaved` stays false, and BYOK does not open. `save` and `delete` still fail when the Keychain rejects them. The simulator target is ad-hoc signed so a local run can use the Keychain. A device build signs only when `TEAM_ID` is set outside git. CI still passes `CODE_SIGNING_ALLOWED=NO`, and that process has no key. The secret is still not logged.

## Revision 2026-10-30 — Stage 6.3 refresh family in Redis

`saveFamily` in `:auth-service` writes one `RefreshFamily` through `Redis.set`. The key is `refresh:` plus the family id. The value is version `1`, the family id, the account id, the revoked flag, the current SHA-256 as hex, and at most 8 remembered hashes. The token is not in the value. `loadFamily` returns that family. A missing key returns null. Any other value throws `Auth refresh record is invalid.` and the message does not include the value. A reload still rotates a current token and revokes the family when a remembered token is presented. `POST /sessions` does not call `saveFamily` or `loadFamily`. `main` does not open Redis and does not create a family. The process does not call PostgreSQL, does not store an account, and is not called by `:api-gateway`. `:cache` still does not define a refresh record. The mobile app does not link the server. `finpulse.cloud.enabled` stays false. This step does not force TLS to Redis.

Still open: a stored account, a client sign-in screen, a session issued by the route, TLS to PostgreSQL, TLS to Redis, TLS to Kafka, TLS to storage, a running tunnel, a receipt upload, and `ledger-sync-service` and `ai-proxy-service`. The refresh-token row is a hash family in Redis that the route does not issue.

## Revision 2026-10-30 — Stage 6.4 session issued by the route

`issueSession` in `:auth-service` returns a `Session` for one account id. The family id is 32 hex characters from random bytes and is not derived from the token. The token is 32 bytes. The family stores only its SHA-256. When `POST /sessions` is given a Redis store and the password matches, the route calls `issueSession` and `saveFamily`, then returns 200 and `familyId.tokenHex` with `Cache-Control: private, no-store`. The text omits the email, the password, the account id, and the hash. Redis stores the hash, not the token. `issueSession` clears its token array, and the route clears the session token after writing the text. Any other call returns 401 with an empty body and the same cache header. The route does not call `rotate` or `loadFamily`. `main` calls `auth(emptyList())` and does not open Redis, so the process issues no session. The token stays out of `toString` and out of exception text. The process does not call PostgreSQL, does not store an account, and is not called by `:api-gateway`. The mobile app does not link it. `finpulse.cloud.enabled` stays false. This step does not force TLS to Redis.

Still open: a stored account, a client sign-in screen, a refresh presented to the route, TLS to PostgreSQL, TLS to Redis, TLS to Kafka, TLS to storage, a running tunnel, a receipt upload, and `ledger-sync-service` and `ai-proxy-service`. The refresh-token row is a hash family in Redis that the route issues once and does not rotate over HTTP.

## Revision 2026-10-30 — Android document scan

The Android camera opens the ML Kit document scanner. Play services crops and cleans the page on the device and returns JPEG pages. Our code stitches those pages in memory and does not write them to Room. If the scanner cannot start, the camera writes one full-size JPEG in the app cache and reads it back. Gallery bytes stay as chosen. ML Kit text recognition still runs on the device. The image is not sent by our code. `play-services-mlkit-document-scanner` 16.0.0 is a reviewed license exception. The settings screen no longer shows an open-source list, and the app no longer exports AboutLibraries. Notices stay in `NOTICE` and `THIRD_PARTY_NOTICES`.

Still open: a stored account, a client sign-in screen, a refresh presented to the route, TLS to PostgreSQL, TLS to Redis, TLS to Kafka, TLS to storage, a running tunnel, a receipt upload, and `ledger-sync-service` and `ai-proxy-service`.
