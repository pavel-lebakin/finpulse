package com.lebakin.finpulse.server.queue

import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import org.apache.kafka.common.Uuid

internal class KafkaProcess private constructor(
    val endpoint: KafkaEndpoint,
    private val process: Process,
    private val root: Path,
    private val log: Path,
) : AutoCloseable {
    override fun close() {
        if (process.isAlive) {
            process.destroy()
            if (!process.waitFor(STOP_WAIT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                process.waitFor()
            }
        }
        deleteTree(root)
    }

    private fun waitUntilReady() {
        val deadline = System.currentTimeMillis() + WAIT_MILLIS
        while (System.currentTimeMillis() < deadline) {
            if (!process.isAlive) {
                error(failure("Kafka exited."))
            }
            if (log.exists() && log.readText().contains(READY)) {
                return
            }
            Thread.sleep(POLL_MILLIS)
        }
        error(failure("Kafka did not accept connections."))
    }

    private fun failure(reason: String): String {
        val tail = if (log.exists()) {
            log.readText().lineSequence()
                .filterNot { line -> line.contains(endpoint.password) }
                .toList()
                .takeLast(LOG_TAIL)
                .joinToString("\n")
        } else {
            ""
        }
        return "$reason\n$tail"
    }

    companion object {
        fun start(): KafkaProcess {
            val root = Files.createTempDirectory(Path.of("/tmp"), "finpulse-kafka")
            try {
                return launch(root)
            } catch (error: Exception) {
                deleteTree(root)
                throw error
            }
        }

        private fun launch(root: Path): KafkaProcess {
            val binary = kafkaBinary()
            val storage = binary.resolveSibling("kafka-storage")
            val brokerPort = freePort()
            val controllerPort = freePort()
            val password = "pw" + UUID.randomUUID().toString().replace("-", "")
            val endpoint = KafkaEndpoint(
                host = "127.0.0.1",
                port = brokerPort,
                user = "finpulse",
                password = password,
            )
            val logs = root.resolve("logs")
            Files.createDirectories(logs)
            val log = root.resolve("kafka.log")
            val formatLog = root.resolve("format.log")
            val config = root.resolve("server.properties")
            config.writeText(configText(brokerPort, controllerPort, password, logs))
            format(storage, config, formatLog, endpoint, root)
            val process = ProcessBuilder(binary.absolutePathString(), config.absolutePathString())
                .directory(root.toFile())
                .redirectErrorStream(true)
                .redirectOutput(log.toFile())
                .apply { environment()["KAFKA_HEAP_OPTS"] = HEAP }
                .start()
            val kafka = KafkaProcess(endpoint, process, root, log)
            var opened = false
            try {
                kafka.waitUntilReady()
                opened = true
                return kafka
            } finally {
                if (!opened) {
                    kafka.close()
                }
            }
        }

        private fun format(storage: Path, config: Path, log: Path, endpoint: KafkaEndpoint, root: Path) {
            val process = ProcessBuilder(
                storage.absolutePathString(),
                "format",
                "--standalone",
                "-t",
                Uuid.randomUuid().toString(),
                "-c",
                config.absolutePathString(),
            )
                .directory(root.toFile())
                .redirectErrorStream(true)
                .redirectOutput(log.toFile())
                .apply { environment()["KAFKA_HEAP_OPTS"] = HEAP }
                .start()
            if (!process.waitFor(FORMAT_WAIT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                process.waitFor()
                error(formatFailure(endpoint, log, "Kafka storage format did not finish."))
            }
            if (process.exitValue() != 0) {
                error(formatFailure(endpoint, log, "Kafka storage format failed."))
            }
        }

        private fun formatFailure(endpoint: KafkaEndpoint, log: Path, reason: String): String {
            val tail = if (log.exists()) {
                log.readText().lineSequence()
                    .filterNot { line -> line.contains(endpoint.password) }
                    .toList()
                    .takeLast(LOG_TAIL)
                    .joinToString("\n")
            } else {
                ""
            }
            return "$reason\n$tail"
        }

        private fun configText(brokerPort: Int, controllerPort: Int, password: String, logs: Path): String {
            val serverJaas = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                "username=\"finpulse\" password=\"$password\" user_finpulse=\"$password\";"
            val clientJaas = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                "username=\"finpulse\" password=\"$password\";"
            return """
                process.roles=broker,controller
                node.id=1
                controller.quorum.bootstrap.servers=127.0.0.1:$controllerPort
                listeners=SASL_PLAINTEXT://127.0.0.1:$brokerPort,CONTROLLER://127.0.0.1:$controllerPort
                advertised.listeners=SASL_PLAINTEXT://127.0.0.1:$brokerPort
                inter.broker.listener.name=SASL_PLAINTEXT
                controller.listener.names=CONTROLLER
                listener.security.protocol.map=CONTROLLER:PLAINTEXT,SASL_PLAINTEXT:SASL_PLAINTEXT
                sasl.enabled.mechanisms=PLAIN
                sasl.mechanism.inter.broker.protocol=PLAIN
                listener.name.sasl_plaintext.plain.sasl.jaas.config=$serverJaas
                sasl.jaas.config=$clientJaas
                log.dirs=${logs.absolutePathString()}
                num.partitions=1
                offsets.topic.replication.factor=1
                transaction.state.log.replication.factor=1
                transaction.state.log.min.isr=1
                share.coordinator.state.topic.replication.factor=1
                share.coordinator.state.topic.min.isr=1
                auto.create.topics.enable=true
                num.network.threads=1
                num.io.threads=1
                """.trimIndent() + "\n"
        }

        private fun freePort(): Int = ServerSocket(0).use { socket -> socket.localPort }
    }
}

private fun kafkaBinary(): Path {
    val candidates = buildList {
        val override = System.getenv("FINPULSE_KAFKA")
        if (!override.isNullOrBlank()) {
            add(Path.of(override))
        }
        val path = System.getenv("PATH").orEmpty()
        path.split(java.io.File.pathSeparator).forEach { dir ->
            if (dir.isNotEmpty()) {
                add(Path.of(dir, "kafka-server-start"))
            }
        }
        add(Path.of("/opt/homebrew/bin/kafka-server-start"))
        add(Path.of("/opt/homebrew/opt/kafka/bin/kafka-server-start"))
        add(Path.of("/opt/homebrew/opt/kafka/libexec/bin/kafka-server-start"))
        add(Path.of("/usr/local/bin/kafka-server-start"))
        add(Path.of("/usr/bin/kafka-server-start"))
    }
    val binary = candidates.firstOrNull { Files.isExecutable(it) }
        ?: error("Kafka binary is missing. Set FINPULSE_KAFKA to the kafka-server-start executable.")
    return binary.toRealPath()
}

private fun deleteTree(root: Path) {
    if (!root.exists()) {
        return
    }
    Files.walk(root).use { paths ->
        paths.sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) }
    }
}

private const val READY = "Kafka Server started"
private const val WAIT_MILLIS = 30_000L
private const val POLL_MILLIS = 100L
private const val STOP_WAIT_SECONDS = 10L
private const val FORMAT_WAIT_SECONDS = 20L
private const val LOG_TAIL = 40
private const val HEAP = "-Xmx256M -Xms128M"
