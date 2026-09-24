---
name: release
description: Use when preparing FinPulse store listing, signing, privacy answers, or the Android release workflow. Not for day-to-day feature work.
---

# Release

This skill is for Stage 5. Do not create a keystore, a publish workflow, or a privacy policy earlier just to have them.

At Stage 5, do this:

- R8 for the release build.
- Signing through gitignored `keystore.properties` and CI secrets.
- A privacy policy in [docs/legal](../../../docs/legal/README.md), published through GitHub Pages.
- Data safety and App Privacy answers in [docs/release](../../../docs/release/README.md).
- `PrivacyInfo.xcprivacy`, camera and photo usage descriptions, `ITSAppUsesNonExemptEncryption`.
- Icons, screenshots, the manual `release-android` workflow, and the final README.

Until that stage, the list above is a reminder, not a task for the current chat. Details: [references/store.md](references/store.md).
