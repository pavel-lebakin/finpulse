# Tokens and our components

A token changes in the theme, not at the component call site. A screen reads colors and spacing from the theme. System bars, the display cutout, and the IME are `WindowInsets.safeDrawing` on the frame, not a hardcoded top padding.

`BackArrow` is the back control. `MenuButton` opens the wide menu. `BottomNavBar` is the compact section bar. Do not replace them with a text button. The app frame passes empty window insets into `BottomNavBar` because safe drawing is already applied.

A custom component is worth it when the same combination is already needed by two screens, or when it is a product pattern from the list: amount, transaction row, banner, empty state, chart. A wrapper over one Material button with no new behavior is not needed.

The catalog must show the component states that exist in the API: normal, empty, error, and dark theme, when the component distinguishes them. The token configurator changes the theme live and does not rebuild the app.

Roborazzi screenshots for UIKit component states run in `:core:uikit:jvmTest`. Do not claim the visuals match unless that comparison was run.
