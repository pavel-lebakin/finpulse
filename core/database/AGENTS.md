# :core:database

Package `com.lebakin.finpulse.core.database`. Room on the bundled SQLite driver. Depends on `:core` only through the app graph; this module has no feature dependency.

Public types: `FinPulseDatabase`, `TransactionEntity`, `CategoryEntity`, `LedgerDao`, `openFinPulseDatabase`, `databaseFilePath`. Android calls `bindAndroidDatabase` before the first open.

Entities stay in this package. A feature imports them only from a `data` package. Schema version 1 is exported under `schemas/`. There is no sync outbox.

Check: `scripts/check.sh :core:database:jvmTest :core:database:compileKotlinIosSimulatorArm64 :core:database:detekt`.
