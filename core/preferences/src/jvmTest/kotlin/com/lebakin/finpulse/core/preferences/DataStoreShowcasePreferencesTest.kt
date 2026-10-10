package com.lebakin.finpulse.core.preferences

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class DataStoreShowcasePreferencesTest {
    @Test
    fun showcaseModeStartsOffAndCanBeTurnedOn() = runTest {
        val directory = Files.createTempDirectory("finpulse-preferences")
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val preferences = openShowcasePreferences(
            path = directory.resolve("finpulse.preferences_pb").toString(),
            scope = scope,
        )
        try {
            assertFalse(preferences.observeShowcaseMode().first())
            preferences.setShowcaseMode(true)
            assertTrue(preferences.observeShowcaseMode().first())
            preferences.setShowcaseMode(false)
            assertFalse(preferences.observeShowcaseMode().first())
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun onboardingAndShowcaseShareOneFile() = runTest {
        val directory = Files.createTempDirectory("finpulse-preferences-both")
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val preferences = openAppPreferences(
            path = directory.resolve("finpulse.preferences_pb").toString(),
            scope = scope,
        )
        try {
            assertFalse(preferences.onboarding.observeCompleted().first())
            preferences.onboarding.setCompleted(true)
            preferences.showcase.setShowcaseMode(true)
            assertTrue(preferences.onboarding.observeCompleted().first())
            assertTrue(preferences.showcase.observeShowcaseMode().first())
            preferences.onboarding.setCompleted(false)
            assertFalse(preferences.onboarding.observeCompleted().first())
            assertTrue(preferences.showcase.observeShowcaseMode().first())
        } finally {
            scope.cancel()
        }
    }
}
