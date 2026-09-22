package com.example.swara_browser.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tabSessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "swara_tab_preferences")

class TabSessionPreferences(private val context: Context) {

    private val activeTabKey = stringPreferencesKey("active_tab_id")
    private val rawTabJsonKey = stringPreferencesKey("raw_tab_json")

    val activeTabId: Flow<String?> = context.tabSessionDataStore.data.map { prefs ->
        prefs[activeTabKey]
    }

    val rawTabJsonFlow: Flow<String> = context.tabSessionDataStore.data.map { prefs ->
        prefs[rawTabJsonKey] ?: ""
    }

    suspend fun saveActiveTabId(tabId: String) {
        context.tabSessionDataStore.edit { prefs ->
            prefs[activeTabKey] = tabId
        }
    }

    suspend fun saveRawTabJson(json: String) {
        context.tabSessionDataStore.edit { prefs ->
            prefs[rawTabJsonKey] = json
        }
    }

    suspend fun clearActiveTabId() {
        context.tabSessionDataStore.edit { prefs ->
            prefs.remove(activeTabKey)
        }
    }
}
