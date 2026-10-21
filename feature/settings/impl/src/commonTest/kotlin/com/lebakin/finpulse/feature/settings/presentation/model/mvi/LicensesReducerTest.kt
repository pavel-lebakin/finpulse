package com.lebakin.finpulse.feature.settings.presentation.model.mvi

import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.feature.settings.presentation.viewmodel.licensesOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class LicensesReducerTest {
    @Test
    fun initialIsLoadingWithoutAMessage() {
        val start = LicensesReducer.initial()
        assertIs<Loadable.Loading>(start.content)
        assertNull(start.message)
        assertNull(start.sheet)
    }

    @Test
    fun librariesReplaceLoading() {
        val libraries = licensesOf(SAMPLE_NOTICES)
        val ready = LicensesReducer.run {
            LicensesReducer.initial().withLibraries(libraries)
        }
        val content = assertIs<Loadable.Ready<LicensesContent>>(ready.content)
        assertEquals("Sample", content.value.libraries.libraries.single().name)
    }
}

internal const val SAMPLE_NOTICES = """
    {
      "licenses": {
        "apache": {
          "name": "Apache-2.0",
          "url": "https://www.apache.org/licenses/LICENSE-2.0.txt",
          "spdxId": "Apache-2.0",
          "hash": "apache"
        }
      },
      "libraries": [
        {
          "uniqueId": "com.example:sample",
          "artifactVersion": "1.0.0",
          "name": "Sample",
          "licenses": ["apache"]
        }
      ]
    }
"""
