# :feature:settings

Package `com.lebakin.finpulse.feature.settings`. Modules `:feature:settings:api` and `:feature:settings:impl`. Group `com.lebakin.finpulse.feature.settings`.

`api` exposes `SettingsEntry`, `SettingsReader`, and `SettingsWriter`. It depends on `:core:common` and kotlinx. `openConsent` pushes the consent screen.

`impl` explains that receipt text would leave the device for Google Gemini. Agree stores that decision. Decline clears it. Both then leave the screen. The screen does not collect a Gemini key and does not call the network. The dashboard opens it through `SettingsEntry.openConsent`.

Check: `scripts/check.sh :feature:settings:api:jvmTest :feature:settings:impl:jvmTest :feature:settings:api:compileKotlinIosSimulatorArm64 :feature:settings:impl:compileKotlinIosSimulatorArm64 :feature:settings:api:detekt :feature:settings:impl:detekt`.
