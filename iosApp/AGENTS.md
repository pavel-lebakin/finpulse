# iosApp

Xcode project `iosApp/iosApp.xcodeproj`, bundle id `com.lebakin.finpulse`, iOS 16. A device build signs when `TEAM_ID` is set in `Config.xcconfig`. That value stays empty in git. A simulator build is ad-hoc signed so the Keychain is available. CI still passes `CODE_SIGNING_ALLOWED=NO`.

Startup: `registerFoundationOnDeviceEngine()`, then `KoinKt.doInitKoin()`, then `MainViewControllerKt.MainViewController()`. On iOS 26 and later that registration passes Apple Foundation Models through `OnDeviceEngine`. An unavailable model stays unready, and the app does not download it. The prompt and the reply are not logged. The framework is looked up in `shared/build/xcode-frameworks`. The Xcode script still runs Gradle when `Shared.framework` for the current SDK is missing, including when the IDE sets `OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED`. Camera and photo library usage strings are in `Info.plist`. The photo stays on the device. App Transport Security sets `NSAllowsArbitraryLoads` to false, so cleartext is off. `PrivacyInfo.xcprivacy` is in the app bundle. `ITSAppUsesNonExemptEncryption` is false: this target uses HTTPS and the Keychain, not a custom cipher. The app icon is `AppIcon` in `Assets.xcassets`.

The simulator build command is in `finpulse-workflow/references/build.md`. Do not run it on every commit.
