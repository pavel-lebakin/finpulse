# :core:testing

Package `com.lebakin.finpulse.core.testing`. Depends on `:core:navigation`.

Public type: `FakeRootNavigator`. It records `Open`, `Replace`, and `Back`. Do not put other features' test doubles here.

Check: `scripts/check.sh :core:testing:jvmTest :core:testing:compileKotlinIosSimulatorArm64`.
