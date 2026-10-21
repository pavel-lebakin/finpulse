package com.lebakin.finpulse.feature.settings.presentation.viewmodel

import com.lebakin.finpulse.core.mvi.ScreenViewModel
import com.lebakin.finpulse.feature.settings.api.LicenseReader
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.LicensesAction
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.LicensesReducer
import com.lebakin.finpulse.feature.settings.presentation.model.mvi.LicensesState
import com.mikepenz.aboutlibraries.Libs
import kotlinx.coroutines.yield

internal class LicensesScreenViewModel(
    private val reader: LicenseReader,
) : ScreenViewModel<LicensesState, LicensesAction>() {
    init {
        load()
    }

    override fun createInitialState(): LicensesState = LicensesReducer.initial()

    override fun onAction(action: LicensesAction) {
        when (action) {
            LicensesAction.Retry -> load()
            LicensesAction.DismissMessage -> reduce { LicensesReducer.run { withMessage(null) } }
            LicensesAction.DismissSheet -> reduce { LicensesReducer.run { withSheet(null) } }
        }
    }

    private fun load() {
        launchCatching(onError = { error -> reduce { LicensesReducer.run { withError(error) } } }) {
            yield()
            val libraries = licensesOf(reader.read())
            reduce { LicensesReducer.run { withLibraries(libraries) } }
        }
    }
}

internal fun licensesOf(json: String): Libs {
    if (!json.contains("\"libraries\"")) {
        error("License notices could not be read.")
    }
    val parsed = Libs.Builder().withJson(json).build()
    val shown = parsed.libraries.filter { library ->
        library.targets != setOf(JVM_TARGET) && "hot-reload" !in library.uniqueId
    }
    return parsed.copy(libraries = shown)
}

private const val JVM_TARGET = "jvm"
