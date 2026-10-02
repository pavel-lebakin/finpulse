# :core:uikit

Package `com.lebakin.finpulse.core.uikit`. Compose Material 3. Features take colors and spacing from here, not from their own files.

Public types: `FinPulseTheme`, `FinPulseTokens`, `FinPulseSpacing`, `FinPulseRadii`, `MoneyAmount`, `TransactionRow`, `NoticeBanner`, `EmptyState`, `AmountBars`.

Starting values: seed `0xFF1F7A4D`, spacing step `4.dp`, base radius `12.dp`.

Check: `scripts/check.sh :core:uikit:jvmTest :core:uikit:compileKotlinIosSimulatorArm64`.

Component screenshots live in `src/jvmTest/snapshots/components`. `jvmTest` compares them. Record after a visual change with `scripts/check.sh :core:uikit:jvmTest -Proborazzi.record=true`.
