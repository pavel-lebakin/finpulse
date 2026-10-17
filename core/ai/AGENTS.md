# :core:ai

Package `com.lebakin.finpulse.core.ai`. Depends on kotlinx datetime, kotlinx serialization, and, on Android, ML Kit `genai-prompt` 1.0.0-beta5. No feature dependency.

Public types: `ReceiptReply`, `ReceiptCategory`, `ReceiptCheck`, `receiptPrompt`, `checkReceiptReply`, `OnDeviceModel`, `OnDeviceFailed`, `openOnDeviceModel`, `OnDeviceEngine`, `registerOnDeviceEngine`.

`receiptPrompt` places receipt text between `RECEIPT_DATA_BEGIN` and `RECEIPT_DATA_END`. Those markers inside the text are broken so the block has one end. The instructions do not tell the model to follow commands from the receipt. `checkReceiptReply` accepts one JSON object with `merchant`, `amountMinor`, `currencyCode`, `occurredOn`, and `categoryName`. `amountMinor` is a positive integer of at most nine digits. A repeated key is rejected. Anything else is `ReceiptCheck.Rejected`. An accepted value is a `ReceiptReply`, not a transaction.

`openOnDeviceModel` returns an `OnDeviceModel` only when that platform has a ready engine. Android returns one when ML Kit reports `FeatureStatus.AVAILABLE` for Gemini Nano. A downloadable or missing engine returns null, and this step does not start a download. iOS returns one only after Swift calls `registerOnDeviceEngine` and `OnDeviceEngine.ready` is true. Apple Foundation Models stay in Swift. A missing registration, an older system, or an unavailable Apple Intelligence model returns null, and this step does not start a download. The JVM returns null. `complete` returns the model text. An empty reply or a model failure is `OnDeviceFailed` and is not a transaction. `CancellationException` is rethrown. The prompt and the reply are not logged. `genai-prompt` 1.0.0-beta5, `genai-common` 1.0.0-beta5, `genai-schema` 1.0.0-alpha2, `play-services-basement` 18.9.0, and `reactive-streams` 1.0.3 are reviewed Licensee exceptions. No feature calls this module yet. The model is not bound in Koin. `registeredOnDeviceEngine` is hidden from Swift and is what the iOS platform module binds.

Check: `scripts/check.sh :core:ai:jvmTest :core:ai:compileKotlinIosSimulatorArm64 :core:ai:detekt :core:ai:licensee`.
