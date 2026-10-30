# :feature:assistant

Package `com.lebakin.finpulse.feature.assistant`. Modules `:feature:assistant:api` and `:feature:assistant:impl`. Group `com.lebakin.finpulse.feature.assistant`.

`api` exposes `AssistantEntry`. It depends on `:core:common`. `openAsk` pushes the ask screen.

`impl` reads a phrase. A month-spend question is the intent `MonthSpend` and calls `LedgerReader.observeTransactions`. The answer is the expense total for this month, in the currency of the newest transaction. Any other question gets a fixed reply and does not call a model. A phrase the rules can read becomes a draft and does not open BYOK. Anything else with a digit goes through `phrasePrompt`: a ready on-device model, then BYOK. The reply is accepted only by `checkReceiptReply`. An accepted reply is a draft. Confirm writes one user category and one expense through `LedgerWriter`. Nothing is written from the phrase or the reply alone. A missing consent or key does not call the network. The phrase and the reply are not logged. The app frame opens this screen by replacing the back stack. `openAsk` still pushes it.

Check: `scripts/check.sh :feature:assistant:api:jvmTest :feature:assistant:impl:jvmTest :feature:assistant:api:compileKotlinIosSimulatorArm64 :feature:assistant:impl:compileKotlinIosSimulatorArm64 :feature:assistant:api:detekt :feature:assistant:impl:detekt`.
