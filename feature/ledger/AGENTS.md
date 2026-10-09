# :feature:ledger

Package `com.lebakin.finpulse.feature.ledger`. Modules `:feature:ledger:api` and `:feature:ledger:impl`. Group `com.lebakin.finpulse.feature.ledger`.

`api` exposes `Transaction`, `Category`, `MoneyDirection`, `RecordOrigin`, `LedgerReader`, and `LedgerWriter`. It depends on `:core:common` and kotlinx.

`impl` stores rows through `:core:database` and the showcase flag through `:core:preferences`. `ShowcaseHistory` builds three months of rows with `origin = SHOWCASE`. `addUserTransaction` deletes every showcase row and inserts the user row in one Room transaction, then turns showcase mode off. `clearShowcase` deletes the sample and does not insert a row. Seeding does nothing when a user row already exists.

No screen and no network. Platform file paths stay in `core`.

Check: `scripts/check.sh :feature:ledger:api:jvmTest :feature:ledger:impl:jvmTest :feature:ledger:api:compileKotlinIosSimulatorArm64 :feature:ledger:impl:compileKotlinIosSimulatorArm64 :feature:ledger:api:detekt :feature:ledger:impl:detekt`.
