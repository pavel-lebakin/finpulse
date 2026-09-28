package com.lebakin.finpulse.core.common

sealed interface Loadable<out T> {
    data object Loading : Loadable<Nothing>

    data class Ready<T>(val value: T) : Loadable<T>

    data class Failed(val error: AppError) : Loadable<Nothing>
}
