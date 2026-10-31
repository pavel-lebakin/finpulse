# UIKit and the showcase

`:core:uikit` is built on Material 3 for Compose Multiplatform. The theme is configured with our tokens:

- light and dark theme colors;
- a spacing scale with a 4 dp step;
- radii computed from one base value;
- typography.

There are few custom components. They cover only repeated product patterns: amount, transaction row, banner, empty state, charts. `BackArrow`, `MenuButton`, and `BottomNavBar` are the shared chrome controls. `BottomNavBar` takes container, selected, unselected, and indicator colors. Features do not hardcode colors and do not assemble a second theme beside this one.

## Screen frame

`FinPulseApp` wraps `NavDisplay` in one frame. The frame applies `WindowInsets.safeDrawing` once, so content stays clear of the status bar, the display cutout, the system navigation bar, and the IME. A screen inside the frame does not apply that inset again. The catalog is not inside the frame, so it applies `WindowInsets.safeDrawing` itself.

On a compact width the frame shows Home, Scan, Assistant, and Settings in `BottomNavBar`. Those roots are pages of a `HorizontalPager`. A tap or a swipe slides left or right toward the selected index. At 600 dp and wider the same sections open from `MenuButton`, and the pager still slides. Choosing a section replaces the back stack with that section's root after the pager settles. A pushed screen slides in over the pager, shows `BackArrow` at the top, and hides the bar and the menu. Settings rows, including System, Light, and Dark, live on the settings screen. The frame paints `colorScheme.background`, so the stored theme changes the whole app.

`NavDisplay` is used for onboarding. It moves the outgoing screen fully out and the incoming screen fully in. Both stay opaque. The motion is 250 ms. Fade, veil, and scale are not added to that slide, because a transparent screen that is also moving looks stuck.

An onboarding page fills the frame. Its picture and text are centered. Its button is pinned to the bottom. Page back is `BackArrow` and does not pop the graph.

`:catalog` builds separately for Android and for desktop (JVM). A menu switches separate screens: tokens, buttons, fields, selection, navigation, surfaces, feedback, and bars. Tokens keep the live configurator: palette, base radius, spacing multiplier, theme, font scale. The other screens show the Material 3 controls used in the app, including a customizable `BottomNavBar`. The page background is the palette background, in light and in dark. The catalog depends on `:core:uikit` and does not pull in features.

UIKit component states are captured by Roborazzi in `:core:uikit` `jvmTest`. The images live next to that test source. Do not describe a layout as matching the design unless those images were recorded or compared.
