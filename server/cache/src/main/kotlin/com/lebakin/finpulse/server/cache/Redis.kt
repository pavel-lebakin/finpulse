package com.lebakin.finpulse.server.cache

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

private const val MIN_PORT = 1
private const val MAX_PORT = 65535
private const val MAX_HOST_LENGTH = 253
private const val MAX_NAME_LENGTH = 63
private const val MAX_PASSWORD_LENGTH = 1024
private const val MAX_KEY_LENGTH = 256
private const val MAX_VALUE_LENGTH = 65_536
private const val MAX_LINE_LENGTH = 512
private const val SOCKET_TIMEOUT_MILLIS = 5_000
private const val NULL_BULK = -1
private const val SIMPLE = '+'.code
private const val ERROR_MARK = '-'.code
private const val BULK = '$'.code
private const val INTEGER = ':'.code
private const val ARRAY = '*'.code
private const val CR = '\r'.code
private const val LF = '\n'.code
private const val INVALID_REPLY = "Redis reply is invalid."

class RedisEndpoint(
    val host: String,
    val port: Int,
    val user: String,
    password: String,
) {
    internal val password: String = password

    override fun toString(): String = "RedisEndpoint(host=$host, port=$port, user=$user)"
}

class Redis internal constructor(
    private val socket: Socket,
) : AutoCloseable {
    private val input = DataInputStream(BufferedInputStream(socket.getInputStream()))
    private val output = DataOutputStream(BufferedOutputStream(socket.getOutputStream()))
    private val lock = Any()

    fun set(key: String, value: String) {
        validateKey(key)
        validateValue(value)
        val reply = command(listOf("SET", key, value))
        check(reply is SimpleReply && reply.text == "OK") { "Redis command failed." }
    }

    fun get(key: String): String? {
        validateKey(key)
        return when (val reply = command(listOf("GET", key))) {
            is BulkReply -> reply.text
            is NullReply -> null
            else -> error("Redis command failed.")
        }
    }

    internal fun authenticate(user: String, password: String) {
        synchronized(lock) {
            writeCommand(listOf("AUTH", user, password))
            when (val reply = readReply()) {
                is SimpleReply -> check(reply.text == "OK") { "Redis authentication failed." }
                else -> error("Redis authentication failed.")
            }
        }
    }

    override fun close() {
        val quitError = quit()
        val closeError = closeSocket(socket)
        if (quitError != null) {
            if (closeError != null) {
                quitError.addSuppressed(closeError)
            }
            throw quitError
        }
        if (closeError != null) {
            throw closeError
        }
    }

    private fun command(parts: List<String>): Reply = synchronized(lock) {
        writeCommand(parts)
        when (val reply = readReply()) {
            is ErrorReply -> error("Redis command failed.")
            else -> reply
        }
    }

    private fun quit(): Exception? = try {
        if (!socket.isClosed) {
            writeCommand(listOf("QUIT"))
            readReply()
        }
        null
    } catch (error: IOException) {
        error
    } catch (error: IllegalStateException) {
        error
    }

    private fun writeCommand(parts: List<String>) {
        output.writeBytes("*${parts.size}\r\n")
        parts.forEach { part ->
            val bytes = part.toByteArray(Charsets.UTF_8)
            output.writeBytes("$${bytes.size}\r\n")
            output.write(bytes)
            output.writeBytes("\r\n")
        }
        output.flush()
    }

    private fun readReply(): Reply {
        val type = input.read()
        if (type < 0) {
            error(INVALID_REPLY)
        }
        return when (type) {
            SIMPLE -> SimpleReply(readLine())
            ERROR_MARK -> {
                readLine()
                ErrorReply
            }
            BULK -> readBulk()
            INTEGER, ARRAY -> {
                readLine()
                error(INVALID_REPLY)
            }
            else -> error(INVALID_REPLY)
        }
    }

    private fun readBulk(): Reply {
        val length = readLine().toIntOrNull()
        if (length == null || length < NULL_BULK || length > MAX_VALUE_LENGTH) {
            error(INVALID_REPLY)
        }
        if (length < 0) {
            return NullReply
        }
        val bytes = ByteArray(length)
        input.readFully(bytes)
        val carriageReturn = input.read()
        val lineFeed = input.read()
        if (carriageReturn != CR || lineFeed != LF) {
            error(INVALID_REPLY)
        }
        return BulkReply(bytes.toString(Charsets.UTF_8))
    }

    private fun readLine(): String {
        val buffer = ByteArrayOutputStream()
        var done = false
        while (!done && buffer.size() <= MAX_LINE_LENGTH) {
            val byte = input.read()
            if (byte < 0 || byte == LF) {
                error(INVALID_REPLY)
            }
            if (byte == CR) {
                if (input.read() != LF) {
                    error(INVALID_REPLY)
                }
                done = true
            } else {
                buffer.write(byte)
            }
        }
        if (!done) {
            error(INVALID_REPLY)
        }
        return buffer.toString(Charsets.UTF_8)
    }
}

fun openRedis(endpoint: RedisEndpoint): Redis {
    validate(endpoint)
    val socket = connect(endpoint)
    try {
        val session = Redis(socket)
        session.authenticate(endpoint.user, endpoint.password)
        return session
    } catch (error: IOException) {
        closeSocket(socket)?.let(error::addSuppressed)
        throw error
    } catch (error: IllegalStateException) {
        closeSocket(socket)?.let(error::addSuppressed)
        throw error
    }
}

private fun validate(endpoint: RedisEndpoint) {
    require(validHost(endpoint.host)) { "Redis host is invalid." }
    require(endpoint.port in MIN_PORT..MAX_PORT) { "Redis port is invalid." }
    require(validName(endpoint.user)) { "Redis user is invalid." }
    require(endpoint.password.isNotBlank()) { "Redis password is empty." }
    require(endpoint.password.length <= MAX_PASSWORD_LENGTH) { "Redis password is too long." }
    require(endpoint.password.none { it == '\u0000' || it == '\n' || it == '\r' }) {
        "Redis password is not plain text."
    }
}

private fun validateKey(key: String) {
    val lengthOk = key.isNotBlank() && key.length <= MAX_KEY_LENGTH
    val plain = key.none { it == '\u0000' || it == '\n' || it == '\r' }
    require(lengthOk && plain) { "Redis key is invalid." }
}

private fun validateValue(value: String) {
    require(value.length <= MAX_VALUE_LENGTH) { "Redis value is too long." }
}

private fun connect(endpoint: RedisEndpoint): Socket {
    val socket = Socket()
    socket.tcpNoDelay = true
    socket.soTimeout = SOCKET_TIMEOUT_MILLIS
    try {
        socket.connect(InetSocketAddress(endpoint.host, endpoint.port), SOCKET_TIMEOUT_MILLIS)
        return socket
    } catch (error: IOException) {
        closeSocket(socket)?.let(error::addSuppressed)
        throw error
    }
}

private fun closeSocket(socket: Socket): IOException? = try {
    socket.close()
    null
} catch (error: IOException) {
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

private sealed interface Reply

private data class SimpleReply(val text: String) : Reply

private data object ErrorReply : Reply

private data class BulkReply(val text: String) : Reply

private data object NullReply : Reply
