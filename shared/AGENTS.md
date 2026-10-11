# :shared

Package `com.lebakin.finpulse`. Koin root and the application shell. Do not put features here.

Public entry points: `initKoin`, `appModule`, `FinPulseApp`, `prepareAndroidStorage`. iOS: `MainViewController()` and `doInitKoin()`. Framework `Shared`, static.

`appModule` opens the Room database and one preferences file, and includes `ledgerModule`, `onboardingModule`, and `insightsModule`. `ComposeRootNavigator` is also bound as `RootNavigator`. `NoOpErrorReporter` is bound here. `platformModule()` is empty for now. `NavDisplay` starts at `OnboardingDestination.Launch` and registers `onboardingGraph` and `insightsGraph`. Android calls `prepareAndroidStorage` before `initKoin`.

Check: `scripts/check.sh :shared:jvmTest :shared:compileKotlinIosSimulatorArm64`.
