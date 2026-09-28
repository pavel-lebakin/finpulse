package com.lebakin.finpulse.di

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.ErrorReporter
import kotlin.test.Test
import kotlin.test.assertIs
import org.koin.dsl.koinApplication

class AppModuleTest {
    @Test
    fun graphProvidesTheReporterAndNavigator() {
        val koin = koinApplication {
            modules(appModule)
        }.koin
        val reporter = koin.get<ErrorReporter>()
        assertIs<NoOpErrorReporter>(reporter)
        reporter.report(AppError.Unexpected(IllegalStateException("unused")))
        koin.get<com.lebakin.finpulse.core.navigation.ComposeRootNavigator>()
    }
}
