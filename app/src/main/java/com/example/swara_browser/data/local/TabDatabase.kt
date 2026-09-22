package com.example.swara_browser.data.local

import android.content.Context

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import org.json.JSONArray
import org.json.JSONObject

data class TabEntity(
    val id: String,
    val url: String,
    val title: String,
    val position: Int,
    val lastAccessedTimestamp: Long = System.currentTimeMillis()
)

interface TabDao {
    fun getAllTabs(): Flow<List<TabEntity>>
    suspend fun getTabsSnapshot(): List<TabEntity>
    suspend fun insertOrUpdateTab(tab: TabEntity)
    suspend fun deleteTab(tabId: String)
    suspend fun clearAllTabs()
}

class DataStoreTabDao(context: Context) : TabDao {
    private val tabSessionPrefs = TabSessionPreferences(context)

    override fun getAllTabs(): Flow<List<TabEntity>> {
        return flow { emit(getTabsSnapshot()) }
    }

    override suspend fun getTabsSnapshot(): List<TabEntity> {
        val json = tabSessionPrefs.rawTabJsonFlow.firstOrNull() ?: ""
        if (json.isBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<TabEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TabEntity(
                        id = obj.optString("id", "tab_$i"),
                        url = obj.optString("url", ""),
                        title = obj.optString("title", "New Tab"),
                        position = obj.optInt("position", i),
                        lastAccessedTimestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list.sortedBy { it.position }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun insertOrUpdateTab(tab: TabEntity) {
        val current = getTabsSnapshot().toMutableList()
        current.removeAll { it.id == tab.id }
        current.add(tab)
        current.sortBy { it.position }

        val array = JSONArray()
        for (t in current) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("url", t.url)
                put("title", t.title)
                put("position", t.position)
                put("timestamp", t.lastAccessedTimestamp)
            }
            array.put(obj)
        }
        tabSessionPrefs.saveRawTabJson(array.toString())
    }

    override suspend fun deleteTab(tabId: String) {
        val current = getTabsSnapshot().filter { it.id != tabId }
        val array = JSONArray()
        for (t in current) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("url", t.url)
                put("title", t.title)
                put("position", t.position)
                put("timestamp", t.lastAccessedTimestamp)
            }
            array.put(obj)
        }
        tabSessionPrefs.saveRawTabJson(array.toString())
    }

    override suspend fun clearAllTabs() {
        tabSessionPrefs.saveRawTabJson("")
    }
}


object TabDatabaseFactory {
    fun getDao(context: Context): TabDao {
        return DataStoreTabDao(context.applicationContext)
    }
}
