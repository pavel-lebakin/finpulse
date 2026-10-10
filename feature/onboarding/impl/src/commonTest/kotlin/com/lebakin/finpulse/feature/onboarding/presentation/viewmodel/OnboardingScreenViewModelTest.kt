package com.lebakin.finpulse.feature.onboarding.presentation.viewmodel

import com.lebakin.finpulse.feature.onboarding.api.OnboardingChoice
import com.lebakin.finpulse.feature.onboarding.api.OnboardingWriter
import com.lebakin.finpulse.feature.onboarding.presentation.model.mvi.OnboardingAction
import com.lebakin.finpulse.feature.onboarding.presentation.navigation.OnboardingNavigator
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingScreenViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sampleChoiceIsIgnoredUntilTheLastPage() {
        val writer = FakeOnboardingWriter()
        val navigator = FakeOnboardingNavigator()
        val model = OnboardingScreenViewModel(writer, navigator)
        model.onAction(OnboardingAction.SeeSample)
        assertEquals(emptyList(), writer.choices)
        assertEquals(emptyList(), navigator.calls)
    }

    @Test
    fun lastPageSampleOpensHome() {
        val writer = FakeOnboardingWriter()
        val navigator = FakeOnboardingNavigator()
        val model = OnboardingScreenViewModel(writer, navigator)
        repeat(3) {
            model.onAction(OnboardingAction.Next)
        }
        model.onAction(OnboardingAction.SeeSample)
        assertEquals(listOf(OnboardingChoice.Sample), writer.choices)
        assertEquals(listOf("home"), navigator.calls)
    }

    @Test
    fun lastPageEmptyOpensHome() {
        val writer = FakeOnboardingWriter()
        val navigator = FakeOnboardingNavigator()
        val model = OnboardingScreenViewModel(writer, navigator)
        repeat(3) {
            model.onAction(OnboardingAction.Next)
        }
        model.onAction(OnboardingAction.StartEmpty)
        assertEquals(listOf(OnboardingChoice.Empty), writer.choices)
        assertEquals(listOf("home"), navigator.calls)
    }
}

private class FakeOnboardingWriter : OnboardingWriter {
    val choices = mutableListOf<OnboardingChoice>()

    override suspend fun complete(choice: OnboardingChoice) {
        choices += choice
    }

    override suspend fun showAgain() = Unit
}

private class FakeOnboardingNavigator : OnboardingNavigator {
    val calls = mutableListOf<String>()

    override fun openFlow() {
        calls += "flow"
    }

    override fun openHome() {
        calls += "home"
    }
}
