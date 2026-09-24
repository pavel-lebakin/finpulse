---
name: security-review
description: Use when handling secrets, BYOK storage, logging, network, threat-model updates, or checking that FinPulse data stays on device.
---

# Security check

Compare the change with [docs/security/guidelines.md](../../../docs/security/guidelines.md) and update [docs/security/threat-model.md](../../../docs/security/threat-model.md) when the system gains a new asset or boundary.

## Stop and do not commit

- The diff contains a key, token, password, keystore, `local.properties`, or the contents of `.env`.
- A release log contains an amount, receipt text, a prompt, or a key.
- Cleartext is enabled.
- The BYOK key is written to DataStore or UserDefaults as plaintext.
- `security-crypto` is chosen for Android.
- A model reply or OCR result is written to the database without user confirmation.
- A showcase row goes into sync.

The hook and CI run gitleaks. The hook does not replace rotation if a secret was already in history.

Checklist before a commit that touches security: [references/checklist.md](references/checklist.md).
