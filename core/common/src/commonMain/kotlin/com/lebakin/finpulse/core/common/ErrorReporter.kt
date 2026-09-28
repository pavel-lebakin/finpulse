package com.lebakin.finpulse.core.common

interface ErrorReporter {
    fun report(error: AppError)
}
