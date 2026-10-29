package com.lebakin.finpulse.server.storage

import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

internal class MinioProcess private constructor(
    val endpoint: R2Endpoint,
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
                error(failure("MinIO exited."))
            }
            if (log.exists() && log.readText().contains(READY)) {
                return
            }
            Thread.sleep(POLL_MILLIS)
        }
        error(failure("MinIO did not accept connections."))
    }

    private fun waitUntilBucket() {
        val deadline = System.currentTimeMillis() + WAIT_MILLIS
        var last = "R2 command failed."
        while (System.currentTimeMillis() < deadline) {
            if (!process.isAlive) {
                error(failure("MinIO exited."))
            }
            val opened = try {
                ensureBucket(endpoint)
                true
            } catch (error: IllegalStateException) {
                last = error.message ?: last
                false
            }
            if (opened) {
                return
            }
            Thread.sleep(POLL_MILLIS)
        }
        error(failure("MinIO did not accept the bucket. $last"))
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
        fun start(): MinioProcess {
            val root = Files.createTempDirectory(Path.of("/tmp"), "finpulse-minio")
            try {
                return launch(root)
            } catch (error: Exception) {
                deleteTree(root)
                throw error
            }
        }

        private fun launch(root: Path): MinioProcess {
            val binary = minioBinary()
            val port = freePort()
            val password = "pw" + UUID.randomUUID().toString().replace("-", "")
            val endpoint = R2Endpoint(
                host = "127.0.0.1",
                port = port,
                region = "us-east-1",
                bucket = "finpulse",
                accessKey = "finpulse",
                password = password,
            )
            val data = root.resolve("data")
            Files.createDirectories(data)
            val log = root.resolve("minio.log")
            val envFile = root.resolve("minio.env")
            envFile.writeText(envText(password))
            val process = ProcessBuilder(
                binary.absolutePathString(),
                "server",
                "--address",
                "127.0.0.1:$port",
                data.absolutePathString(),
            )
                .directory(root.toFile())
                .redirectErrorStream(true)
                .redirectOutput(log.toFile())
                .apply { environment()["MINIO_CONFIG_ENV_FILE"] = envFile.absolutePathString() }
                .start()
            val minio = MinioProcess(endpoint, process, root, log)
            var opened = false
            try {
                minio.waitUntilReady()
                minio.waitUntilBucket()
                opened = true
                return minio
            } finally {
                if (!opened) {
                    minio.close()
                }
            }
        }

        private fun envText(password: String): String = """
            MINIO_ROOT_USER=finpulse
            MINIO_ROOT_PASSWORD=$password
            MINIO_BROWSER=off
            """.trimIndent() + "\n"

        private fun freePort(): Int = ServerSocket(0).use { socket -> socket.localPort }
    }
}

private fun minioBinary(): Path {
    val candidates = buildList {
        val override = System.getenv("FINPULSE_MINIO")
        if (!override.isNullOrBlank()) {
            add(Path.of(override))
        }
        val path = System.getenv("PATH").orEmpty()
        path.split(java.io.File.pathSeparator).forEach { dir ->
            if (dir.isNotEmpty()) {
                add(Path.of(dir, "minio"))
            }
        }
        add(Path.of("/opt/homebrew/bin/minio"))
        add(Path.of("/usr/local/bin/minio"))
        add(Path.of("/usr/bin/minio"))
    }
    val binary = candidates.firstOrNull { Files.isExecutable(it) }
        ?: error("MinIO binary is missing. Set FINPULSE_MINIO to the minio executable.")
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

private const val READY = "API:"
private const val WAIT_MILLIS = 20_000L
private const val POLL_MILLIS = 100L
private const val STOP_WAIT_SECONDS = 5L
private const val LOG_TAIL = 40
