# UIKit and the showcase

`:core:uikit` is built on Material 3 for Compose Multiplatform. The theme is configured with our tokens:

- light and dark theme colors;
- a spacing scale with a 4 dp step;
- radii computed from one base value;
- typography.

There are few custom components. They cover only repeated product patterns: amount, transaction row, banner, empty state, charts. Features do not hardcode colors and do not assemble a second theme beside this one.

`:catalog` builds separately for Android and for desktop (JVM). It contains every component and its states, plus a live token configurator: seed color, base radius, spacing multiplier, theme, font scale. The catalog depends on `:core:uikit` and does not pull in features.

UIKit screenshots on Roborazzi arrive at Stage 3. Until then, do not imitate a visual check with a text description of how it should look.
