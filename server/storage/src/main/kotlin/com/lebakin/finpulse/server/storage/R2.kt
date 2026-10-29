package com.lebakin.finpulse.server.storage

import java.net.http.HttpClient
import java.time.Instant

class R2Endpoint(
    val host: String,
    val port: Int,
    val region: String,
    val bucket: String,
    val accessKey: String,
    password: String,
) {
    internal val password: String = password

    override fun toString(): String =
        "R2Endpoint(host=$host, port=$port, region=$region, bucket=$bucket, accessKey=$accessKey)"
}

class R2 internal constructor(
    private val endpoint: R2Endpoint,
    private val client: HttpClient,
) : AutoCloseable {
    fun put(key: String, bytes: ByteArray) {
        validateKey(key)
        validateBytes(bytes)
        val status = call { execute(client, putRequest(endpoint, key, bytes, Instant.now())) }
        accept(status)
    }

    fun sign(key: String): String {
        validateKey(key)
        return presignGet(endpoint, key, Instant.now())
    }

    override fun close() {
        closeClient(client)?.let { throw it }
    }
}

fun openR2(endpoint: R2Endpoint): R2 {
    validate(endpoint)
    val client = openClient()
    val failure = try {
        val status = call { execute(client, headRequest(endpoint, Instant.now())) }
        accept(status)
        null
    } catch (error: IllegalStateException) {
        error
    } catch (error: InterruptedException) {
        Thread.currentThread().interrupt()
        error
    }
    if (failure != null) {
        closeClient(client)?.let(failure::addSuppressed)
        throw failure
    }
    return R2(endpoint, client)
}
