# :shared

Package `com.lebakin.finpulse`. Koin root and the application shell. Do not put features here.

Public entry points: `initKoin`, `appModule`, `FinPulseApp`, `HomeDestination`. iOS: `MainViewController()` and `doInitKoin()`. Framework `Shared`, static.

`ComposeRootNavigator` and `NoOpErrorReporter` are bound in `appModule`. `platformModule()` is empty for now. `NavDisplay` registration lives here.

Check: `scripts/check.sh :shared:jvmTest :shared:compileKotlinIosSimulatorArm64`.
