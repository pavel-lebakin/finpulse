package com.lebakin.finpulse.feature.settings.di

import com.lebakin.finpulse.feature.settings.api.SettingsEntry
import com.lebakin.finpulse.feature.settings.api.SettingsReader
import com.lebakin.finpulse.feature.settings.api.SettingsWriter
import com.lebakin.finpulse.feature.settings.data.repository.SettingsRepository
import com.lebakin.finpulse.feature.settings.presentation.navigation.SettingsEntryImpl
import com.lebakin.finpulse.feature.settings.presentation.navigation.SettingsNavigator
import com.lebakin.finpulse.feature.settings.presentation.navigation.SettingsNavigatorImpl
import com.lebakin.finpulse.feature.settings.presentation.viewmodel.ConsentScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
    single { SettingsRepository(get()) }
    single<SettingsReader> { get<SettingsRepository>() }
    single<SettingsWriter> { get<SettingsRepository>() }
    single<SettingsNavigator> { SettingsNavigatorImpl(get()) }
    single<SettingsEntry> { SettingsEntryImpl(get()) }
    viewModelOf(::ConsentScreenViewModel)
}
