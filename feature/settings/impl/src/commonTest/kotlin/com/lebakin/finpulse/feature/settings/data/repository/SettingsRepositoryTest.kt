package com.lebakin.finpulse.feature.settings.data.repository

import com.lebakin.finpulse.core.preferences.ConsentPreferences
import com.lebakin.finpulse.core.security.SecretStore
import com.lebakin.finpulse.feature.settings.api.KeySave
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class SettingsRepositoryTest {
    @Test
    fun consentStartsOff() = runTest {
        val preferences = FakeConsentPreferences()
        val repository = SettingsRepository(preferences, FakeSecretStore())
        assertFalse(repository.observeConsent().first())
    }

    @Test
    fun grantThenDecline() = runTest {
        val preferences = FakeConsentPreferences()
        val repository = SettingsRepository(preferences, FakeSecretStore())
        repository.grantConsent()
        assertTrue(repository.observeConsent().first())
        repository.declineConsent()
        assertFalse(preferences.isGranted())
    }

    @Test
    fun saveStoresAnAcceptedKeyAfterConsent() = runTest {
        val secrets = FakeSecretStore()
        val repository = SettingsRepository(FakeConsentPreferences(granted = true), secrets)
        assertFalse(repository.observeKeySaved().first())
        assertEquals(KeySave.Saved, repository.saveKey("token-ok"))
        assertEquals("token-ok", secrets.value)
        assertTrue(repository.observeKeySaved().first())
    }

    @Test
    fun saveWithoutConsentDoesNotWrite() = runTest {
        val secrets = FakeSecretStore()
        val repository = SettingsRepository(FakeConsentPreferences(granted = false), secrets)
        assertEquals(KeySave.NeedsConsent, repository.saveKey("token-ok"))
        assertEquals(0, secrets.saves)
        assertNull(secrets.value)
    }

    @Test
    fun saveRejectsAKeyTheModelWouldIgnore() = runTest {
        val secrets = FakeSecretStore()
        val repository = SettingsRepository(FakeConsentPreferences(granted = true), secrets)
        val rejected = listOf("", " ", "token ok", "a".repeat(513), "café")
        rejected.forEach { key ->
            assertEquals(KeySave.Rejected, repository.saveKey(key))
        }
        assertEquals(KeySave.Saved, repository.saveKey("a".repeat(512)))
        assertEquals(1, secrets.saves)
    }

    @Test
    fun deleteClearsTheSecret() = runTest {
        val secrets = FakeSecretStore()
        val repository = SettingsRepository(FakeConsentPreferences(granted = true), secrets)
        repository.saveKey("token-ok")
        repository.deleteKey()
        assertNull(secrets.value)
        assertFalse(repository.observeKeySaved().first())
    }
}

private class FakeConsentPreferences(
    granted: Boolean = false,
) : ConsentPreferences {
    private val granted = MutableStateFlow(granted)

    override fun observeGranted(): Flow<Boolean> = granted

    override suspend fun isGranted(): Boolean = granted.value

    override suspend fun setGranted(granted: Boolean) {
        this.granted.value = granted
    }
}

private class FakeSecretStore : SecretStore {
    private val saved = MutableStateFlow(false)
    var value: String? = null
    var saves: Int = 0

    override fun observeSaved(): Flow<Boolean> = saved

    override suspend fun save(secret: String) {
        saves += 1
        value = secret
        saved.value = true
    }

    override suspend fun read(): String? = value

    override suspend fun delete() {
        value = null
        saved.value = false
    }
}
