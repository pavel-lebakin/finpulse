package com.lebakin.finpulse.feature.settings.presentation.viewmodel

import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.feature.settings.api.KeySave
import com.lebakin.finpulse.feature.settings.api.SettingsReader
import com.lebakin.finpulse.feature.settings.api.SettingsWriter
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.ConsentAction
import com.lebakin.finpulse.feature.settings.presentation.navigation.SettingsNavigator
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ConsentScreenViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun agreeStoresConsentAndGoesBack() {
        val writer = RecordingSettingsWriter()
        val navigator = RecordingSettingsNavigator()
        val model = ConsentScreenViewModel(
            reader = MemorySettingsReader(false),
            writer = writer,
            navigator = navigator,
        )
        val ready = assertIs<Loadable.Ready<Boolean>>(model.state.value.choice)
        assertEquals(false, ready.value)
        model.onAction(ConsentAction.Agree)
        assertEquals(listOf("grant"), writer.calls)
        assertEquals(listOf("back"), navigator.calls)
    }

    @Test
    fun declineClearsConsentAndGoesBack() {
        val writer = RecordingSettingsWriter()
        val navigator = RecordingSettingsNavigator()
        val model = ConsentScreenViewModel(
            reader = MemorySettingsReader(true),
            writer = writer,
            navigator = navigator,
        )
        model.onAction(ConsentAction.Decline)
        assertEquals(listOf("decline"), writer.calls)
        assertEquals(listOf("back"), navigator.calls)
    }
}

private class MemorySettingsReader(initial: Boolean) : SettingsReader {
    private val granted = MutableStateFlow(initial)

    override fun observeConsent(): Flow<Boolean> = granted

    override fun observeKeySaved(): Flow<Boolean> = MutableStateFlow(false)
}

private class RecordingSettingsWriter : SettingsWriter {
    val calls = mutableListOf<String>()

    override suspend fun grantConsent() {
        calls += "grant"
    }

    override suspend fun declineConsent() {
        calls += "decline"
    }

    override suspend fun saveKey(key: String): KeySave = KeySave.Rejected

    override suspend fun deleteKey() = Unit
}

private class RecordingSettingsNavigator : SettingsNavigator {
    val calls = mutableListOf<String>()

    override fun openConsent() {
        calls += "open"
    }

    override fun openKey() {
        calls += "key"
    }

    override fun back() {
        calls += "back"
    }
}
