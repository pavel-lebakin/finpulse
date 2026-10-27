# Threat model

Revision: Stage 1. There is no executable app yet. Below are the assets and the decisions that code in later stages must follow. This file is updated at every stage: what is already protected, and what is still only planned.

## The system in this revision

A repository of documentation and rules for agents. The mobile client, the server, and receipt storage are not built yet. The development channel and its token are not in git.

## Assets

| Asset | Where it will live | When it appears |
| --- | --- | --- |
| Transactions and categories | Room on the device | Stage 4 |
| Receipt photos | Memory during capture and recognition. Not stored in Room. Cloud copies are Stage 6 | Stage 4.4, cloud at Stage 6 |
| BYOK key | Keystore + DataStore / Keychain | Stage 4 |
| Showcase history | Room, `origin = SHOWCASE` | Stage 4 |
| Release signing | a gitignored file and GitHub Secrets | Stage 5 |
| Cloud session | Keystore / Keychain, refresh family in Redis | Stage 6 |

Showcase is not the user's financial data and is not synced.

## Trust boundaries

- The repository is public to read. Treat everything in git as available to an outsider.
- Receipt text and the model reply are untrusted, even when OCR ran locally.
- The server does not trust the client. The client does not trust model text.
- Cloudflare terminates TLS for the client. The leaf certificate is not pinned.

## Threats and where we close them

| Threat | What we do | Stage |
| --- | --- | --- |
| A secret in git | gitignore, gitleaks in the hook and in CI, secrets only in the environment | Stage 1, then on every commit |
| A dependency with a forbidden license, or a swapped action | Licensee, SHA-pinned actions, Dependabot, CodeQL | Stage 3 |
| Another module reads the database directly | `core.database` imported only from `data` packages, Konsist | Stage 3–4 |
| The BYOK key in a backup or a log | Keystore / Keychain, no PII in release logs | Stage 4 |
| A transaction replaced by model text or OCR | Reply schema, the user confirms the write | Stage 4 |
| Cleartext and extra data in logs | Cleartext forbidden, log redaction | Stage 4 |
| Publishing without describing data collection | Policy, Data safety, App Privacy, `PrivacyInfo.xcprivacy` | Stage 5 |
| Refresh-token theft, reuse | Rotation and token families in Redis | Stage 6 |
| Direct access to the origin and API brute force | Tunnel, WAF, rate limit | Stage 6 |
| A receipt file swapped through the server | A short-lived signed URL, `Cache-Control: private, no-store` for private objects | Stage 6 |
| Prompt injection through receipt text | Untrusted input, a schema, no execution of free text | Stage 4 and Stage 6 |

## What code does not protect yet

At Stage 1 there is no binary, database, or network. The controls of this revision are the absence of secrets in the tree and the documentation check. That does not replace a MASVS check of a built app.

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
