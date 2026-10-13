package com.lebakin.finpulse.di

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.ErrorReporter
import com.lebakin.finpulse.core.security.SecretStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.koin.dsl.koinApplication

class AppModuleTest {
    @Test
    fun graphProvidesTheReporterAndNavigator() = runTest {
        val koin = koinApplication {
            modules(appModule)
        }.koin
        val reporter = koin.get<ErrorReporter>()
        assertIs<NoOpErrorReporter>(reporter)
        reporter.report(AppError.Unexpected(IllegalStateException("unused")))
        koin.get<com.lebakin.finpulse.core.navigation.ComposeRootNavigator>()
        val store = koin.get<SecretStore>()
        assertFalse(store.observeSaved().first())
        store.save("local-only")
        assertEquals("local-only", store.read())
        store.delete()
        assertNull(store.read())
    }
}
