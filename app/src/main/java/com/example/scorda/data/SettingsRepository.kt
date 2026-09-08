package com.example.scorda.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.scorda.data.database.entities.BrushFamilyType
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
    private val _eraserThickness = floatPreferencesKey("eraser_thickness")
    private val _isTabsVisible = booleanPreferencesKey("is_tabs_visible")

    // Tool Settings
    private val _penColor = intPreferencesKey("pen_color")
    private val _penThickness = floatPreferencesKey("pen_thickness")
    private val _markerColor = intPreferencesKey("marker_color")
    private val _markerThickness = floatPreferencesKey("marker_thickness")
    private val _highlighterColor = intPreferencesKey("highlighter_color")
    private val _highlighterThickness = floatPreferencesKey("highlighter_thickness")
    private val _dashedColor = intPreferencesKey("dashed_color")
    private val _dashedThickness = floatPreferencesKey("dashed_thickness")
    private val _colorPresets = stringPreferencesKey("color_presets")

    companion object {
        const val COLOR_MIDNIGHT = 0xFF1A1C1E.toInt()
        const val COLOR_IRON = 0xFF45474F.toInt()
        const val COLOR_CORAL = 0xFFD14343.toInt()
        const val COLOR_ROSE = 0xFFD16086.toInt()
        const val COLOR_AMBER = 0xFFD1A543.toInt()
        const val COLOR_SAGE = 0xFF4B915E.toInt()
        const val COLOR_OCEAN = 0xFF4386D1.toInt()
    }

    private val defaultColorPresets = listOf(
        COLOR_MIDNIGHT,
        COLOR_IRON,
        COLOR_CORAL,
        COLOR_ROSE,
        COLOR_AMBER,
        COLOR_SAGE,
        COLOR_OCEAN
    )

    val colorPresets: Flow<List<Int>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val json = preferences[_colorPresets] ?: ""
            if (json.isEmpty()) {
                defaultColorPresets
            } else {
                try {
                    Json.decodeFromString<List<Int>>(json)
                } catch (_: Exception) {
                    defaultColorPresets
                }
            }
        }

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

    val eraserThickness: Flow<Float> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[_eraserThickness] ?: 20f
        }

    fun toolColor(family: BrushFamilyType): Flow<Int> = context.dataStore.data
        .map { preferences ->
            when (family) {
                BrushFamilyType.PRESSURE_PEN -> preferences[_penColor] ?: COLOR_MIDNIGHT
                BrushFamilyType.MARKER -> preferences[_markerColor] ?: COLOR_OCEAN
                BrushFamilyType.HIGHLIGHTER -> preferences[_highlighterColor] ?: COLOR_AMBER
                BrushFamilyType.DASHED_LINE -> preferences[_dashedColor] ?: COLOR_IRON
            }
        }

    fun toolThickness(family: BrushFamilyType): Flow<Float> = context.dataStore.data
        .map { preferences ->
            when (family) {
                BrushFamilyType.PRESSURE_PEN -> preferences[_penThickness] ?: 5f
                BrushFamilyType.MARKER -> preferences[_markerThickness] ?: 10f
                BrushFamilyType.HIGHLIGHTER -> preferences[_highlighterThickness] ?: 20f
                BrushFamilyType.DASHED_LINE -> preferences[_dashedThickness] ?: 5f
            }
        }

    val isTabsVisible: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[_isTabsVisible] ?: true
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

    suspend fun saveEraserThickness(thickness: Float) {
        context.dataStore.edit { preferences ->
            preferences[_eraserThickness] = thickness
        }
    }

    suspend fun saveToolColor(family: BrushFamilyType, color: Int) {
        context.dataStore.edit { preferences ->
            when (family) {
                BrushFamilyType.PRESSURE_PEN -> preferences[_penColor] = color
                BrushFamilyType.MARKER -> preferences[_markerColor] = color
                BrushFamilyType.HIGHLIGHTER -> preferences[_highlighterColor] = color
                BrushFamilyType.DASHED_LINE -> preferences[_dashedColor] = color
            }
        }
    }

    suspend fun saveToolThickness(family: BrushFamilyType, thickness: Float) {
        context.dataStore.edit { preferences ->
            when (family) {
                BrushFamilyType.PRESSURE_PEN -> preferences[_penThickness] = thickness
                BrushFamilyType.MARKER -> preferences[_markerThickness] = thickness
                BrushFamilyType.HIGHLIGHTER -> preferences[_highlighterThickness] = thickness
                BrushFamilyType.DASHED_LINE -> preferences[_dashedThickness] = thickness
            }
        }
    }

    suspend fun saveTabsVisible(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[_isTabsVisible] = enabled
        }
    }

    suspend fun saveColorPreset(color: Int) {
        context.dataStore.edit { preferences ->
            val json = preferences[_colorPresets] ?: ""
            val currentPresets = if (json.isEmpty()) {
                defaultColorPresets
            } else {
                try {
                    Json.decodeFromString<List<Int>>(json)
                } catch (_: Exception) {
                    defaultColorPresets
                }
            }
            if (!currentPresets.contains(color)) {
                val updatedPresets = currentPresets + color
                preferences[_colorPresets] = Json.encodeToString(updatedPresets)
            }
        }
    }

    suspend fun deleteColorPreset(color: Int) {
        context.dataStore.edit { preferences ->
            val json = preferences[_colorPresets] ?: ""
            val currentPresets = if (json.isEmpty()) {
                defaultColorPresets
            } else {
                try {
                    Json.decodeFromString<List<Int>>(json)
                } catch (_: Exception) {
                    defaultColorPresets
                }
            }
            val updatedPresets = currentPresets.filter { it != color }
            preferences[_colorPresets] = Json.encodeToString(updatedPresets)
        }
    }
}
