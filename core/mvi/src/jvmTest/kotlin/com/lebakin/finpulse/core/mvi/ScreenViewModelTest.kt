package com.lebakin.finpulse.core.mvi

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.common.NoConnectionException
import com.lebakin.finpulse.core.common.ServerStatusException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ScreenViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun reduceAppliesTheReducer() {
        val model = TestScreenViewModel()
        model.onAction(TestAction.Succeed)
        assertEquals(Loadable.Ready("ok"), model.state.value.items)
    }

    @Test
    fun failureBecomesAppErrorInsideReduce() {
        val model = TestScreenViewModel()
        model.onAction(TestAction.Fail)
        val failed = assertIs<Loadable.Failed>(model.state.value.items)
        assertIs<AppError.NoConnection>(failed.error)
    }

    @Test
    fun serverStatusBecomesServerError() {
        val model = TestScreenViewModel()
        model.onAction(TestAction.Server)
        val failed = assertIs<Loadable.Failed>(model.state.value.items)
        val error = assertIs<AppError.Server>(failed.error)
        assertEquals(503, error.statusCode)
    }

    @Test
    fun cancellationDoesNotCallOnError() {
        val model = TestScreenViewModel()
        model.onAction(TestAction.Cancel)
        assertNull(model.reported)
        assertEquals(Loadable.Loading, model.state.value.items)
    }
}

private data class TestState(
    val items: Loadable<String>,
) : ScreenState

private sealed interface TestAction : ScreenAction {
    data object Succeed : TestAction

    data object Fail : TestAction

    data object Server : TestAction

    data object Cancel : TestAction
}

private object TestReducer {
    fun initial(): TestState = TestState(items = Loadable.Loading)

    fun TestState.withReady(value: String): TestState = copy(items = Loadable.Ready(value))

    fun TestState.withError(error: AppError): TestState = copy(items = Loadable.Failed(error))
}

private class TestScreenViewModel : ScreenViewModel<TestState, TestAction>() {
    var reported: AppError? = null

    override fun createInitialState(): TestState = TestReducer.initial()

    override fun onAction(action: TestAction) {
        when (action) {
            TestAction.Succeed -> launchCatching(onError = { reported = it }) {
                reduce { TestReducer.run { withReady("ok") } }
            }
            TestAction.Fail -> failWith(NoConnectionException())
            TestAction.Server -> failWith(ServerStatusException(statusCode = 503))
            TestAction.Cancel -> launchCatching(onError = { reported = it }) {
                throw CancellationException("stop")
            }
        }
    }

    private fun failWith(cause: Throwable) {
        launchCatching(onError = { error ->
            reported = error
            reduce { TestReducer.run { withError(error) } }
        }) {
            throw cause
        }
    }
}
