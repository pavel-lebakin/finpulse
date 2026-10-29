package com.lebakin.finpulse.server.storage

import java.io.IOException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant

internal const val HTTP_OK = 200
internal const val HTTP_NO_CONTENT = 204
internal const val HTTP_UNAUTHORIZED = 401
internal const val HTTP_FORBIDDEN = 403
internal const val HTTP_CONFLICT = 409

private const val AUTH_FAILED = "R2 authentication failed."
private const val COMMAND_FAILED = "R2 command failed."

internal val requestTimeout: Duration = Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS)

private const val REQUEST_TIMEOUT_SECONDS = 5L

internal fun openClient(): HttpClient = HttpClient.newBuilder()
    .followRedirects(HttpClient.Redirect.NEVER)
    .connectTimeout(requestTimeout)
    .build()

internal fun execute(client: HttpClient, call: SignedCall): Int {
    val publisher = if (call.method == "HEAD") {
        HttpRequest.BodyPublishers.noBody()
    } else {
        HttpRequest.BodyPublishers.ofByteArray(call.body)
    }
    val builder = HttpRequest.newBuilder(call.uri).timeout(requestTimeout).method(call.method, publisher)
    call.headers.forEach { header -> builder.header(header.name, header.value) }
    val response = client.send(builder.build(), HttpResponse.BodyHandlers.discarding())
    return response.statusCode()
}

internal fun accept(status: Int) {
    when (status) {
        HTTP_OK, HTTP_NO_CONTENT -> Unit
        HTTP_UNAUTHORIZED, HTTP_FORBIDDEN -> error(AUTH_FAILED)
        else -> error(COMMAND_FAILED)
    }
}

internal fun ensureBucket(endpoint: R2Endpoint) {
    validate(endpoint)
    val client = openClient()
    val failure = try {
        val status = call { execute(client, bucketRequest(endpoint, Instant.now())) }
        if (status != HTTP_OK && status != HTTP_CONFLICT) {
            accept(status)
        }
        null
    } catch (error: IllegalStateException) {
        error
    } catch (error: InterruptedException) {
        Thread.currentThread().interrupt()
        error
    }
    val closeError = closeClient(client)
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

internal fun closeClient(client: HttpClient): Exception? = try {
    client.close()
    null
} catch (error: InterruptedException) {
    Thread.currentThread().interrupt()
    error
} catch (_: IOException) {
    IllegalStateException(COMMAND_FAILED)
}

internal fun <T> call(block: () -> T): T {
    val result = try {
        Result.success(block())
    } catch (error: InterruptedException) {
        Thread.currentThread().interrupt()
        Result.failure(error)
    } catch (_: IOException) {
        Result.failure(IllegalStateException(COMMAND_FAILED))
    }
    return result.getOrThrow()
}
