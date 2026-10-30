# :shared

Package `com.lebakin.finpulse`. Koin root and the application shell. Do not put features here.

Public entry points: `initKoin`, `appModule`, `FinPulseApp`, `prepareAndroidStorage`, `attachReceiptCapture`. iOS: `MainViewController()` and `doInitKoin()`. Framework `Shared`, static. It exports `:core:ai` so Swift can implement `OnDeviceEngine`.

`appModule` opens the Room database, one preferences file, the secret store, and `HttpsClient`, and includes `ledgerModule`, `onboardingModule`, `insightsModule`, `scannerModule`, `assistantModule`, and `settingsModule`. `finpulse.cloud.enabled` selects `cloudMain` or `offlineMain`. The default is false, so `offlineMain` is compiled and `accountModule` is not linked. `cloudMain` includes `accountModule`. `appModule` binds `LicenseReader` to the AboutLibraries export of this module. The plugin writes `aboutlibraries.json` from the runtime classpath, offline, and Compose resources pack that file. The licenses screen stays in `:feature:settings`. `ComposeRootNavigator` is also bound as `RootNavigator`. `NoOpErrorReporter` is bound here. `platformModule()` is empty on Android and JVM. On iOS it binds `OnDeviceEngine` when Swift registered one before `initKoin`. `NavDisplay` starts at `OnboardingDestination.Launch` and registers the onboarding, insights, scanner, assistant, and settings graphs. Android calls `prepareAndroidStorage` before `initKoin`. That binds the database, preferences, and the secret directory under `noBackupFilesDir`. `attachReceiptCapture` runs from `MainActivity` before the first frame.

Check: `scripts/check.sh :shared:jvmTest :shared:compileKotlinIosSimulatorArm64`.
