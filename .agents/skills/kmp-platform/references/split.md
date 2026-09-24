# Where to draw the boundary

An interface in `commonMain` when a system stands behind it: camera, OCR, Keystore, Keychain, an on-device model, files with a different API.

`expect`/`actual` when the function is short and does not pull platform types into the domain. Domain models do not become `expect`.

`commonMain` must not import the Android SDK, UIKit, or Swift. A test of shared logic runs without an emulator.

An iOS implementation in Kotlin lives in `iosMain`. An implementation in Swift lives in `iosApp` or in a separate Swift target and is registered at startup, before the feature is used for the first time.

Do not close a stage that includes platform code while the module does not build for the simulator.
