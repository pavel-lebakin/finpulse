# Security rules

The mobile app reference is OWASP MASVS L1. For the cloud, when it appears, also ASVS, OWASP API Top 10, and OWASP LLM Top 10. The threat model is updated at every stage: [threat-model.md](threat-model.md).

## Secrets

Git has no signing material, keys, tokens, or passwords. Places for them: a gitignored file, an environment variable, a GitHub Actions secret.

If a secret lands in a commit, rotate it. The next commit that deletes the file does not clean history.

## BYOK key

The user's Gemini key is stored like this:

- Android: an AES-GCM key in Android Keystore, ciphertext in DataStore. Do not use the `security-crypto` library. It is deprecated.
- iOS: Keychain, accessibility `WhenUnlockedThisDeviceOnly`.

The key is not written to logs, to analytics, or into screen State in full. The settings screen shows only that the key is saved, and a "delete" action.

## Network and logs

HTTPS only. Cleartext is forbidden in the app's debug configuration too, not only in release.

Release-build logs have no PII: no amounts, no receipt text, no prompt contents, no key. A debug log may show the operation type and the error code.

## Untrusted input

OCR text and the model reply are untrusted input. Model output is accepted only after a schema check. Arbitrary model text is not executed as a command.

Any write that changes transactions, categories, or data settings requires user confirmation. Read-only assistant scenarios call our use cases. They do not build SQL or a query from model text.

The prompt does not contain an "ignore the rules" instruction as a way to widen the model's rights. Prompt protection and the reply schema live in `:core:ai`.

## Errors

`CancellationException` is not caught as an ordinary error and is not turned into `AppError`. Other exceptions map to `NoConnection`, `Server`, or `Unexpected`. The original `cause` is kept.
