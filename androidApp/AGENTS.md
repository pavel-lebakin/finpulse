# :androidApp

Package and applicationId `com.lebakin.finpulse`. Depends only on `:shared`.

`FinPulseApplication` calls `prepareAndroidStorage`, then `initKoin()`. `MainActivity` attaches receipt capture, then shows `FinPulseApp`. The camera permission is optional. There is no network permission.

Check: `scripts/check.sh :androidApp:assembleDebug`.
