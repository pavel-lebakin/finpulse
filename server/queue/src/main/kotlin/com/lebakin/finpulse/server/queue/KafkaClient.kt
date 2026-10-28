package com.lebakin.finpulse.server.queue

import java.util.Properties
import java.util.concurrent.TimeUnit
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer

private const val CLIENT_ID = "finpulse"
private const val SECURITY_PROTOCOL = "SASL_PLAINTEXT"
private const val SASL_MECHANISM = "PLAIN"

internal fun authenticate(properties: Properties) {
    val admin = openAdmin(properties)
    val failure = try {
        call {
            val clusterId = admin.describeCluster().clusterId()
            clusterId.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        }
        null
    } catch (error: IllegalStateException) {
        error
    } catch (error: InterruptedException) {
        Thread.currentThread().interrupt()
        error
    }
    val closeError = closeAdmin(admin)
    if (failure != null && closeError != null) {
        failure.addSuppressed(closeError)
    }
    if (failure != null) {
        throw failure
    }
    if (closeError != null) {
        throw closeError
    }
}

private fun openAdmin(properties: Properties): AdminClient = try {
    AdminClient.create(properties)
} catch (error: KafkaException) {
    throw failed(error)
}

internal fun clientProperties(endpoint: KafkaEndpoint): Properties {
    val timeoutMillis = requestTimeout.toMillis().toInt()
    val properties = Properties()
    properties[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = "${endpoint.host}:${endpoint.port}"
    properties[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = SECURITY_PROTOCOL
    properties[CommonClientConfigs.CLIENT_ID_CONFIG] = CLIENT_ID
    properties[CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG] = timeoutMillis
    properties[CommonClientConfigs.DEFAULT_API_TIMEOUT_MS_CONFIG] = timeoutMillis
    properties[SaslConfigs.SASL_MECHANISM] = SASL_MECHANISM
    properties[SaslConfigs.SASL_JAAS_CONFIG] = clientJaas(endpoint.user, endpoint.password)
    properties[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
    properties[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
    properties[ProducerConfig.ACKS_CONFIG] = "all"
    properties[ProducerConfig.LINGER_MS_CONFIG] = 0
    properties[ProducerConfig.MAX_BLOCK_MS_CONFIG] = timeoutMillis
    properties[ProducerConfig.COMPRESSION_TYPE_CONFIG] = "none"
    return properties
}

internal fun consumerProperties(base: Properties): Properties {
    val properties = Properties()
    properties.putAll(base)
    properties[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "false"
    properties[ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG] = "false"
    properties[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
    properties[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
    return properties
}

private fun clientJaas(user: String, password: String): String =
    "org.apache.kafka.common.security.plain.PlainLoginModule required " +
        "username=\"$user\" password=\"$password\";"

internal fun closeProducer(producer: KafkaProducer<String, String>): Exception? = try {
    producer.close()
    null
} catch (error: InterruptedException) {
    Thread.currentThread().interrupt()
    error
} catch (_: KafkaException) {
    IllegalStateException(COMMAND_FAILED)
}

internal fun closeConsumer(consumer: KafkaConsumer<String, String>): Exception? = try {
    consumer.close()
    null
} catch (error: InterruptedException) {
    Thread.currentThread().interrupt()
    error
} catch (_: KafkaException) {
    IllegalStateException(COMMAND_FAILED)
}

private fun closeAdmin(admin: AdminClient): Exception? = try {
    admin.close()
    null
} catch (error: InterruptedException) {
    Thread.currentThread().interrupt()
    error
} catch (_: KafkaException) {
    IllegalStateException(COMMAND_FAILED)
}
