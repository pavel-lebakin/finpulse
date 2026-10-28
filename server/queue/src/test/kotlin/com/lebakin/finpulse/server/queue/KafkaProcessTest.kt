package com.lebakin.finpulse.server.queue

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

class KafkaProcessTest {
    @Test
    fun publishThenTake() {
        KafkaProcess.start().use { server ->
            openKafka(server.endpoint).use { kafka ->
                assertNull(kafka.take(TOPIC))
                kafka.publish(TOPIC, VALUE)
                assertEquals(VALUE, kafka.take(TOPIC))
            }
        }
    }

    @Test
    fun blankTopicIsRejected() {
        KafkaProcess.start().use { server ->
            openKafka(server.endpoint).use { kafka ->
                val error = assertFailsWith<IllegalArgumentException> {
                    kafka.publish("", VALUE)
                }
                assertEquals("Kafka topic is invalid.", error.message)
            }
        }
    }

    @Test
    fun longValueIsRejected() {
        KafkaProcess.start().use { server ->
            openKafka(server.endpoint).use { kafka ->
                val error = assertFailsWith<IllegalArgumentException> {
                    kafka.publish(TOPIC, "x".repeat(VALUE_LIMIT))
                }
                assertEquals("Kafka value is too long.", error.message)
            }
        }
    }

    @Test
    fun wrongPasswordIsRejected() {
        KafkaProcess.start().use { server ->
            val password = server.endpoint.password + "x"
            val endpoint = KafkaEndpoint(
                host = server.endpoint.host,
                port = server.endpoint.port,
                user = server.endpoint.user,
                password = password,
            )
            val error = assertFailsWith<IllegalStateException> {
                openKafka(endpoint)
            }
            assertEquals("Kafka authentication failed.", error.message)
            assertFalse(error.toString().contains(password))
            assertFalse(error.toString().contains(server.endpoint.password))
        }
    }

    private companion object {
        const val TOPIC = "marker"
        const val VALUE = "7"
        const val VALUE_LIMIT = 65_537
    }
}
