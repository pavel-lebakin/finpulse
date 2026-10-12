# Checklist

- A secret lives in gitignore, in an environment variable, or in a GitHub Secret. A document example uses a clear placeholder, not a real key.
- Android: AES-GCM, the key in Keystore, ciphertext in DataStore. iOS: Keychain `WhenUnlockedThisDeviceOnly`.
- `CancellationException` is not mapped to `AppError`.
- A private receipt file is not cached as public static content. For future R2 that is `Cache-Control: private, no-store` and a short-lived signed URL.
- The client does not pin the Cloudflare leaf certificate.
- The offline build does not contain account modules.
- The threat model received this stage's revision if assets changed.
- A dependency outside the license allowlist was added only after the user agreed. The coordinate is a reviewed `allowDependency` in `scripts/check-licensee.sh`.
