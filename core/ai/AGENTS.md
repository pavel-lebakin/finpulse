# :core:ai

Package `com.lebakin.finpulse.core.ai`. Depends on kotlinx datetime and kotlinx serialization. No feature dependency.

Public types: `ReceiptReply`, `ReceiptCategory`, `ReceiptCheck`, `receiptPrompt`, `checkReceiptReply`.

`receiptPrompt` places receipt text between `RECEIPT_DATA_BEGIN` and `RECEIPT_DATA_END`. Those markers inside the text are broken so the block has one end. The instructions do not tell the model to follow commands from the receipt. `checkReceiptReply` accepts one JSON object with `merchant`, `amountMinor`, `currencyCode`, `occurredOn`, and `categoryName`. `amountMinor` is a positive integer of at most nine digits. A repeated key is rejected. Anything else is `ReceiptCheck.Rejected`. An accepted value is a `ReceiptReply`, not a transaction. No feature calls this module yet.

Check: `scripts/check.sh :core:ai:jvmTest :core:ai:compileKotlinIosSimulatorArm64 :core:ai:detekt :core:ai:licensee`.
