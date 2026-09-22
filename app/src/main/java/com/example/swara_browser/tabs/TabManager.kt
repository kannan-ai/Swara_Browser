package com.example.swara_browser.tabs

import com.example.swara_browser.data.local.TabDao
import com.example.swara_browser.data.local.TabEntity
import com.example.swara_browser.data.local.TabSessionPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

data class TabModel(
    val id: String,
    val url: String,
    val title: String,
    val isIncognito: Boolean = false,
    val isCrashed: Boolean = false,
    val crashReason: String? = null
)

class TabManager(
    private val tabDao: TabDao,
    private val tabSessionPrefs: TabSessionPreferences,
    private val scope: CoroutineScope
) {
    suspend fun restoreTabsSession(): Pair<List<TabModel>, String> {
        val savedTabs = tabDao.getTabsSnapshot()
        val lastActiveId = tabSessionPrefs.activeTabId.firstOrNull()

        if (savedTabs.isNotEmpty()) {
            val tabModels = savedTabs.map {
                TabModel(
                    id = it.id,
                    url = it.url,
                    title = it.title
                )
            }
            val activeId = if (savedTabs.any { it.id == lastActiveId }) {
                lastActiveId ?: savedTabs.first().id
            } else {
                savedTabs.first().id
            }
            return Pair(tabModels, activeId)
        } else {
            val initialTab = TabEntity(
                id = UUID.randomUUID().toString(),
                url = "https://www.google.com",
                title = "Google",
                position = 0
            )
            tabDao.insertOrUpdateTab(initialTab)
            tabSessionPrefs.saveActiveTabId(initialTab.id)
            return Pair(
                listOf(TabModel(id = initialTab.id, url = initialTab.url, title = initialTab.title)),
                initialTab.id
            )
        }
    }

    fun onTabUpdated(tab: TabModel, position: Int) {
        if (tab.isIncognito) return // Do not persist incognito tabs
        scope.launch {
            tabDao.insertOrUpdateTab(
                TabEntity(
                    id = tab.id,
                    url = tab.url,
                    title = tab.title.ifBlank { "New Tab" },
                    position = position
                )
            )
        }
    }

    fun onTabClosed(tabId: String) {
        scope.launch {
            tabDao.deleteTab(tabId)
        }
    }

    fun onTabSelected(tabId: String, isIncognito: Boolean) {
        if (isIncognito) return
        scope.launch {
            tabSessionPrefs.saveActiveTabId(tabId)
        }
    }
}
