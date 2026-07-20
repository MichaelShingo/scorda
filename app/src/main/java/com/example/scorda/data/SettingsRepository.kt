package com.example.scorda.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.dataStore by preferencesDataStore(
    name = "settings",
)

@Serializable
data class OpenScore(
    val scoreId: Long,
    val setlistId: Long?,
    val lastOpenPage: Int,
)

class SettingsRepository(private val context: Context) {
    private val _isDarkMode = booleanPreferencesKey("is_dark_mode")
    private val _currentSetlistId = longPreferencesKey("current_setlist_id")
    private val _currentTabIndex = longPreferencesKey("current_tab_index")
    private val _openScores = stringPreferencesKey("open_scores")

    val openScores: Flow<List<OpenScore>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val json = preferences[_openScores] ?: "[]"
            try {
                Json.decodeFromString<List<OpenScore>>(json)
            } catch (_: Exception) {
                emptyList()
            }
        }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[_isDarkMode] ?: false
        }

    val currentSetlistId: Flow<Long?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[_currentSetlistId]
        }

    val currentTabIndex: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[_currentTabIndex]?.toInt() ?: 0
        }

    suspend fun updateOpenScores(transform: (List<OpenScore>) -> List<OpenScore>) {
        context.dataStore.edit { preferences ->
            val json = preferences[_openScores] ?: "[]"
            val currentScores = try {
                Json.decodeFromString<List<OpenScore>>(json)
            } catch (_: Exception) {
                emptyList()
            }
            val updatedScores = transform(currentScores)
            preferences[_openScores] = Json.encodeToString(updatedScores)
        }
    }

    suspend fun saveCurrentSetlistId(setlistId: Long?) {
        context.dataStore.edit { preferences ->
            if (setlistId != null) {
                preferences[_currentSetlistId] = setlistId
            } else {
                preferences.remove(_currentSetlistId)
            }
        }
    }

    suspend fun saveCurrentTabIndex(tabIndex: Int) {
        context.dataStore.edit { preferences ->
            preferences[_currentTabIndex] = tabIndex.toLong()
        }
    }

    suspend fun saveDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[_isDarkMode] = enabled
        }
    }
}
