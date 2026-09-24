# Store

The offline build that goes to the store has no account and does not send transactions to a server. Data safety and App Privacy forms must say that plainly, including:

- data that stays on the device;
- the receipt photo, if it does not leave the device;
- the Gemini key, if the user turned on BYOK and agreed to send receipt text to the provider;
- no sale of data.

The BYOK consent screen is shown before any send. Without it the key is not saved and the request is not sent.

`ITSAppUsesNonExemptEncryption` is set from the cryptography the build actually uses, not by copying someone else's plist.

The `release-android` workflow is manual. It does not run on every push. Do not embed signing secrets in YAML. Only reference GitHub Secrets.
