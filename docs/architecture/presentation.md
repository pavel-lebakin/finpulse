# MVI, navigation, and errors

## MVI without effects

The snackbar, the sheet, and the error are State fields. The user dismisses them through an Action. There is no separate effect channel.

All State changes are extension functions in `object <Screen>Reducer`. The ViewModel calls them only inside `reduce {}`. From Stage 3 a Konsist test checks this.

The base-class contract and a reducer example are in the [glossary](../conventions.md).

`Loadable<T>` is a sealed interface with the variants `Loading`, `Ready(value)`, and `Failed(error)`. It is not a value class: a value class is exported to Swift as `Any`.

## Navigation

Each feature has `<Feature>Navigator` and `<Feature>NavigatorImpl`. The Navigator is injected into the ViewModel. A Composable knows nothing about `NavController`.

`:core:navigation` contains `Destination`, `RootNavigator`, and `ComposeRootNavigator`. `ComposeRootNavigator` queues commands while `NavController` is not attached yet.

Feature routes are an `@Serializable` sealed type `<Feature>Destination`. `<Feature>Graph` registers them in the NavHost. `<Screen>Host` receives the ViewModel and state. `<Screen>Screen` is a pure function `(state, onAction)`.

Stage 2 chose Navigation 3: `org.jetbrains.androidx.navigation3:navigation3-ui` version `1.2.0-rc01`, published by Compose Multiplatform 1.12.2. Navigation 2 in the same release is also a release candidate (`2.10.0-rc01`). Kotlin Multiplatform documentation describes Navigation 3 as the path that works on Android and on iOS when route keys are serialized explicitly.

`Destination` inside `:core:navigation` extends the Navigation 3 key. A feature does not choose that: the ViewModel calls `RootNavigator`. `ComposeRootNavigator` holds a command queue until the Compose host is attached. Route registration in `NavDisplay` lives in `:shared`.

## Errors

`AppError` distinguishes three cases: no connection, a server error, and an unexpected error. The original `cause` is always kept so `ErrorReporter` can take it. `CancellationException` is not swallowed.

While there is no server, the `Server` branch exists on the type, but the ordinary offline flow does not reach it. Do not copy a wider error taxonomy from other projects.
