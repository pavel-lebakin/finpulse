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
