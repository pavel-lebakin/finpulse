package com.lebakin.finpulse.feature.settings.presentation.viewmodel

import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.feature.settings.api.LicenseReader
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.LicensesAction
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.LicensesContent
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.SAMPLE_NOTICES
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class LicensesScreenViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun readyListsTheNoticeName() {
        val model = LicensesScreenViewModel(FixedLicenseReader(SAMPLE_NOTICES))
        val content = assertIs<Loadable.Ready<LicensesContent>>(model.state.value.content)
        assertEquals("Sample", content.value.libraries.libraries.single().name)
        assertEquals("Apache-2.0", content.value.libraries.libraries.single().licenses.single().name)
    }

    @Test
    fun desktopTestArtifactsAreLeftOut() {
        val model = LicensesScreenViewModel(FixedLicenseReader(DESKTOP_NOTICES))
        val content = assertIs<Loadable.Ready<LicensesContent>>(model.state.value.content)
        assertEquals(listOf("Room"), content.value.libraries.libraries.map { it.name })
    }

    @Test
    fun unreadableNoticesFail() {
        val model = LicensesScreenViewModel(FixedLicenseReader("not notices"))
        assertIs<Loadable.Failed>(model.state.value.content)
    }

    @Test
    fun retryReadsAgain() {
        val reader = FixedLicenseReader("not notices")
        val model = LicensesScreenViewModel(reader)
        reader.json = SAMPLE_NOTICES
        model.onAction(LicensesAction.Retry)
        val content = assertIs<Loadable.Ready<LicensesContent>>(model.state.value.content)
        assertEquals("Sample", content.value.libraries.libraries.single().name)
    }
}

private class FixedLicenseReader(
    var json: String,
) : LicenseReader {
    override suspend fun read(): String = json
}

private const val DESKTOP_NOTICES = """
    {
      "licenses": {},
      "libraries": [
        {
          "uniqueId": "org.jetbrains.skiko:skiko-awt",
          "name": "Skiko",
          "licenses": [],
          "targets": ["jvm"]
        },
        {
          "uniqueId": "org.jetbrains.compose.hot-reload:hot-reload-core",
          "name": "Hot reload",
          "licenses": [],
          "targets": []
        },
        {
          "uniqueId": "androidx.room:room-runtime",
          "name": "Room",
          "licenses": [],
          "targets": ["android", "jvm", "metadata"]
        }
      ]
    }
"""
