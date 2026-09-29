package com.lebakin.finpulse.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.toAppError
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class ScreenViewModel<S : ScreenState, A : ScreenAction> : ViewModel() {
    protected abstract fun createInitialState(): S

    private val states: MutableStateFlow<S> by lazy(LazyThreadSafetyMode.NONE) {
        MutableStateFlow(createInitialState())
    }

    val state: StateFlow<S> by lazy(LazyThreadSafetyMode.NONE) {
        states.asStateFlow()
    }

    protected val currentState: S
        get() = states.value

    abstract fun onAction(action: A)

    protected fun reduce(block: S.() -> S) {
        states.update { current -> current.block() }
    }

    // Unknown failures become AppError. CancellationException is rethrown above.
    @Suppress("TooGenericExceptionCaught")
    protected fun launchCatching(
        onError: (AppError) -> Unit,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = viewModelScope.launch {
        try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            onError(error.toAppError())
        }
    }
}
