# :feature:onboarding

Package `com.lebakin.finpulse.feature.onboarding`. Modules `:feature:onboarding:api` and `:feature:onboarding:impl`. Group `com.lebakin.finpulse.feature.onboarding`.

`api` exposes `OnboardingChoice`, `OnboardingReader`, `OnboardingWriter`, and `OnboardingEntry`. It depends on `:core:common` and kotlinx.

`impl` draws one flow of four promises, then a home screen. `complete(Sample)` seeds the showcase ledger. `complete(Empty)` clears showcase rows and leaves an empty hint. Home shows the sample banner and Clear now while showcase mode is on. `OnboardingEntry.showAgain` clears the completed flag and opens the four promises again. Settings does not exist yet, so Home is the caller. No network.

Check: `scripts/check.sh :feature:onboarding:api:jvmTest :feature:onboarding:impl:jvmTest :feature:onboarding:api:compileKotlinIosSimulatorArm64 :feature:onboarding:impl:compileKotlinIosSimulatorArm64 :feature:onboarding:api:detekt :feature:onboarding:impl:detekt`.
