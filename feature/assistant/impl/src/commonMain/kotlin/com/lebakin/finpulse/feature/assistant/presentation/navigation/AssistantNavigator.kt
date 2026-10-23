package com.lebakin.finpulse.feature.assistant.presentation.navigation

import com.lebakin.finpulse.core.navigation.RootNavigator
import com.lebakin.finpulse.feature.assistant.api.AssistantEntry

internal interface AssistantNavigator {
    fun openAsk()

    fun openConfirm()

    fun back()
}

internal class AssistantNavigatorImpl(
    private val root: RootNavigator,
) : AssistantNavigator {
    override fun openAsk() {
        root.navigate(AssistantDestination.Ask)
    }

    override fun openConfirm() {
        root.navigate(AssistantDestination.Confirm)
    }

    override fun back() {
        root.back()
    }
}

internal class AssistantEntryImpl(
    private val navigator: AssistantNavigator,
) : AssistantEntry {
    override fun openAsk() {
        navigator.openAsk()
    }
}
