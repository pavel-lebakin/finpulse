# :core:security

Package `com.lebakin.finpulse.core.security`. Depends on DataStore and coroutines. No feature dependency.

Public types: `SecretStore`, `openSecretStore`. Android calls `bindAndroidSecretDirectory` before the first open.

`SecretStore` saves one secret, reads it back, deletes it, and exposes `observeSaved` as a boolean. The value is not part of that flow. Android keeps an AES-GCM key in Android Keystore and the ciphertext in DataStore under `noBackupFilesDir`. iOS writes the secret to the Keychain with `WhenUnlockedThisDeviceOnly`. A read that is not a success is no secret, so a missing entitlement does not crash the process. `save` and `delete` still fail when the Keychain rejects them. The JVM store is in memory for tests and is not a product store. `security-crypto` is not used.

Check: `scripts/check.sh :core:security:jvmTest :core:security:compileKotlinIosSimulatorArm64 :core:security:detekt`.
