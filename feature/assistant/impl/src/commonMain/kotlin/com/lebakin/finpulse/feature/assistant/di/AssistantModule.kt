package com.lebakin.finpulse.feature.assistant.di

import com.lebakin.finpulse.core.ai.ByokModel
import com.lebakin.finpulse.core.ai.OnDeviceModel
import com.lebakin.finpulse.core.ai.openByokModel
import com.lebakin.finpulse.core.ai.openOnDeviceModel
import com.lebakin.finpulse.core.network.HttpsClient
import com.lebakin.finpulse.core.preferences.ConsentPreferences
import com.lebakin.finpulse.core.security.SecretStore
import com.lebakin.finpulse.feature.assistant.api.AssistantEntry
import com.lebakin.finpulse.feature.assistant.domain.PhraseClock
import com.lebakin.finpulse.feature.assistant.domain.PhraseEngines
import com.lebakin.finpulse.feature.assistant.domain.PhraseIds
import com.lebakin.finpulse.feature.assistant.domain.PhraseModel
import com.lebakin.finpulse.feature.assistant.domain.PhraseRules
import com.lebakin.finpulse.feature.assistant.domain.PhraseSession
import com.lebakin.finpulse.feature.assistant.domain.SystemPhraseClock
import com.lebakin.finpulse.feature.assistant.domain.SystemPhraseIds
import com.lebakin.finpulse.feature.assistant.presentation.navigation.AssistantEntryImpl
import com.lebakin.finpulse.feature.assistant.presentation.navigation.AssistantNavigator
import com.lebakin.finpulse.feature.assistant.presentation.navigation.AssistantNavigatorImpl
import com.lebakin.finpulse.feature.assistant.presentation.viewmodel.AskScreenViewModel
import com.lebakin.finpulse.feature.assistant.presentation.viewmodel.ConfirmScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val assistantModule = module {
    single<PhraseClock> { SystemPhraseClock() }
    single<PhraseIds> { SystemPhraseIds() }
    single { PhraseRules(get()) }
    single {
        val store = get<SecretStore>()
        val client = get<HttpsClient>()
        val consent = get<ConsentPreferences>()
        PhraseEngines(
            rules = get(),
            onDevice = { openOnDeviceModel().asPhraseModel() },
            byok = { openByokModel(store, client, consent).asPhraseModel() },
        )
    }
    single { PhraseSession() }
    single<AssistantNavigator> { AssistantNavigatorImpl(get()) }
    single<AssistantEntry> { AssistantEntryImpl(get()) }
    viewModelOf(::AskScreenViewModel)
    viewModelOf(::ConfirmScreenViewModel)
}

private fun OnDeviceModel?.asPhraseModel(): PhraseModel? =
    this?.let { model -> PhraseModel { prompt -> model.complete(prompt) } }

private fun ByokModel?.asPhraseModel(): PhraseModel? =
    this?.let { model -> PhraseModel { prompt -> model.complete(prompt) } }
