package com.lebakin.finpulse.server.queue

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KafkaTest {
    @Test
    fun hostIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openKafka(endpoint(host = "127.0.0.1/kafka"))
        }
        assertEquals("Kafka host is invalid.", error.message)
    }

    @Test
    fun userIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openKafka(endpoint(user = "finpulse;drop"))
        }
        assertEquals("Kafka user is invalid.", error.message)
    }

    @Test
    fun portIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openKafka(endpoint(port = 0))
        }
        assertEquals("Kafka port is invalid.", error.message)
    }

    @Test
    fun blankPasswordIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openKafka(endpoint(password = " "))
        }
        assertEquals("Kafka password is empty.", error.message)
    }

    @Test
    fun quotedPasswordIsRejected() {
        val error = assertFailsWith<IllegalArgumentException> {
            openKafka(endpoint(password = "pw\"secret"))
        }
        assertEquals("Kafka password is not plain text.", error.message)
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
        port: Int = KAFKA_PORT,
        user: String = "finpulse",
        password: String = "pw" + "b".repeat(PASSWORD_MARK),
    ) = KafkaEndpoint(
        host = host,
        port = port,
        user = user,
        password = password,
    )

    private companion object {
        const val PASSWORD_MARK = 16
        const val KAFKA_PORT = 9092
    }
}
