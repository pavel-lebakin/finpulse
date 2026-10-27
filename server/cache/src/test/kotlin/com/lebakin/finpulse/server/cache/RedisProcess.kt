package com.lebakin.finpulse.server.cache

import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

internal class RedisProcess private constructor(
    val endpoint: RedisEndpoint,
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
                error(failure("Redis exited."))
            }
            if (log.exists() && log.readText().contains(READY)) {
                return
            }
            Thread.sleep(POLL_MILLIS)
        }
        error(failure("Redis did not accept connections."))
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
        fun start(): RedisProcess {
            val binary = redisBinary()
            val root = Files.createTempDirectory(Path.of("/tmp"), "finpulse-redis")
            val port = freePort()
            val password = "pw" + UUID.randomUUID().toString().replace("-", "")
            val endpoint = RedisEndpoint(
                host = "127.0.0.1",
                port = port,
                user = "default",
                password = password,
            )
            val log = root.resolve("redis.log")
            val config = root.resolve("redis.conf")
            config.writeText(configText(port, password, root))
            val process = ProcessBuilder(binary.absolutePathString(), config.absolutePathString())
                .redirectErrorStream(true)
                .redirectOutput(log.toFile())
                .start()
            val redis = RedisProcess(endpoint, process, root, log)
            var opened = false
            try {
                redis.waitUntilReady()
                opened = true
                return redis
            } finally {
                if (!opened) {
                    redis.close()
                }
            }
        }

        private fun configText(port: Int, password: String, root: Path): String = """
            bind 127.0.0.1
            port $port
            protected-mode yes
            requirepass $password
            appendonly no
            save ""
            dir ${root.absolutePathString()}
            dbfilename dump.rdb
            daemonize no
            logfile ""
            """.trimIndent() + "\n"

        private fun freePort(): Int = ServerSocket(0).use { socket -> socket.localPort }
    }
}

private fun redisBinary(): Path {
    val candidates = buildList {
        val override = System.getenv("FINPULSE_REDIS")
        if (!override.isNullOrBlank()) {
            add(Path.of(override))
        }
        val path = System.getenv("PATH").orEmpty()
        path.split(java.io.File.pathSeparator).forEach { dir ->
            if (dir.isNotEmpty()) {
                add(Path.of(dir, "redis-server"))
            }
        }
        add(Path.of("/opt/homebrew/bin/redis-server"))
        add(Path.of("/opt/homebrew/opt/redis/bin/redis-server"))
        add(Path.of("/usr/local/bin/redis-server"))
        add(Path.of("/usr/bin/redis-server"))
    }
    return candidates.firstOrNull { Files.isExecutable(it) }
        ?: error("Redis binary is missing. Set FINPULSE_REDIS to the redis-server executable.")
}

private fun deleteTree(root: Path) {
    if (!root.exists()) {
        return
    }
    Files.walk(root).use { paths ->
        paths.sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) }
    }
}

private const val READY = "Ready to accept connections"
private const val WAIT_MILLIS = 5_000L
private const val POLL_MILLIS = 50L
private const val STOP_WAIT_SECONDS = 5L
private const val LOG_TAIL = 20
