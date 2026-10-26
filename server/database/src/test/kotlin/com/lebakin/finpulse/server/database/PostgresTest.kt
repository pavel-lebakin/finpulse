package com.lebakin.finpulse.server.database

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PostgresTest {
    @Test
    fun sameServerIsRejected() {
        val primary = endpoint(port = PRIMARY_PORT)
        val replica = endpoint(port = PRIMARY_PORT, database = "Postgres")
        val error = assertFailsWith<IllegalArgumentException> {
            openPostgres(primary, replica)
        }
        assertEquals("Primary and replica must be different servers.", error.message)
    }

    @Test
    fun hostIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openPostgres(endpoint(host = "127.0.0.1/postgres"), endpoint(port = REPLICA_PORT))
        }
        assertEquals("PostgreSQL host is invalid.", error.message)
    }

    @Test
    fun databaseIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openPostgres(endpoint(database = "postgres;drop"), endpoint(port = REPLICA_PORT))
        }
        assertEquals("PostgreSQL database is invalid.", error.message)
    }

    @Test
    fun blankPasswordIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openPostgres(endpoint(password = " "), endpoint(port = REPLICA_PORT))
        }
        assertEquals("PostgreSQL password is empty.", error.message)
    }

    @Test
    fun passwordStaysOutOfToString() {
        val password = "pw" + "a".repeat(PASSWORD_MARK)
        val endpoint = endpoint(password = password)
        assertFalse(endpoint.toString().contains(password))
        assertTrue(endpoint.toString().contains("user=finpulse"))
    }

    private fun endpoint(
        host: String = "127.0.0.1",
        port: Int = PRIMARY_PORT,
        database: String = "postgres",
        password: String = "pw" + "b".repeat(PASSWORD_MARK),
    ) = PostgresEndpoint(
        host = host,
        port = port,
        database = database,
        user = "finpulse",
        password = password,
    )

    private companion object {
        const val PASSWORD_MARK = 16
        const val PRIMARY_PORT = 5432
        const val REPLICA_PORT = 5433
    }
}
