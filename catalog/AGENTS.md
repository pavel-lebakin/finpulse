# :catalog

A token showcase, not a product screen. Package `com.lebakin.finpulse.catalog`.

The Gradle path of the shared code is `:catalog:catalogShared` (directory on disk `catalog/shared`). Shells: `:catalog:android`, `:catalog:desktop`.

Screen: `CatalogApp`. A menu opened by `MenuButton` switches Tokens, Buttons, Fields, Selection, Navigation, Surfaces, Feedback, and Bars. Tokens keep the palette index, dark switch, radius, spacing, and font scale. The palette is stored as an index, not as a `Color`. The page is a `Surface` in `colorScheme.background`, so light and dark themes change the whole screen. Android system bars follow that background. Navigation shows `BottomNavBar` with a switch for its container color.

Check: `scripts/check.sh :catalog:catalogShared:compileKotlinIosSimulatorArm64 :catalog:android:assembleDebug :catalog:desktop:compileKotlin`.
