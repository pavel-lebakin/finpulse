package com.lebakin.finpulse.di

import com.lebakin.finpulse.core.navigation.RootNavigator
import com.lebakin.finpulse.core.preferences.OnboardingPreferences
import com.lebakin.finpulse.core.preferences.ShowcasePreferences
import com.lebakin.finpulse.feature.insights.api.InsightsEntry
import com.lebakin.finpulse.feature.ledger.api.LedgerReader
import com.lebakin.finpulse.feature.ledger.api.LedgerWriter
import com.lebakin.finpulse.feature.onboarding.api.OnboardingEntry
import com.lebakin.finpulse.feature.onboarding.api.OnboardingReader
import com.lebakin.finpulse.feature.onboarding.api.OnboardingWriter
import com.lebakin.finpulse.feature.scanner.api.ScannerEntry
import java.nio.file.Files
import kotlin.test.Test
import org.koin.dsl.koinApplication

class LedgerGraphTest {
    @Test
    fun graphProvidesTheLedger() {
        val directory = Files.createTempDirectory("finpulse-graph")
        System.setProperty("finpulse.database.file", directory.resolve("finpulse.db").toString())
        System.setProperty("finpulse.preferences.file", directory.resolve("finpulse.preferences_pb").toString())
        try {
            val koin = koinApplication {
                modules(appModule)
            }.koin
            koin.get<LedgerReader>()
            koin.get<LedgerWriter>()
            koin.get<ShowcasePreferences>()
            koin.get<OnboardingPreferences>()
            koin.get<OnboardingReader>()
            koin.get<OnboardingWriter>()
            koin.get<OnboardingEntry>()
            koin.get<InsightsEntry>()
            koin.get<ScannerEntry>()
            koin.get<RootNavigator>()
        } finally {
            System.clearProperty("finpulse.database.file")
            System.clearProperty("finpulse.preferences.file")
        }
    }
}
