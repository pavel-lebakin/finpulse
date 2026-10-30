package com.lebakin.finpulse.core.preferences

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
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

    @Test
    fun consentStartsOffAndSharesTheFile() = runTest {
        val directory = Files.createTempDirectory("finpulse-preferences-consent")
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val preferences = openAppPreferences(
            path = directory.resolve("finpulse.preferences_pb").toString(),
            scope = scope,
        )
        try {
            assertFalse(preferences.consent.isGranted())
            preferences.consent.setGranted(true)
            preferences.onboarding.setCompleted(true)
            assertTrue(preferences.consent.observeGranted().first())
            assertTrue(preferences.onboarding.observeCompleted().first())
            preferences.consent.setGranted(false)
            assertFalse(preferences.consent.isGranted())
            assertTrue(preferences.onboarding.observeCompleted().first())
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun themeStartsAsSystemAndCanBeStored() = runTest {
        val directory = Files.createTempDirectory("finpulse-preferences-theme")
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val preferences = openAppPreferences(
            path = directory.resolve("finpulse.preferences_pb").toString(),
            scope = scope,
        )
        try {
            assertEquals(ThemeMode.System, preferences.theme.observe().first())
            preferences.theme.set(ThemeMode.Dark)
            assertEquals(ThemeMode.Dark, preferences.theme.observe().first())
            preferences.theme.set(ThemeMode.Light)
            assertEquals(ThemeMode.Light, preferences.theme.observe().first())
            assertEquals(ThemeMode.Light, ThemeMode.fromStored("light"))
            assertEquals(ThemeMode.System, ThemeMode.fromStored("other"))
        } finally {
            scope.cancel()
        }
    }
}
