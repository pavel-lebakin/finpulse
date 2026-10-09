# :shared

Package `com.lebakin.finpulse`. Koin root and the application shell. Do not put features here.

Public entry points: `initKoin`, `appModule`, `FinPulseApp`, `HomeDestination`, `prepareAndroidStorage`. iOS: `MainViewController()` and `doInitKoin()`. Framework `Shared`, static.

`appModule` opens the Room database and showcase preferences, and includes `ledgerModule`. `ComposeRootNavigator` and `NoOpErrorReporter` are bound here. `platformModule()` is empty for now. `NavDisplay` registration lives here. Android calls `prepareAndroidStorage` before `initKoin`.

Check: `scripts/check.sh :shared:jvmTest :shared:compileKotlinIosSimulatorArm64`.
