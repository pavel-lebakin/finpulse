# :androidApp

Package and applicationId `com.lebakin.finpulse`. Depends only on `:shared`.

`FinPulseApplication` calls `prepareAndroidStorage`, then `initKoin()`. `MainActivity` attaches receipt capture, then shows `FinPulseApp`. The camera permission is optional. `INTERNET` is declared. Cleartext is off in every build through `usesCleartextTraffic` and `network_security_config`. There is no debug exception.

The release build type runs R8 and shrinks resources. Signing reads gitignored `keystore.properties` when that file exists. Without it the release bundle is unsigned. The launcher is the adaptive icon `mipmap/ic_launcher`.

Check: `scripts/check.sh :androidApp:assembleDebug`. The release bundle is `:androidApp:bundleRelease`.
