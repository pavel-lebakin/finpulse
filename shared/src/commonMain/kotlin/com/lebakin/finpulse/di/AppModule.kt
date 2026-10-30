package com.lebakin.finpulse.di

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.ErrorReporter
import com.lebakin.finpulse.core.database.databaseFilePath
import com.lebakin.finpulse.core.database.openFinPulseDatabase
import com.lebakin.finpulse.core.navigation.ComposeRootNavigator
import com.lebakin.finpulse.core.navigation.RootNavigator
import com.lebakin.finpulse.core.preferences.AppPreferences
import com.lebakin.finpulse.core.preferences.ConsentPreferences
import com.lebakin.finpulse.core.preferences.OnboardingPreferences
import com.lebakin.finpulse.core.preferences.ShowcasePreferences
import com.lebakin.finpulse.core.preferences.ThemePreferences
import com.lebakin.finpulse.core.preferences.openAppPreferences
import com.lebakin.finpulse.core.preferences.preferencesFilePath
import com.lebakin.finpulse.core.network.openHttpsClient
import com.lebakin.finpulse.core.security.openSecretStore
import com.lebakin.finpulse.feature.assistant.di.assistantModule
import com.lebakin.finpulse.feature.insights.di.insightsModule
import com.lebakin.finpulse.feature.ledger.di.ledgerModule
import com.lebakin.finpulse.feature.onboarding.di.onboardingModule
import com.lebakin.finpulse.feature.scanner.di.scannerModule
import com.lebakin.finpulse.feature.settings.api.LicenseReader
import com.lebakin.finpulse.feature.settings.di.settingsModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

expect fun platformModule(): Module

class NoOpErrorReporter : ErrorReporter {
    override fun report(error: AppError) = Unit
}

val appModule = module {
    includes(platformModule())
    single { openFinPulseDatabase(databaseFilePath()) }
    single { openAppPreferences(preferencesFilePath()) }
    single { openSecretStore() }
    single { openHttpsClient() }
    single<ShowcasePreferences> { get<AppPreferences>().showcase }
    single<OnboardingPreferences> { get<AppPreferences>().onboarding }
    single<ConsentPreferences> { get<AppPreferences>().consent }
    single<ThemePreferences> { get<AppPreferences>().theme }
    includes(ledgerModule)
    includes(onboardingModule)
    includes(insightsModule)
    includes(scannerModule)
    includes(assistantModule)
    includes(settingsModule)
    cloudModules().forEach { cloud -> includes(cloud) }
    single<LicenseReader> { GeneratedLicenseReader() }
    single<ErrorReporter> { NoOpErrorReporter() }
    single { ComposeRootNavigator() }
    single<RootNavigator> { get<ComposeRootNavigator>() }
}

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}
