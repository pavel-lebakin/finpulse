# :feature:scanner

Package `com.lebakin.finpulse.feature.scanner`. Modules `:feature:scanner:api` and `:feature:scanner:impl`. Group `com.lebakin.finpulse.feature.scanner`.

`api` exposes `ScannerEntry`. It depends on `:core:common`. `openCapture` pushes the capture screen.

`impl` reads a receipt from the camera, the gallery, or a bundled sample. Samples skip recognition and go straight to the rule-based parser. The same text always produces the same draft. The review screen shows that draft. `Confirm` writes one user category and one expense through `LedgerWriter`. Nothing is written from recognition text alone. iOS reads image bytes with Vision. Android reads Latin text with ML Kit `text-recognition` 16.0.1. That artifact and the Play Services libraries it pulls are a reviewed Licensee exception. The image stays in memory for the read. No network.

Check: `scripts/check.sh :feature:scanner:api:jvmTest :feature:scanner:impl:jvmTest :feature:scanner:api:compileKotlinIosSimulatorArm64 :feature:scanner:impl:compileKotlinIosSimulatorArm64 :feature:scanner:api:detekt :feature:scanner:impl:detekt`.
