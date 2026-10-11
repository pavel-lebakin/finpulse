package com.lebakin.finpulse.feature.insights.di

import com.lebakin.finpulse.feature.insights.api.InsightsEntry
import com.lebakin.finpulse.feature.insights.domain.InsightsClock
import com.lebakin.finpulse.feature.insights.domain.SystemInsightsClock
import com.lebakin.finpulse.feature.insights.presentation.navigation.InsightsEntryImpl
import com.lebakin.finpulse.feature.insights.presentation.navigation.InsightsNavigator
import com.lebakin.finpulse.feature.insights.presentation.navigation.InsightsNavigatorImpl
import com.lebakin.finpulse.feature.insights.presentation.viewmodel.DashboardScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val insightsModule = module {
    single<InsightsClock> { SystemInsightsClock() }
    single<InsightsNavigator> { InsightsNavigatorImpl(get()) }
    single<InsightsEntry> { InsightsEntryImpl(get()) }
    viewModelOf(::DashboardScreenViewModel)
}
