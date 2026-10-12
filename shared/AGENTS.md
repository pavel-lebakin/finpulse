# :shared

Package `com.lebakin.finpulse`. Koin root and the application shell. Do not put features here.

Public entry points: `initKoin`, `appModule`, `FinPulseApp`, `prepareAndroidStorage`, `attachReceiptCapture`. iOS: `MainViewController()` and `doInitKoin()`. Framework `Shared`, static.

`appModule` opens the Room database and one preferences file, and includes `ledgerModule`, `onboardingModule`, `insightsModule`, and `scannerModule`. `ComposeRootNavigator` is also bound as `RootNavigator`. `NoOpErrorReporter` is bound here. `platformModule()` is empty for now. `NavDisplay` starts at `OnboardingDestination.Launch` and registers the onboarding, insights, and scanner graphs. Android calls `prepareAndroidStorage` before `initKoin`, and `attachReceiptCapture` from `MainActivity` before the first frame.

Check: `scripts/check.sh :shared:jvmTest :shared:compileKotlinIosSimulatorArm64`.
