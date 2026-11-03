# Parse chain

The scanner takes an image from the camera or the gallery. OCR: ML Kit on Android, Vision on iOS. iOS uses accurate recognition, turns language correction off, detects the language, and joins words that share a row so a price stays on its item. Android opens the ML Kit document scanner for a full cropped page, falls back to a full-size photo, honors EXIF rotation, scales a short image up, and groups ML Kit lines into rows. It uses the Latin model. The prompt asks for English, Russian, Portuguese, Spanish, French, German, Italian, Polish, and Turkish, and for item lines. The on-device model is asked whenever the prompt accepts the text. The rule draft stays when it lists at least as many items. A model reply with more items replaces it. BYOK runs only when both are missing. The reply may include `lines`. An invalid reply does not become a transaction: the capture screen says the receipt could not be read. A printed currency wins. Otherwise the language hints RUB, EUR, GBP, or USD. Review can switch among those four codes. Confirm writes one expense for the total and stores the item lines on it. A later scan replaces the draft already on the review screen.

BYOK does not turn on silently. First the consent screen, then the key field, then storage in Keystore or Keychain. The key is not in State and not in logs.

The assistant creates transaction drafts and waits for confirmation. A question about spending this month is the `MonthSpend` intent in our code, in English or Russian, and calls the read-only ledger total. The answer stays English. Any other question gets a fixed reply. A new intent is added in our code, not described in the prompt.

The prompt does not receive an instruction to execute commands from the receipt text. Receipt text is passed as data.

Prompt API availability is checked on a device at Stage 4. If the engine is not registered, the interface does not promise an on-device model.
