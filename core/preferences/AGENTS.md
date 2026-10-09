# :core:preferences

Package `com.lebakin.finpulse.core.preferences`. DataStore file in the app directory.

Public types: `ShowcasePreferences`, `openShowcasePreferences`, `preferencesFilePath`. Android calls `bindAndroidPreferencesDirectory` before the first open.

`observeShowcaseMode` stays false until the sample ledger is stored. The first user write and a clear both set it to false.

Check: `scripts/check.sh :core:preferences:jvmTest :core:preferences:compileKotlinIosSimulatorArm64 :core:preferences:detekt`.
