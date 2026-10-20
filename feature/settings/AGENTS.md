# :feature:settings

Package `com.lebakin.finpulse.feature.settings`. Modules `:feature:settings:api` and `:feature:settings:impl`. Group `com.lebakin.finpulse.feature.settings`.

`api` exposes `SettingsEntry`, `SettingsReader`, `SettingsWriter`, and `KeySave`. It depends on `:core:common` and kotlinx. `openConsent` pushes the consent screen. `openKey` pushes the key screen.

`impl` explains that receipt text would leave the device for Google Gemini. Agree stores that decision. Decline clears it. Both then leave the screen. The consent screen does not collect a Gemini key and does not call the network. The key screen shows a field only after consent. Save stores one printable ASCII key of at most 512 characters in `SecretStore`. The key is not a State field and is not logged. A saved key is shown only as saved, with Delete. Delete clears the secret. A missing consent or a rejected key does not call `save`. The screen does not call the network. The dashboard opens consent through `SettingsEntry.openConsent` and the key screen through `SettingsEntry.openKey`.

Check: `scripts/check.sh :feature:settings:api:jvmTest :feature:settings:impl:jvmTest :feature:settings:api:compileKotlinIosSimulatorArm64 :feature:settings:impl:compileKotlinIosSimulatorArm64 :feature:settings:api:detekt :feature:settings:impl:detekt`.
