package com.lebakin.finpulse.server.database

import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.appendText
import kotlin.io.path.readText
import kotlin.io.path.writeText

internal class PostgresReplica private constructor(
    val primary: PostgresEndpoint,
    val replica: PostgresEndpoint,
    private val tools: PostgresTools,
    private val root: Path,
    private val primaryData: Path,
    private val replicaData: Path,
) : AutoCloseable {
    override fun close() {
        stop(replicaData)
        stop(primaryData)
        deleteTree(root)
    }

    private fun boot(password: String) {
        initPrimary(password)
        startServer(primaryData, root.resolve("primary.log"))
        copyReplica(password)
        retargetReplicaPort()
        startServer(replicaData, root.resolve("replica.log"))
    }

    private fun initPrimary(password: String) {
        val passwordFile = root.resolve("password")
        Files.writeString(passwordFile, password)
        try {
            run(
                tools,
                listOf(
                    tools.initdb.absolutePathString(),
                    "-D",
                    primaryData.absolutePathString(),
                    "--username=finpulse",
                    "--pwfile=${passwordFile.absolutePathString()}",
                    "--auth=scram-sha-256",
                    "--encoding=UTF8",
                    "--locale=C",
                ),
            )
        } finally {
            passwordFile.deleteExisting()
        }
        primaryData.resolve("postgresql.conf").appendText(listenConfig(primary.port, root))
    }

    private fun copyReplica(password: String) {
        run(
            tools,
            listOf(
                tools.pgBasebackup.absolutePathString(),
                "-h",
                "127.0.0.1",
                "-p",
                primary.port.toString(),
                "-U",
                "finpulse",
                "-D",
                replicaData.absolutePathString(),
                "-R",
                "-X",
                "stream",
                "-c",
                "fast",
            ),
            password = password,
        )
    }

    private fun retargetReplicaPort() {
        val replicaConfig = replicaData.resolve("postgresql.conf")
        val updated = replicaConfig.readText().replace("port = ${primary.port}", "port = ${replica.port}")
        check(updated != replicaConfig.readText()) { "Replica port was not changed." }
        replicaConfig.writeText(updated)
    }

    private fun startServer(data: Path, log: Path) {
        run(
            tools,
            listOf(
                tools.pgCtl.absolutePathString(),
                "-D",
                data.absolutePathString(),
                "-l",
                log.absolutePathString(),
                "-w",
                "start",
            ),
        )
    }

    private fun stop(data: Path) {
        if (!data.isDirectory()) {
            return
        }
        val process = ProcessBuilder(
            listOf(
                tools.pgCtl.absolutePathString(),
                "-D",
                data.absolutePathString(),
                "-m",
                "fast",
                "-w",
                "stop",
            ),
        ).apply {
            environment()["DYLD_LIBRARY_PATH"] = tools.lib.absolutePathString()
            environment()["LD_LIBRARY_PATH"] = tools.lib.absolutePathString()
        }
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()
        process.waitFor()
    }

    companion object {
        fun start(): PostgresReplica {
            val tools = PostgresTools.find()
            val root = Files.createTempDirectory(Path.of("/tmp"), "finpulse-pg")
            val ports = freePorts()
            val password = "pw" + java.util.UUID.randomUUID().toString().replace("-", "")
            val replica = PostgresReplica(
                primary = endpoint(ports.first, password),
                replica = endpoint(ports.second, password),
                tools = tools,
                root = root,
                primaryData = root.resolve("primary"),
                replicaData = root.resolve("replica"),
            )
            var opened = false
            try {
                replica.boot(password)
                opened = true
                return replica
            } finally {
                if (!opened) {
                    replica.close()
                }
            }
        }

        private fun endpoint(port: Int, password: String) = PostgresEndpoint(
            host = "127.0.0.1",
            port = port,
            database = "postgres",
            user = "finpulse",
            password = password,
        )

        private fun freePorts(): Pair<Int, Int> = ServerSocket(0).use { first ->
            ServerSocket(0).use { second ->
                first.localPort to second.localPort
            }
        }
    }
}

private class PostgresTools(
    val initdb: Path,
    val pgCtl: Path,
    val pgBasebackup: Path,
    val lib: Path,
) {
    companion object {
        fun find(): PostgresTools {
            val binary = postgresBinary()
            val bin = binary.parent
            val tools = PostgresTools(
                initdb = bin.resolve("initdb"),
                pgCtl = bin.resolve("pg_ctl"),
                pgBasebackup = bin.resolve("pg_basebackup"),
                lib = bin.parent.resolve("lib"),
            )
            check(tools.initdb.exists() && tools.pgCtl.exists() && tools.pgBasebackup.exists()) {
                "PostgreSQL tools are missing next to $binary."
            }
            return tools
        }
    }
}

private fun postgresBinary(): Path {
    val candidates = buildList {
        val override = System.getenv("FINPULSE_POSTGRES")
        if (!override.isNullOrBlank()) {
            add(Path.of(override))
        }
        val path = System.getenv("PATH").orEmpty()
        path.split(java.io.File.pathSeparator).forEach { dir ->
            if (dir.isNotEmpty()) {
                add(Path.of(dir, "postgres"))
            }
        }
        add(Path.of("/Library/PostgreSQL/17/bin/postgres"))
        add(Path.of("/opt/homebrew/opt/postgresql@17/bin/postgres"))
        add(Path.of("/usr/lib/postgresql/17/bin/postgres"))
    }
    return candidates.firstOrNull { Files.isExecutable(it) }
        ?: error("PostgreSQL binary is missing. Set FINPULSE_POSTGRES to the postgres executable.")
}

private fun listenConfig(port: Int, socketDirectory: Path): String = """

listen_addresses = '127.0.0.1'
port = $port
unix_socket_directories = '${socketDirectory.absolutePathString()}'
wal_level = replica
max_wal_senders = 10
max_replication_slots = 10
hot_standby = on
shared_buffers = 16MB
fsync = off
"""

private fun run(tools: PostgresTools, command: List<String>, password: String? = null) {
    val process = ProcessBuilder(command)
        .apply {
            environment()["DYLD_LIBRARY_PATH"] = tools.lib.absolutePathString()
            environment()["LD_LIBRARY_PATH"] = tools.lib.absolutePathString()
            environment()["LC_ALL"] = "C"
            if (password != null) {
                environment()["PGPASSWORD"] = password
            }
        }
        .redirectErrorStream(true)
        .start()
    val output = process.inputStream.bufferedReader().readText()
    val code = process.waitFor()
    if (code != 0) {
        val safe = output.lineSequence()
            .filterNot { line -> line.contains("password", ignoreCase = true) }
            .toList()
            .takeLast(LOG_TAIL)
            .joinToString("\n")
        error("PostgreSQL ${Path.of(command.first()).fileName} failed.\n$safe")
    }
}

private fun deleteTree(root: Path) {
    if (!root.exists()) {
        return
    }
    Files.walk(root).use { paths ->
        paths.sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) }
    }
}

private const val LOG_TAIL = 20
