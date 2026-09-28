# :core:navigation

Package `com.lebakin.finpulse.core.navigation`. Depends on `navigation3-ui` as `api`.

Public types: `Destination` (`NavKey`), `RootNavigator`, `RootCommand` (`Open`, `Back`), `ComposeRootNavigator`.

Commands queue until `attach` is called and are cleared on `detach`. A feature calls only `RootNavigator`. `NavDisplay` lives in `:shared`.

Check: `scripts/check.sh :core:navigation:jvmTest :core:navigation:compileKotlinIosSimulatorArm64`.
