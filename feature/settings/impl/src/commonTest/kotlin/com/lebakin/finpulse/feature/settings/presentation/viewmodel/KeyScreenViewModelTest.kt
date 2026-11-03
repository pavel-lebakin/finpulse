package com.lebakin.finpulse.feature.settings.presentation.viewmodel

import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.feature.settings.api.KeySave
import com.lebakin.finpulse.feature.settings.api.SettingsReader
import com.lebakin.finpulse.feature.settings.api.SettingsWriter
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.KeyAction
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.KeyContent
import com.lebakin.finpulse.feature.settings.presentation.navigation.SettingsNavigator
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class KeyScreenViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveKeepsTheKeyOutOfState() {
        val writer = RecordingKeyWriter(KeySave.Saved)
        val model = model(writer = writer, granted = true)
        val secret = "token-ok"
        model.onAction(KeyAction.Save(secret))
        assertEquals(listOf(secret), writer.saved)
        assertEquals("Gemini key saved.", model.state.value.message?.text)
        assertFalse(model.state.value.toString().contains(secret))
        val content = assertIs<Loadable.Ready<KeyContent>>(model.state.value.content)
        assertEquals(false, content.value.saved)
    }

    @Test
    fun rejectedKeyIsNotDescribedWithItsValue() {
        val writer = RecordingKeyWriter(KeySave.Rejected)
        val model = model(writer = writer, granted = true)
        val secret = "token ok"
        model.onAction(KeyAction.Save(secret))
        assertEquals("That key cannot be saved.", model.state.value.message?.text)
        assertFalse(model.state.value.toString().contains(secret))
    }

    @Test
    fun missingConsentDoesNotClaimTheKeyWasSaved() {
        val writer = RecordingKeyWriter(KeySave.NeedsConsent)
        val model = model(writer = writer, granted = false)
        model.onAction(KeyAction.Save("token-ok"))
        assertEquals("Agree before a Gemini key can be saved.", model.state.value.message?.text)
    }

    @Test
    fun deleteClearsTheKey() {
        val writer = RecordingKeyWriter(KeySave.Saved)
        val model = model(writer = writer, saved = true, granted = true)
        model.onAction(KeyAction.Delete)
        assertEquals(1, writer.deletes)
        assertEquals("Gemini key deleted.", model.state.value.message?.text)
    }

    @Test
    fun openConsentLeavesTheKeyScreen() {
        val navigator = RecordingKeyNavigator()
        val model = model(navigator = navigator)
        model.onAction(KeyAction.OpenConsent)
        assertEquals(listOf("consent"), navigator.calls)
    }

    private fun model(
        writer: RecordingKeyWriter = RecordingKeyWriter(KeySave.Rejected),
        navigator: RecordingKeyNavigator = RecordingKeyNavigator(),
        granted: Boolean = false,
        saved: Boolean = false,
    ): KeyScreenViewModel = KeyScreenViewModel(
        reader = MemoryKeyReader(granted = granted, saved = saved),
        writer = writer,
        navigator = navigator,
    )
}

private class MemoryKeyReader(
    granted: Boolean,
    saved: Boolean,
) : SettingsReader {
    private val granted = MutableStateFlow(granted)
    private val saved = MutableStateFlow(saved)

    override fun observeConsent(): Flow<Boolean> = granted

    override fun observeKeySaved(): Flow<Boolean> = saved
}

private class RecordingKeyWriter(
    private val result: KeySave,
) : SettingsWriter {
    val saved = mutableListOf<String>()
    var deletes: Int = 0

    override suspend fun grantConsent() = Unit

    override suspend fun declineConsent() = Unit

    override suspend fun saveKey(key: String): KeySave {
        saved += key
        return result
    }

    override suspend fun deleteKey() {
        deletes += 1
    }
}

private class RecordingKeyNavigator : SettingsNavigator {
    val calls = mutableListOf<String>()

    override fun openConsent() {
        calls += "consent"
    }

    override fun openKey() {
        calls += "key"
    }

    override fun back() {
        calls += "back"
    }
}
