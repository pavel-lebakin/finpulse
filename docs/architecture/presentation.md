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

The Compose Multiplatform navigation library (`org.jetbrains.androidx.navigation:navigation-compose`, type-safe routes) works on Android and on iOS. Version 2.x or Navigation 3 is chosen at Stage 2 based on stability at that time. `RootNavigator` hides that choice from features.

## Errors

`AppError` distinguishes three cases: no connection, a server error, and an unexpected error. The original `cause` is always kept so `ErrorReporter` can take it. `CancellationException` is not swallowed.

While there is no server, the `Server` branch exists on the type, but the ordinary offline flow does not reach it. Do not copy a wider error taxonomy from other projects.
