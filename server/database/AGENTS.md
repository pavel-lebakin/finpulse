# :database

Separate Gradle build `server/`. Package `com.lebakin.finpulse.server.database`. A library, not a process, and not a module of the mobile project.

`openPostgres(primary, replica)` opens two JDBC connections. `Postgres.write` uses the primary. `Postgres.read` uses the replica. The same host, port, and database is rejected before connecting. The password is a connection property and is omitted from `toString`. The module creates no tables, does not start PostgreSQL, and does not call `:api-gateway`. `finpulse.cloud.enabled` stays false. `org.postgresql:postgresql:42.7.14` is BSD-2-Clause. Licensee 1.14.1 does not map its published URL, so that coordinate is a reviewed exception.

The replica test starts a primary and a streaming replica on `127.0.0.1`. `FINPULSE_POSTGRES` selects the `postgres` binary when it is not on `PATH`.

Check: `scripts/check.sh -p server :database:test :database:detekt :database:licensee`.
