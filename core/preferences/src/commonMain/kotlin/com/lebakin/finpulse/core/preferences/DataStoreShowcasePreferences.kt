package com.lebakin.finpulse.core.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

internal expect val preferencesDispatcher: CoroutineDispatcher

expect fun preferencesFilePath(): String

fun openAppPreferences(
    path: String,
    scope: CoroutineScope = CoroutineScope(preferencesDispatcher + SupervisorJob()),
): AppPreferences {
    val store = PreferenceDataStoreFactory.createWithPath(
        scope = scope,
        produceFile = { path.toPath() },
    )
    return AppPreferences(
        showcase = DataStoreShowcasePreferences(store),
        onboarding = DataStoreOnboardingPreferences(store),
        consent = DataStoreConsentPreferences(store),
        theme = DataStoreThemePreferences(store),
    )
}

fun openShowcasePreferences(
    path: String,
    scope: CoroutineScope = CoroutineScope(preferencesDispatcher + SupervisorJob()),
): ShowcasePreferences = openAppPreferences(path, scope).showcase

private class DataStoreShowcasePreferences(
    private val store: DataStore<Preferences>,
) : ShowcasePreferences {
    override fun observeShowcaseMode(): Flow<Boolean> = store.data.map { preferences ->
        preferences[SHOWCASE_MODE] ?: false
    }

    override suspend fun setShowcaseMode(enabled: Boolean) {
        store.edit { preferences ->
            preferences[SHOWCASE_MODE] = enabled
        }
    }

    private companion object {
        val SHOWCASE_MODE = booleanPreferencesKey("showcase_mode")
    }
}

private class DataStoreOnboardingPreferences(
    private val store: DataStore<Preferences>,
) : OnboardingPreferences {
    override fun observeCompleted(): Flow<Boolean> = store.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    override suspend fun setCompleted(completed: Boolean) {
        store.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }
}

private class DataStoreConsentPreferences(
    private val store: DataStore<Preferences>,
) : ConsentPreferences {
    override fun observeGranted(): Flow<Boolean> = store.data.map { preferences ->
        preferences[SENDING_GRANTED] ?: false
    }

    override suspend fun isGranted(): Boolean = observeGranted().first()

    override suspend fun setGranted(granted: Boolean) {
        store.edit { preferences ->
            preferences[SENDING_GRANTED] = granted
        }
    }

    private companion object {
        val SENDING_GRANTED = booleanPreferencesKey("sending_granted")
    }
}

private class DataStoreThemePreferences(
    private val store: DataStore<Preferences>,
) : ThemePreferences {
    override fun observe(): Flow<ThemeMode> = store.data.map { preferences ->
        ThemeMode.fromStored(preferences[THEME])
    }

    override suspend fun set(mode: ThemeMode) {
        store.edit { preferences ->
            preferences[THEME] = mode.stored()
        }
    }

    private companion object {
        val THEME = stringPreferencesKey("theme_mode")
    }
}
