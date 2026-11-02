# :core:database

Package `com.lebakin.finpulse.core.database`. Room on the bundled SQLite driver. Depends on `:core` only through the app graph; this module has no feature dependency.

Public types: `FinPulseDatabase`, `TransactionEntity`, `CategoryEntity`, `LedgerDao`, `openFinPulseDatabase`, `databaseFilePath`. Android calls `bindAndroidDatabase` before the first open. `androidDatabaseContext` returns that context for the cloud outbox.

Entities stay in this package. A feature imports them only from a `data` package. Schema version 2 is exported under `schemas/`. Version 2 adds `transactions.linesText`. `Migration1To2` adds that column for a database created at version 1. There is no sync outbox.

Check: `scripts/check.sh :core:database:jvmTest :core:database:compileKotlinIosSimulatorArm64 :core:database:detekt`.
