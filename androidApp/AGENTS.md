# :androidApp

Package and applicationId `com.lebakin.finpulse`. Depends only on `:shared`.

`FinPulseApplication` calls `prepareAndroidStorage`, then `initKoin()`. `MainActivity` attaches receipt capture, then shows `FinPulseApp`. The camera permission is optional. `INTERNET` is declared. Cleartext is off in every build through `usesCleartextTraffic` and `network_security_config`. There is no debug exception.

Check: `scripts/check.sh :androidApp:assembleDebug`.
