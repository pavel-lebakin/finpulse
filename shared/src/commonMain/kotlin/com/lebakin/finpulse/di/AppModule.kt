package com.lebakin.finpulse.di

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.ErrorReporter
import com.lebakin.finpulse.core.database.databaseFilePath
import com.lebakin.finpulse.core.database.openFinPulseDatabase
import com.lebakin.finpulse.core.navigation.ComposeRootNavigator
import com.lebakin.finpulse.core.navigation.RootNavigator
import com.lebakin.finpulse.core.preferences.AppPreferences
import com.lebakin.finpulse.core.preferences.OnboardingPreferences
import com.lebakin.finpulse.core.preferences.ShowcasePreferences
import com.lebakin.finpulse.core.preferences.openAppPreferences
import com.lebakin.finpulse.core.preferences.preferencesFilePath
import com.lebakin.finpulse.feature.ledger.di.ledgerModule
import com.lebakin.finpulse.feature.onboarding.di.onboardingModule
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
    single<ShowcasePreferences> { get<AppPreferences>().showcase }
    single<OnboardingPreferences> { get<AppPreferences>().onboarding }
    includes(ledgerModule)
    includes(onboardingModule)
    single<ErrorReporter> { NoOpErrorReporter() }
    single { ComposeRootNavigator() }
    single<RootNavigator> { get<ComposeRootNavigator>() }
}

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}
