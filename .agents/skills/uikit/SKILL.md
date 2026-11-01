---
name: uikit
description: Use when changing FinPulse colors, spacing, type, Material 3 theme, shared components, or the catalog app.
---

# UIKit

Components and the theme live in `:core:uikit`. The base is Material 3 Compose Multiplatform, configured with project tokens: light and dark colors, spacing on a 4 dp step, radii from one base value, and typography.

A feature does not set a color as a number and does not copy the theme into itself. If a pattern repeats (amount, transaction row, banner, empty state, chart), the component is added to UIKit. `MessageSnackbar` is the short Material confirmation at the bottom of a screen. `BackArrow`, `MenuButton`, and `BottomNavBar` live here because the frame and the catalog use the same controls. `BottomNavBar` exposes container, selected, unselected, and indicator colors. One-off screen layout does not move into UIKit.

The app frame applies `WindowInsets.safeDrawing`. A component does not add that padding again.

`:catalog` splits controls across screens opened from a menu: tokens, buttons, fields, selection, navigation, surfaces, feedback, and bars. Tokens still choose a palette and turn the base radius, spacing multiplier, theme, and font scale. The catalog page background follows that palette in light and dark. The catalog builds for Android and desktop. It does not depend on features.

Details: [docs/architecture/ui.md](../../../docs/architecture/ui.md). What counts as our component: [references/tokens.md](references/tokens.md).
