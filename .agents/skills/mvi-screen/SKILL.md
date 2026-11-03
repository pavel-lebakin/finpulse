---
name: mvi-screen
description: Use when adding or changing a FinPulse screen, its State, Action, Reducer, ViewModel, or Navigator.
---

# Screen

One screen is the triple `<Screen>State`, `<Screen>Action`, `<Screen>Reducer`, plus the class `ScreenViewModel`.

The reducer is an `object` with `initial(...)` and `withX()` extensions. The ViewModel calls them only inside `reduce {}`. The snackbar (`UserMessage`) and the sheet (`<Screen>Sheet`) are State fields. There is no separate effect. A saved confirmation uses `MessageSnackbar` from UIKit: a Material 3 snackbar on the bottom edge, short duration, no action button. When it disappears, the screen sends the dismiss Action.

`Loadable` is a sealed interface: `Loading`, `Ready`, `Failed`.

Navigation from the ViewModel goes through `<Feature>Navigator`. A Composable has no `NavController`.

The app frame in `:shared` applies `WindowInsets.safeDrawing` once. A screen fills that area and pads with spacing tokens. Do not add a second `windowInsetsPadding`, and do not draw under the status bar, the display cutout, or the system navigation bar. A screen that is not inside that frame, such as the catalog, applies `WindowInsets.safeDrawing` itself.

Back from a pushed screen is `BackArrow` at the top of the frame. Do not put a "Back" text button at the bottom. A step back inside one screen, such as an onboarding page, is also `BackArrow` at the top start and sends an Action. It does not pop the graph.

Settings live on the settings screen. Another feature does not list consent, a key, or other preferences.

Primary sections are Home, Scan, Assistant, and Settings. A compact width shows them in `BottomNavBar`. Those roots are a `HorizontalPager`, so a tap or a swipe slides toward the selected index. A wide width shows them in a menu opened by `MenuButton` and still uses that pager. Choosing a section replaces the back stack with that section's root after the pager settles. A detail screen slides over the pager, and the frame shows `BackArrow` instead of the bar or the menu.

Onboarding uses `NavDisplay`. That slide keeps both screens opaque. Do not add fade, veil, or scale on that slide.

An onboarding page fills the frame. The picture and the text are centered. The action button is pinned to the bottom.

`<Screen>Host` wires Koin and collects State. `<Screen>Screen` draws a ready State and sends Action outward.

Base-class fields and a reducer example: [docs/conventions.md](../../../docs/conventions.md). How errors and the navigation queue work: [docs/architecture/presentation.md](../../../docs/architecture/presentation.md).

Step by step: [references/screen.md](references/screen.md).
