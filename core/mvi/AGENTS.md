# :core:mvi

Package `com.lebakin.finpulse.core.mvi`. Depends on `:core:common`.

Public types: `ScreenState`, `ScreenAction`, `ScreenViewModel`. State changes only through `reduce { }`. `launchCatching` maps an error to `AppError` and does not swallow `CancellationException`.

The reducer is an `object` with extension functions. Call from the ViewModel: `reduce { ScreenReducer.run { withItems(items) } }`.

Check: `scripts/check.sh :core:mvi:jvmTest :core:mvi:compileKotlinIosSimulatorArm64`.
