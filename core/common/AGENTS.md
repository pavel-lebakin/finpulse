# :core:common

Package `com.lebakin.finpulse.core.common`. No dependencies.

Public types: `Loadable` (sealed interface: `Loading`, `Ready`, `Failed`), `AppError`, `NoConnectionException`, `ServerStatusException`, `Throwable.toAppError`, `ErrorReporter`.

`CancellationException` from `toAppError` is rethrown.

Check: `scripts/check.sh :core:common:jvmTest :core:common:compileKotlinIosSimulatorArm64`.
