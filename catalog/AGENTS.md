# :catalog

A token showcase, not a product screen. Package `com.lebakin.finpulse.catalog`.

The Gradle path of the shared code is `:catalog:catalogShared` (directory on disk `catalog/shared`). Shells: `:catalog:android`, `:catalog:desktop`.

Screen: `CatalogApp`. The seed is stored as an index, not as a `Color`.

Check: `scripts/check.sh :catalog:catalogShared:compileKotlinIosSimulatorArm64 :catalog:android:assembleDebug :catalog:desktop:compileKotlin`.
