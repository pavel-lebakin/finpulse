# :core:uikit

Package `com.lebakin.finpulse.core.uikit`. Compose Material 3. Features take colors and spacing from here, not from their own files.

Public types: `FinPulseTheme`, `FinPulseTokens`, `FinPulseSpacing`, `FinPulseRadii`, `FinPulsePalette`, `FinPulsePalettes`, `finPulseColorScheme`, `MoneyAmount`, `TransactionRow`, `NoticeBanner`, `EmptyState`, `AmountBars`, `FinPulseIcons`, `BackArrow`, `MenuButton`, `BottomNavBar`, `BottomNavItem`.

`BottomNavBar` is the compact section bar. Container, selected, unselected, and indicator colors are parameters so the catalog can change them. The app frame passes empty window insets because it already applied safe drawing.

`FinPulseTheme` keeps the product scheme unless `palette` is true. The catalog turns that on so the page background, surface, and accent colors follow the selected palette in light and dark.

Starting values: seed `0xFF1F7A4D`, spacing step `4.dp`, base radius `12.dp`.

Check: `scripts/check.sh :core:uikit:jvmTest :core:uikit:compileKotlinIosSimulatorArm64`.

Component screenshots live in `src/jvmTest/snapshots/components`. `jvmTest` compares them. Record after a visual change with `scripts/check.sh :core:uikit:jvmTest -Proborazzi.record=true`.
