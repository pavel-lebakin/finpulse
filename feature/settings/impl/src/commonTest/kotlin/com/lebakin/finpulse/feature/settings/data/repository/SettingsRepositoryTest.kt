package com.lebakin.finpulse.feature.settings.data.repository

import com.lebakin.finpulse.core.preferences.ConsentPreferences
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class SettingsRepositoryTest {
    @Test
    fun consentStartsOff() = runTest {
        val preferences = FakeConsentPreferences()
        val repository = SettingsRepository(preferences)
        assertFalse(repository.observeConsent().first())
    }

    @Test
    fun grantThenDecline() = runTest {
        val preferences = FakeConsentPreferences()
        val repository = SettingsRepository(preferences)
        repository.grantConsent()
        assertTrue(repository.observeConsent().first())
        repository.declineConsent()
        assertFalse(preferences.isGranted())
    }
}

private class FakeConsentPreferences : ConsentPreferences {
    private val granted = MutableStateFlow(false)

    override fun observeGranted(): Flow<Boolean> = granted

    override suspend fun isGranted(): Boolean = granted.value

    override suspend fun setGranted(granted: Boolean) {
        this.granted.value = granted
    }
}
