package com.example.swara_browser.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.swara_browser.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "browser_settings")

enum class RedirectPolicy {
    ALWAYS_BROWSER,
    ALWAYS_APP
}

class BrowserSettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val SEARCH_ENGINE_CHOSEN = booleanPreferencesKey("is_search_engine_selected")
        val DESKTOP_MODE = booleanPreferencesKey("desktop_mode")
        val JAVASCRIPT_ENABLED = booleanPreferencesKey("javascript_enabled")
        val PRIVATE_MODE = booleanPreferencesKey("private_mode")
        val ANTI_TRACKING = booleanPreferencesKey("anti_tracking")
        val HTTPS_ONLY = booleanPreferencesKey("https_only")
        val INDIAN_LANGUAGE = stringPreferencesKey("indian_language")
        val EULA_ACCEPTED = booleanPreferencesKey("eula_accepted")
        val DATA_SAVER_ENABLED = booleanPreferencesKey("data_saver_enabled")
        val STRICT_AD_BLOCK_ENABLED = booleanPreferencesKey("strict_ad_block_enabled")
        val YOUTUBE_AD_BLOCK_ENABLED = booleanPreferencesKey("youtube_ad_block_enabled")
        val YOUTUBE_EMBEDDED_AD_BLOCK_ENABLED = booleanPreferencesKey("youtube_embedded_ad_block_enabled")
        val REMOVE_ANNOTATIONS_ENABLED = booleanPreferencesKey("remove_annotations_enabled")
        val UNRESTRICTED_SOCIAL_MODE_ENABLED = booleanPreferencesKey("unrestricted_social_mode_enabled")
        val VIRUS_TOTAL_ENABLED = booleanPreferencesKey("virus_total_enabled")
        val AUTO_UPDATE_EXTENSIONS_ENABLED = booleanPreferencesKey("auto_update_extensions_enabled")
        val COOKIE_AUTO_DESTRUCT_DURATION = stringPreferencesKey("cookie_auto_destruct_duration")
        val STARTUP_MODE = stringPreferencesKey("startup_mode")
        val CUSTOM_STARTUP_URL = stringPreferencesKey("custom_startup_url")
        val PINNED_SHORTCUTS = stringPreferencesKey("pinned_shortcuts")
        val LAST_VISITED_URL = stringPreferencesKey("last_visited_url")
        val SHOW_TRENDING_NEWS = booleanPreferencesKey("show_trending_news")
        val SELECTED_NEWS_TOPICS = stringSetPreferencesKey("selected_news_topics")
        val IS_NEWS_TOPIC_SETUP_COMPLETED = booleanPreferencesKey("is_news_topic_setup_completed")
        val IS_AUTOFILL_ENABLED = booleanPreferencesKey("is_autofill_enabled")
        val IS_ADULT_CONTENT_BLOCKED = booleanPreferencesKey("is_adult_content_blocked")
        val BLOCKED_ADS_COUNT = intPreferencesKey("blocked_ads_count")
        val TAB_SESSION_JSON = stringPreferencesKey("tab_session_json")
        val REDIRECT_POLICY_MAP = stringPreferencesKey("redirect_policy_map")
    }

    val defaultNewsTopics = setOf("India Top", "Tech", "Business", "Science", "Sports", "Movies")

    fun getRedirectPolicy(domain: String): Flow<RedirectPolicy?> {
        val key = stringPreferencesKey("redirect_policy_$domain")
        return context.dataStore.data.map { preferences ->
            preferences[key]?.let { runCatching { RedirectPolicy.valueOf(it) }.getOrNull() }
        }
    }

    suspend fun saveRedirectPolicy(domain: String, policy: RedirectPolicy) {
        val key = stringPreferencesKey("redirect_policy_$domain")
        context.dataStore.edit { preferences ->
            preferences[key] = policy.name
        }
    }

    suspend fun clearRedirectPolicy(domain: String) {
        val key = stringPreferencesKey("redirect_policy_$domain")
        context.dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }

    val redirectPolicyMapFlow: Flow<Map<String, String>> = context.dataStore.data.map { preferences ->
        val json = preferences[PreferencesKeys.REDIRECT_POLICY_MAP] ?: ""
        if (json.isBlank()) {
            emptyMap()
        } else {
            try {
                val obj = JSONObject(json)
                val map = mutableMapOf<String, String>()
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key] = obj.getString(key)
                }
                map
            } catch (_: Exception) {
                emptyMap()
            }
        }
    }

    suspend fun saveRedirectPolicy(domain: String, policy: String) {
        if (domain.isBlank()) return
        context.dataStore.edit { preferences ->
            val json = preferences[PreferencesKeys.REDIRECT_POLICY_MAP] ?: ""
            val obj = if (json.isNotBlank()) try { JSONObject(json) } catch (_: Exception) { JSONObject() } else JSONObject()
            obj.put(domain.lowercase(), policy)
            preferences[PreferencesKeys.REDIRECT_POLICY_MAP] = obj.toString()
        }
    }

    val tabSessionFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TAB_SESSION_JSON] ?: ""
    }

    suspend fun saveTabSession(tabsJson: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TAB_SESSION_JSON] = tabsJson
        }
    }

    val customShortcutsFlow: Flow<List<IndianBrand>> = context.dataStore.data.map { preferences ->
        val json = preferences[PreferencesKeys.PINNED_SHORTCUTS] ?: ""
        if (json.isBlank()) {
            emptyList()
        } else {
            try {
                val jsonArray = JSONArray(json)
                val list = mutableListOf<IndianBrand>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        IndianBrand(
                            id = obj.optString("id", "custom_$i"),
                            name = obj.optString("name", "Shortcut"),
                            initialLetter = obj.optString("initialLetter", "S"),
                            category = obj.optString("category", "Custom"),
                            accentColor = Color(0xFF008080),
                            url = obj.optString("url", "https://google.com")
                        )
                    )
                }
                list
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val name = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    val searchEngineFlow: Flow<SearchEngine> = context.dataStore.data.map { preferences ->
        val name = preferences[PreferencesKeys.SEARCH_ENGINE] ?: SearchEngine.GOOGLE.name
        try {
            SearchEngine.valueOf(name)
        } catch (_: Exception) {
            SearchEngine.GOOGLE
        }
    }

    val searchEngineChosenFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SEARCH_ENGINE_CHOSEN] ?: false
    }

    val desktopModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DESKTOP_MODE] ?: false
    }

    val javascriptEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.JAVASCRIPT_ENABLED] ?: true
    }

    val privateModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PRIVATE_MODE] ?: false
    }

    val antiTrackingFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ANTI_TRACKING] ?: true
    }

    val httpsOnlyFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HTTPS_ONLY] ?: true
    }

    val indianLanguageFlow: Flow<IndianLanguage> = context.dataStore.data.map { preferences ->
        val name = preferences[PreferencesKeys.INDIAN_LANGUAGE] ?: IndianLanguage.ENGLISH.name
        try {
            IndianLanguage.valueOf(name)
        } catch (_: Exception) {
            IndianLanguage.ENGLISH
        }
    }

    val eulaAcceptedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.EULA_ACCEPTED] ?: false
    }

    val dataSaverEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DATA_SAVER_ENABLED] ?: false
    }

    val strictAdBlockEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.STRICT_AD_BLOCK_ENABLED] ?: true
    }

    val youtubeAdBlockEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.YOUTUBE_AD_BLOCK_ENABLED] ?: true
    }

    val youtubeEmbeddedAdBlockEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.YOUTUBE_EMBEDDED_AD_BLOCK_ENABLED] ?: true
    }

    val removeAnnotationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMOVE_ANNOTATIONS_ENABLED] ?: true
    }

    val unrestrictedSocialModeEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.UNRESTRICTED_SOCIAL_MODE_ENABLED] ?: true
    }

    val virusTotalEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VIRUS_TOTAL_ENABLED] ?: true
    }

    val autoUpdateExtensionsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_UPDATE_EXTENSIONS_ENABLED] ?: true
    }

    val cookieAutoDestructDurationFlow: Flow<CookieAutoDestructDuration> = context.dataStore.data.map { preferences ->
        val name = preferences[PreferencesKeys.COOKIE_AUTO_DESTRUCT_DURATION] ?: CookieAutoDestructDuration.OFF.name
        try {
            CookieAutoDestructDuration.valueOf(name)
        } catch (_: Exception) {
            CookieAutoDestructDuration.OFF
        }
    }

    val startupModeFlow: Flow<StartupMode> = context.dataStore.data.map { preferences ->
        val name = preferences[PreferencesKeys.STARTUP_MODE] ?: StartupMode.OPEN_NEW_TAB.name
        try {
            StartupMode.valueOf(name)
        } catch (_: Exception) {
            StartupMode.OPEN_NEW_TAB
        }
    }

    val customStartupUrlFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CUSTOM_STARTUP_URL] ?: "https://www.google.co.in"
    }

    val showTrendingNewsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_TRENDING_NEWS] ?: false
    }

    val selectedNewsTopicsFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SELECTED_NEWS_TOPICS] ?: defaultNewsTopics
    }

    val isNewsTopicSetupCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_NEWS_TOPIC_SETUP_COMPLETED] ?: false
    }

    val isAutofillEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_AUTOFILL_ENABLED] ?: true
    }

    val isAdultContentBlockedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_ADULT_CONTENT_BLOCKED] ?: true
    }

    val blockedAdsCountFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BLOCKED_ADS_COUNT] ?: 0
    }

    suspend fun addCustomShortcut(name: String, url: String) {
        val cleanUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        val newBrand = IndianBrand(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            initialLetter = name.firstOrNull()?.uppercase() ?: "S",
            category = "Custom",
            accentColor = Color(0xFF008080),
            url = cleanUrl
        )

        context.dataStore.edit { preferences ->
            val existingJson = preferences[PreferencesKeys.PINNED_SHORTCUTS] ?: ""
            val existingArray = if (existingJson.isNotBlank()) {
                try { JSONArray(existingJson) } catch (_: Exception) { JSONArray() }
            } else {
                JSONArray()
            }

            val newArray = JSONArray()
            val newObj = JSONObject().apply {
                put("id", newBrand.id)
                put("name", newBrand.name)
                put("initialLetter", newBrand.initialLetter)
                put("category", newBrand.category)
                put("url", newBrand.url)
            }
            newArray.put(newObj)

            for (i in 0 until existingArray.length()) {
                newArray.put(existingArray.get(i))
            }

            preferences[PreferencesKeys.PINNED_SHORTCUTS] = newArray.toString()
        }
    }

    suspend fun removeCustomShortcut(id: String) {
        context.dataStore.edit { preferences ->
            val existingJson = preferences[PreferencesKeys.PINNED_SHORTCUTS] ?: ""
            if (existingJson.isNotBlank()) {
                try {
                    val array = JSONArray(existingJson)
                    val newArray = JSONArray()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        if (obj.optString("id") != id) {
                            newArray.put(obj)
                        }
                    }
                    preferences[PreferencesKeys.PINNED_SHORTCUTS] = newArray.toString()
                } catch (_: Exception) {
                    // Ignore
                }
            }
        }
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun setSearchEngine(searchEngine: SearchEngine) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SEARCH_ENGINE] = searchEngine.name
            preferences[PreferencesKeys.SEARCH_ENGINE_CHOSEN] = true
        }
    }

    suspend fun setDesktopMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DESKTOP_MODE] = enabled
        }
    }

    suspend fun setJavascriptEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.JAVASCRIPT_ENABLED] = enabled
        }
    }

    suspend fun setPrivateMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PRIVATE_MODE] = enabled
        }
    }

    suspend fun setAntiTracking(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANTI_TRACKING] = enabled
        }
    }

    suspend fun setHttpsOnly(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HTTPS_ONLY] = enabled
        }
    }

    suspend fun setIndianLanguage(language: IndianLanguage) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INDIAN_LANGUAGE] = language.name
        }
    }

    suspend fun setEulaAccepted(accepted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EULA_ACCEPTED] = accepted
        }
    }

    suspend fun setDataSaverEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DATA_SAVER_ENABLED] = enabled
        }
    }

    suspend fun setStrictAdBlockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STRICT_AD_BLOCK_ENABLED] = enabled
        }
    }

    suspend fun setYoutubeAdBlockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.YOUTUBE_AD_BLOCK_ENABLED] = enabled
        }
    }

    suspend fun setYoutubeEmbeddedAdBlockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.YOUTUBE_EMBEDDED_AD_BLOCK_ENABLED] = enabled
        }
    }

    suspend fun setRemoveAnnotationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMOVE_ANNOTATIONS_ENABLED] = enabled
        }
    }

    suspend fun setUnrestrictedSocialModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.UNRESTRICTED_SOCIAL_MODE_ENABLED] = enabled
        }
    }

    suspend fun setVirusTotalEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIRUS_TOTAL_ENABLED] = enabled
        }
    }

    suspend fun setAutoUpdateExtensionsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_UPDATE_EXTENSIONS_ENABLED] = enabled
        }
    }

    suspend fun setCookieAutoDestructDuration(duration: CookieAutoDestructDuration) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COOKIE_AUTO_DESTRUCT_DURATION] = duration.name
        }
    }

    suspend fun setStartupMode(mode: StartupMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STARTUP_MODE] = mode.name
        }
    }

    suspend fun setCustomStartupUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_STARTUP_URL] = url
        }
    }

    suspend fun setLastVisitedUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_VISITED_URL] = url
        }
    }

    suspend fun setShowTrendingNews(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_TRENDING_NEWS] = show
        }
    }

    suspend fun updateSelectedNewsTopics(topics: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_NEWS_TOPICS] = topics
        }
    }

    suspend fun completeNewsTopicSetup() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_NEWS_TOPIC_SETUP_COMPLETED] = true
        }
    }

    suspend fun setAutofillEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_AUTOFILL_ENABLED] = enabled
        }
    }

    suspend fun setAdultContentBlocked(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ADULT_CONTENT_BLOCKED] = enabled
        }
    }

    suspend fun incrementBlockedAdsCount() {
        context.dataStore.edit { preferences ->
            val count = preferences[PreferencesKeys.BLOCKED_ADS_COUNT] ?: 0
            preferences[PreferencesKeys.BLOCKED_ADS_COUNT] = count + 1
        }
    }
}
