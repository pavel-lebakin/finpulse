# Release

The store build has no account. It does not send transactions to a FinPulse server. The privacy policy is [docs/legal/index.html](../legal/index.html).

## Signing

Release signing reads a gitignored `keystore.properties` at the repository root. The file is already listed in `.gitignore`, together with `*.jks` and `*.keystore`. Do not commit it.

The file has four keys. `storeFile` is a path relative to the repository root, or an absolute path. The other three are the store password, the key alias, and the key password. This document does not contain those values.

When the file is absent, `:androidApp:bundleRelease` still runs R8 and writes an unsigned bundle. When the file is present, the release build type uses the `release` signing config.

The manual workflow `.github/workflows/release-android.yml` runs only on `workflow_dispatch`. It decodes the keystore from GitHub Actions secrets and writes `keystore.properties` on the runner. The secrets are:

| Secret | What it is |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64 of the keystore file |
| `ANDROID_KEYSTORE_PASSWORD` | Store password |
| `ANDROID_KEY_ALIAS` | Key alias |
| `ANDROID_KEY_PASSWORD` | Key password |

The workflow names those secrets. It does not contain the keystore or a password. The artifact is `finpulse-release`, an `.aab` from `androidApp/build/outputs/bundle/release/`.

## R8

The Android release build type sets `isMinifyEnabled` and `isShrinkResources`. Rules are `proguard-android-optimize.txt` plus `androidApp/proguard-rules.pro`. Debug builds stay unminified.

## Google Play Data safety

Answers for the store form. "Collected" here means sent off the device. Data that stays on the device is not collected.

| Question | Answer |
| --- | --- |
| Does the app collect or share user data? | Yes. One optional type. |
| Is all of the user data collected by the app encrypted in transit? | Yes. The only off-device call is HTTPS. |
| Do you provide a way for users to request that their data is deleted? | There is no account and no server copy. The user deletes transactions on the device. Delete on the key screen removes the Gemini key. |
| Is data sold? | No. |
| Ads, analytics, or tracking? | No. |

| Data type | Collected? | Notes |
| --- | --- | --- |
| Financial info | No | Transactions and categories stay in the on-device database. |
| Photos | No | The camera or the gallery is used to read a receipt. The image stays in memory and is not uploaded. |
| Other user content | Yes, optional | Receipt text, or a phrase the on-device rules could not read, is sent to Google Gemini after the user agrees and saves a key. Purpose: app functionality. The user can decline. Declining does not save a key and does not send the request. |
| Device or other IDs | No | The app does not read an advertising id. |
| Approximate or precise location | No | |
| Contacts, messages, files, calendar, audio | No | |

The Gemini key is stored on the device and is sent only as the `x-goog-api-key` header of a Gemini request. Play's form has no type for that credential. It is described in the privacy policy. It is not sold.

Android Auto Backup can include the ledger and the preferences file. It does not include the key file, which is under `noBackupFilesDir`.

## Apple App Privacy

The app does not track. `NSPrivacyTracking` is false. There is no tracking domain.

| Category | Collected? | Notes |
| --- | --- | --- |
| Financial Info | No | The ledger stays on the device. |
| Photos or Videos | No | The photo is read on the device and is not uploaded. |
| Other User Content | Yes | Linked to the user. Purpose: App Functionality. Not used for tracking. This is the receipt text or phrase sent to Google Gemini after consent. The same type is in `PrivacyInfo.xcprivacy`. |
| Identifiers, Contact Info, Location, Health, Sensitive Info | No | |

`PrivacyInfo.xcprivacy` is copied into the app bundle. Camera and photo library usage strings are in `Info.plist`. The photo library string covers a photo the user chooses. The app does not write a photo back to the library.

## Encryption

`ITSAppUsesNonExemptEncryption` is false. The iOS app uses HTTPS and the Keychain (`WhenUnlockedThisDeviceOnly`). It does not ship its own cipher. Android AES-GCM runs through Android Keystore in the Android source set, so it is not part of the iOS binary. Standard HTTPS and Apple's Keychain are exempt encryption, which is why the flag is false.

## Store graphics

Icons in the project:

- Android adaptive icon, green `#1F7A4D`, white bars, `androidApp/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`.
- iOS `AppIcon`, 1024×1024, no alpha, `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/`.

Screenshots are captured from a running build and uploaded in the store consoles. They are not stored in git.

| Store | What to capture |
| --- | --- |
| Google Play | At least two phone screenshots, portrait, shortest side at least 320 px and longest side at most 3840 px. Include the dashboard and the receipt review screen. |
| App Store | iPhone 6.7-inch and 6.5-inch screenshots. Same two screens. No status-bar mock that claims a feature the build does not have. |
