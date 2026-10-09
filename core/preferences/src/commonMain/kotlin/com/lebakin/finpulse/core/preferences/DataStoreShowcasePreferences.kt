package com.lebakin.finpulse.core.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

internal expect val preferencesDispatcher: CoroutineDispatcher

expect fun preferencesFilePath(): String

fun openShowcasePreferences(
    path: String,
    scope: CoroutineScope = CoroutineScope(preferencesDispatcher + SupervisorJob()),
): ShowcasePreferences {
    val store = PreferenceDataStoreFactory.createWithPath(
        scope = scope,
        produceFile = { path.toPath() },
    )
    return DataStoreShowcasePreferences(store)
}

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
