# :feature:onboarding

Package `com.lebakin.finpulse.feature.onboarding`. Modules `:feature:onboarding:api` and `:feature:onboarding:impl`. Group `com.lebakin.finpulse.feature.onboarding`.

`api` exposes `OnboardingChoice`, `OnboardingReader`, `OnboardingWriter`, and `OnboardingEntry`. It depends on `:core:common` and kotlinx.

`impl` draws one flow of four promises. `complete(Sample)` seeds the showcase ledger. `complete(Empty)` clears showcase rows. Both then open the dashboard through `InsightsEntry`. `OnboardingEntry.showAgain` clears the completed flag and opens the four promises again. The sample banner lives on the insights dashboard. No network.

Check: `scripts/check.sh :feature:onboarding:api:jvmTest :feature:onboarding:impl:jvmTest :feature:onboarding:api:compileKotlinIosSimulatorArm64 :feature:onboarding:impl:compileKotlinIosSimulatorArm64 :feature:onboarding:api:detekt :feature:onboarding:impl:detekt`.
