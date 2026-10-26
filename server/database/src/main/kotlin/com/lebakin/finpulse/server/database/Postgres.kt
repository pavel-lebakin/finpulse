package com.lebakin.finpulse.server.database

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.Properties

private const val MIN_PORT = 1
private const val MAX_PORT = 65535
private const val MAX_HOST_LENGTH = 253
private const val MAX_NAME_LENGTH = 63
private const val MAX_PASSWORD_LENGTH = 1024

class PostgresEndpoint(
    val host: String,
    val port: Int,
    val database: String,
    val user: String,
    password: String,
) {
    internal val password: String = password

    override fun toString(): String =
        "PostgresEndpoint(host=$host, port=$port, database=$database, user=$user)"
}

class Postgres internal constructor(
    private val primary: Connection,
    private val replica: Connection,
) : AutoCloseable {
    fun <T> write(block: (Connection) -> T): T = block(primary)

    fun <T> read(block: (Connection) -> T): T = block(replica)

    override fun close() {
        val primaryError = closeConnection(primary)
        val replicaError = closeConnection(replica)
        if (primaryError != null) {
            if (replicaError != null) {
                primaryError.addSuppressed(replicaError)
            }
            throw primaryError
        }
        if (replicaError != null) {
            throw replicaError
        }
    }
}

fun openPostgres(primary: PostgresEndpoint, replica: PostgresEndpoint): Postgres {
    validate(primary)
    validate(replica)
    requireDifferent(primary, replica)
    val primaryConnection = connect(primary)
    try {
        val replicaConnection = connect(replica)
        return Postgres(primaryConnection, replicaConnection)
    } catch (error: SQLException) {
        closeConnection(primaryConnection)?.let(error::addSuppressed)
        throw error
    }
}

private fun validate(endpoint: PostgresEndpoint) {
    require(validHost(endpoint.host)) { "PostgreSQL host is invalid." }
    require(endpoint.port in MIN_PORT..MAX_PORT) { "PostgreSQL port is invalid." }
    require(validName(endpoint.database)) { "PostgreSQL database is invalid." }
    require(validName(endpoint.user)) { "PostgreSQL user is invalid." }
    require(endpoint.password.isNotBlank()) { "PostgreSQL password is empty." }
    require(endpoint.password.length <= MAX_PASSWORD_LENGTH) { "PostgreSQL password is too long." }
    require(endpoint.password.none { it == '\u0000' || it == '\n' || it == '\r' }) {
        "PostgreSQL password is not plain text."
    }
}

private fun requireDifferent(primary: PostgresEndpoint, replica: PostgresEndpoint) {
    val sameHost = primary.host.equals(replica.host, ignoreCase = true)
    val sameDatabase = primary.database.equals(replica.database, ignoreCase = true)
    require(!sameHost || primary.port != replica.port || !sameDatabase) {
        "Primary and replica must be different servers."
    }
}

private fun connect(endpoint: PostgresEndpoint): Connection {
    val url = "jdbc:postgresql://${endpoint.host}:${endpoint.port}/${endpoint.database}"
    val properties = Properties()
    properties.setProperty("user", endpoint.user)
    properties.setProperty("password", endpoint.password)
    return DriverManager.getConnection(url, properties)
}

private fun closeConnection(connection: Connection): SQLException? = try {
    connection.close()
    null
} catch (error: SQLException) {
    error
}

private fun validHost(host: String): Boolean {
    val lengthOk = host.length in 1..MAX_HOST_LENGTH
    val dotted = host.contains("..") || host.startsWith('.') || host.endsWith('.')
    val dashed = host.startsWith('-') || host.endsWith('-')
    return lengthOk && !dotted && !dashed && host.all(::isHostChar)
}

private fun isHostChar(char: Char): Boolean =
    char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' || char == '.' || char == '-'

private fun validName(value: String): Boolean {
    val lengthOk = value.length in 1..MAX_NAME_LENGTH
    val firstOk = value.firstOrNull()?.let { it in 'A'..'Z' || it in 'a'..'z' || it == '_' } ?: false
    val charsOk = value.all { it in 'A'..'Z' || it in 'a'..'z' || it in '0'..'9' || it == '_' }
    return lengthOk && firstOk && charsOk
}
