package com.lebakin.finpulse.server.database

import java.sql.Connection
import java.sql.SQLException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PostgresReplicaTest {
    @Test
    fun readFollowsThePrimaryWrite() {
        PostgresReplica.start().use { servers ->
            openPostgres(servers.primary, servers.replica).use { session ->
                session.write { connection ->
                    connection.createStatement().use { statement ->
                        statement.execute("CREATE TABLE marker(id int)")
                        statement.execute("INSERT INTO marker VALUES ($MARKER_ID)")
                    }
                }
                assertFalse(session.write(::inRecovery))
                assertTrue(session.read(::inRecovery))
                val deadline = System.currentTimeMillis() + WAIT_MILLIS
                var seen: Int? = null
                while (System.currentTimeMillis() < deadline) {
                    seen = readMarker(session)
                    if (seen == MARKER_ID) {
                        break
                    }
                    Thread.sleep(POLL_MILLIS)
                }
                assertEquals(MARKER_ID, seen)
            }
        }
    }

    private fun inRecovery(connection: Connection): Boolean =
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT pg_is_in_recovery()").use { rows ->
                check(rows.next()) { "PostgreSQL returned no recovery row." }
                rows.getBoolean(1)
            }
        }

    private fun readMarker(session: Postgres): Int? = try {
        session.read { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT id FROM marker").use { rows ->
                    if (rows.next()) rows.getInt(1) else null
                }
            }
        }
    } catch (error: SQLException) {
        if (error.sqlState == UNDEFINED_TABLE) {
            null
        } else {
            throw error
        }
    }

    private companion object {
        const val MARKER_ID = 7
        const val WAIT_MILLIS = 5_000L
        const val POLL_MILLIS = 100L
        const val UNDEFINED_TABLE = "42P01"
    }
}
