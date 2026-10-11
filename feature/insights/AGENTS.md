# :feature:insights

Package `com.lebakin.finpulse.feature.insights`. Modules `:feature:insights:api` and `:feature:insights:impl`. Group `com.lebakin.finpulse.feature.insights`.

`api` exposes `InsightsEntry`. It depends on `:core:common`. `openDashboard` replaces the back stack with the dashboard.

`impl` reads transactions and categories through `LedgerReader`. `spendingOf` sums expenses for the current month and the two months before it, in the currency of the newest transaction. The screen draws that total, `AmountBars`, category rows, and recent movements. While showcase mode is on, the dashboard keeps the sample banner and Clear now. An empty ledger shows the hint. Show onboarding again calls `OnboardingEntry`. No network.

Check: `scripts/check.sh :feature:insights:api:jvmTest :feature:insights:impl:jvmTest :feature:insights:api:compileKotlinIosSimulatorArm64 :feature:insights:impl:compileKotlinIosSimulatorArm64 :feature:insights:api:detekt :feature:insights:impl:detekt`.
