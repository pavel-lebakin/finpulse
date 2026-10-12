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
