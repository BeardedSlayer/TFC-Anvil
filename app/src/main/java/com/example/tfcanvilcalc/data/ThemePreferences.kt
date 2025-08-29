package com.example.tfcanvilcalc.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.tfcanvilcalc.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemePreferences(private val dataStore: DataStore<Preferences>) {
    
    private val themeKey = stringPreferencesKey("theme_mode")
    
    val themeModeFlow: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val themeString = preferences[themeKey] ?: "SYSTEM"
        try {
            ThemeMode.valueOf(themeString)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
    
    suspend fun setTheme(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[themeKey] = mode.name
        }
    }
}