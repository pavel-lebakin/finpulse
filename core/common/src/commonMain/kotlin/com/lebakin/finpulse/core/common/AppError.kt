package com.lebakin.finpulse.core.common

import kotlin.coroutines.cancellation.CancellationException

sealed interface AppError {
    val cause: Throwable

    data class NoConnection(override val cause: Throwable) : AppError

    data class Server(val statusCode: Int, override val cause: Throwable) : AppError

    data class Unexpected(override val cause: Throwable) : AppError
}

class NoConnectionException(cause: Throwable? = null) : Exception("No connection", cause)

class ServerStatusException(
    val statusCode: Int,
    cause: Throwable? = null,
) : Exception("Server status $statusCode", cause)

fun Throwable.toAppError(): AppError = when (this) {
    is CancellationException -> throw this
    is NoConnectionException -> AppError.NoConnection(this)
    is ServerStatusException -> AppError.Server(statusCode, this)
    else -> AppError.Unexpected(this)
}
