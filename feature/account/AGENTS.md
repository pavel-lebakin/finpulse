# :feature:account

Package `com.lebakin.finpulse.feature.account`. Modules `:feature:account:api` and `:feature:account:impl`. Group `com.lebakin.finpulse.feature.account`.

`api` exposes `LedgerSync`, `SyncChange`, `SyncKind`, `SyncOrigin`, `SyncMerge`, and `SyncConflict`. It depends on `:core:common` and kotlinx.

`impl` stores the outbox in its own Room database, `finpulse-outbox.db`, next to the ledger file. `enqueue` refuses `SHOWCASE` and does not insert it. `merge` keeps the higher category revision. A transaction that differs stays local and is returned as a conflict. Android opens that database with the context from `bindAndroidDatabase`. No screen and no network. `:shared` links `accountModule` only from `cloudMain`, when `finpulse.cloud.enabled=true`. The default build does not link it.

Check: `scripts/check.sh :feature:account:api:jvmTest :feature:account:impl:jvmTest :feature:account:api:compileKotlinIosSimulatorArm64 :feature:account:impl:compileKotlinIosSimulatorArm64 :feature:account:api:detekt :feature:account:impl:detekt`.
