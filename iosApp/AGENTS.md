# iosApp

Xcode project `iosApp/iosApp.xcodeproj`, bundle id `com.lebakin.finpulse`, iOS 16. Signing is off.

Startup: `registerFoundationOnDeviceEngine()`, then `KoinKt.doInitKoin()`, then `MainViewControllerKt.MainViewController()`. On iOS 26 and later that registration passes Apple Foundation Models through `OnDeviceEngine`. An unavailable model stays unready, and the app does not download it. The prompt and the reply are not logged. The framework is looked up in `shared/build/xcode-frameworks`. Camera and photo library usage strings are in `Info.plist`. The photo stays on the device. App Transport Security sets `NSAllowsArbitraryLoads` to false, so cleartext is off. `PrivacyInfo.xcprivacy` is in the app bundle. `ITSAppUsesNonExemptEncryption` is false: this target uses HTTPS and the Keychain, not a custom cipher. The app icon is `AppIcon` in `Assets.xcassets`.

The simulator build command is in `finpulse-workflow/references/build.md`. Do not run it on every commit.
