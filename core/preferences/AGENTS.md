# :core:preferences

Package `com.lebakin.finpulse.core.preferences`. DataStore file in the app directory.

Public types: `ShowcasePreferences`, `OnboardingPreferences`, `ConsentPreferences`, `ThemePreferences`, `ThemeMode`, `AppPreferences`, `openAppPreferences`, `openShowcasePreferences`, `preferencesFilePath`. Android calls `bindAndroidPreferencesDirectory` before the first open. The app opens one file through `openAppPreferences`.

`observeShowcaseMode` stays false until the sample ledger is stored. The first user write and a clear both set it to false. `observeCompleted` stays false until onboarding finishes. Showing onboarding again sets it back to false. `observeGranted` and `isGranted` stay false until the user agrees to send receipt text. Decline sets that flag back to false. The flag is not a Gemini key. `ThemePreferences.observe` starts at `ThemeMode.System`. Light and dark are stored in the same file.

Check: `scripts/check.sh :core:preferences:jvmTest :core:preferences:compileKotlinIosSimulatorArm64 :core:preferences:detekt`.
