# :androidApp

Package and applicationId `com.lebakin.finpulse`. Depends only on `:shared`.

`FinPulseApplication` calls `prepareAndroidStorage`, then `initKoin()`. `MainActivity` shows `FinPulseApp`. There is no network permission.

Check: `scripts/check.sh :androidApp:assembleDebug`.
