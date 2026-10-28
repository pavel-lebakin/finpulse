package com.lebakin.finpulse.server.queue

import java.time.Duration
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.TopicPartition

internal const val REQUEST_TIMEOUT_SECONDS = 5L
internal const val COMMAND_FAILED = "Kafka command failed."

internal val requestTimeout: Duration = Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS)

private const val AUTH_FAILED = "Kafka authentication failed."
private val pollTimeout: Duration = Duration.ofMillis(500)

class KafkaEndpoint(
    val host: String,
    val port: Int,
    val user: String,
    password: String,
) {
    internal val password: String = password

    override fun toString(): String = "KafkaEndpoint(host=$host, port=$port, user=$user)"
}

class Kafka internal constructor(
    private val producer: KafkaProducer<String, String>,
    private val consumer: KafkaConsumer<String, String>,
) : AutoCloseable {
    private val lock = Any()

    fun publish(topic: String, value: String) {
        validateTopic(topic)
        validateValue(value)
        synchronized(lock) {
            call {
                producer.send(ProducerRecord(topic, value)).get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            }
        }
    }

    fun take(topic: String): String? {
        validateTopic(topic)
        return synchronized(lock) { readTopic(topic) }
    }

    override fun close() {
        val producerError = closeProducer(producer)
        val consumerError = closeConsumer(consumer)
        if (producerError != null) {
            if (consumerError != null) {
                producerError.addSuppressed(consumerError)
            }
            throw producerError
        }
        if (consumerError != null) {
            throw consumerError
        }
    }

    private fun readTopic(topic: String): String? {
        val partitions = call { consumer.partitionsFor(topic, requestTimeout) }
        val topicPartition = partitions?.firstOrNull()?.let { TopicPartition(topic, it.partition()) }
        if (topicPartition == null) {
            return null
        }
        consumer.assign(listOf(topicPartition))
        val end = call { consumer.endOffsets(listOf(topicPartition), requestTimeout) }[topicPartition] ?: 0L
        return if (end <= 0L) {
            null
        } else {
            consumer.seek(topicPartition, end - 1)
            readLatest(topicPartition)
        }
    }

    private fun readLatest(topicPartition: TopicPartition): String? {
        val deadline = System.currentTimeMillis() + requestTimeout.toMillis()
        while (System.currentTimeMillis() < deadline) {
            val records = call { consumer.poll(pollTimeout) }.records(topicPartition)
            if (records.isNotEmpty()) {
                return records.last().value()
            }
        }
        return null
    }
}

fun openKafka(endpoint: KafkaEndpoint): Kafka {
    validate(endpoint)
    val properties = clientProperties(endpoint)
    authenticate(properties)
    val producer = KafkaProducer<String, String>(properties)
    try {
        val consumer = KafkaConsumer<String, String>(consumerProperties(properties))
        return Kafka(producer, consumer)
    } catch (error: KafkaException) {
        closeProducer(producer)?.let(error::addSuppressed)
        throw failed(error)
    }
}

internal fun <T> call(block: () -> T): T {
    val result = try {
        Result.success(block())
    } catch (error: InterruptedException) {
        Thread.currentThread().interrupt()
        Result.failure(error)
    } catch (error: TimeoutException) {
        Result.failure(failed(error))
    } catch (error: ExecutionException) {
        Result.failure(failed(error.cause ?: error))
    } catch (error: KafkaException) {
        Result.failure(failed(error))
    }
    return result.getOrThrow()
}

internal fun failed(error: Throwable): IllegalStateException {
    val message = if (isAuth(error)) AUTH_FAILED else COMMAND_FAILED
    return IllegalStateException(message)
}

private fun isAuth(error: Throwable): Boolean {
    val pending = ArrayDeque<Throwable>()
    val seen = HashSet<Throwable>()
    pending.add(error)
    while (pending.isNotEmpty()) {
        val current = pending.removeFirst()
        if (!seen.add(current)) {
            continue
        }
        if (current.javaClass.name.contains("AuthenticationException")) {
            return true
        }
        current.cause?.let(pending::add)
        current.suppressed.forEach(pending::add)
    }
    return false
}
