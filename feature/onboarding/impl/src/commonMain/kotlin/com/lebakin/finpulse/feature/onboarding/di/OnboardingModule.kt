package com.lebakin.finpulse.feature.onboarding.di

import com.lebakin.finpulse.feature.onboarding.api.OnboardingEntry
import com.lebakin.finpulse.feature.onboarding.api.OnboardingReader
import com.lebakin.finpulse.feature.onboarding.api.OnboardingWriter
import com.lebakin.finpulse.feature.onboarding.data.repository.OnboardingRepository
import com.lebakin.finpulse.feature.onboarding.presentation.navigation.OnboardingEntryImpl
import com.lebakin.finpulse.feature.onboarding.presentation.navigation.OnboardingNavigator
import com.lebakin.finpulse.feature.onboarding.presentation.navigation.OnboardingNavigatorImpl
import com.lebakin.finpulse.feature.onboarding.presentation.viewmodel.LaunchScreenViewModel
import com.lebakin.finpulse.feature.onboarding.presentation.viewmodel.OnboardingScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingModule = module {
    single { OnboardingRepository(get(), get()) }
    single<OnboardingReader> { get<OnboardingRepository>() }
    single<OnboardingWriter> { get<OnboardingRepository>() }
    single<OnboardingNavigator> { OnboardingNavigatorImpl(get()) }
    single<OnboardingEntry> { OnboardingEntryImpl(get(), get()) }
    viewModelOf(::LaunchScreenViewModel)
    viewModelOf(::OnboardingScreenViewModel)
}
