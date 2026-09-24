---
name: mvi-screen
description: Use when adding or changing a FinPulse screen, its State, Action, Reducer, ViewModel, or Navigator.
---

# Screen

One screen is the triple `<Screen>State`, `<Screen>Action`, `<Screen>Reducer`, plus the class `ScreenViewModel`.

The reducer is an `object` with `initial(...)` and `withX()` extensions. The ViewModel calls them only inside `reduce {}`. The snackbar (`UserMessage`) and the sheet (`<Screen>Sheet`) are State fields. There is no separate effect.

`Loadable` is a sealed interface: `Loading`, `Ready`, `Failed`.

Navigation from the ViewModel goes through `<Feature>Navigator`. A Composable has no `NavController`.

`<Screen>Host` wires Koin and collects State. `<Screen>Screen` draws a ready State and sends Action outward.

Base-class fields and a reducer example: [docs/conventions.md](../../../docs/conventions.md). How errors and the navigation queue work: [docs/architecture/presentation.md](../../../docs/architecture/presentation.md).

Step by step: [references/screen.md](references/screen.md).
