package com.example.swara_browser.ui

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.Settings
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swara_browser.data.AstraPointsRepository
import com.example.swara_browser.data.BrowserSettingsRepository
import com.example.swara_browser.data.CookieAutoDestructDuration
import com.example.swara_browser.data.IndianBrand
import com.example.swara_browser.data.IndianLanguage
import com.example.swara_browser.data.NewsItem
import com.example.swara_browser.data.NewsNotificationManager
import com.example.swara_browser.data.PointsActivityTransaction
import com.example.swara_browser.data.SearchEngine
import com.example.swara_browser.data.SearchSuggestionRepository
import com.example.swara_browser.data.SelfDestructSecurityManager
import com.example.swara_browser.data.StartupMode
import com.example.swara_browser.data.SwaraDownloadTracker
import com.example.swara_browser.data.TrendingNewsEngine
import com.example.swara_browser.data.UpdateCheckerEngine
import kotlin.math.max
import com.example.swara_browser.data.local.TabDatabaseFactory
import com.example.swara_browser.data.local.TabSessionPreferences
import com.example.swara_browser.tabs.TabManager
import com.example.swara_browser.tabs.TabModel
import com.example.swara_browser.ui.theme.ThemeMode
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject

data class BrowserUiState(
    val currentUrl: String = "",
    val searchQuery: String = "",
    val pageTitle: String = "Swara Browser",
    val progress: Int = 0,
    val isLoading: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isSecure: Boolean = true,
    val showSettingsDialog: Boolean = false,
    val showAstraRewardDialog: Boolean = false,
    val showDownloadsScreen: Boolean = false,
    val showExtensionManagerDialog: Boolean = false,
    val showSearchEngineSelectionDialog: Boolean = false,
    val showTabManagerDialog: Boolean = false,
    val showEulaDialog: Boolean = false,
    val isAtHome: Boolean = true
)

@OptIn(FlowPreview::class)
class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BrowserSettingsRepository(application)
    private val pointsRepository = AstraPointsRepository(application)
    private val selfDestructManager = SelfDestructSecurityManager(application)

    private val tabDao = TabDatabaseFactory.getDao(application)
    private val tabSessionPreferences = TabSessionPreferences(application)
    val tabManager = TabManager(tabDao, tabSessionPreferences, viewModelScope)

    private val initialTab = WebTab(id = "tab_${System.currentTimeMillis()}")
    private val _tabs = MutableStateFlow(listOf(initialTab))
    val tabs: StateFlow<List<WebTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow(initialTab.id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    private val _detectedVideoUrl = MutableStateFlow<String?>(null)
    val detectedVideoUrl: StateFlow<String?> = _detectedVideoUrl.asStateFlow()

    private val _liveNewsList = MutableStateFlow<List<NewsItem>>(emptyList())
    val liveNewsList: StateFlow<List<NewsItem>> = _liveNewsList.asStateFlow()

    private val _autocompleteSuggestions = MutableStateFlow<List<String>>(SearchSuggestionRepository.trendingTags)
    val autocompleteSuggestions: StateFlow<List<String>> = _autocompleteSuggestions.asStateFlow()

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    val redirectPolicyMap: StateFlow<Map<String, String>> = repository.redirectPolicyMapFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    val persistentShortcuts: StateFlow<List<IndianBrand>> = repository.customShortcutsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val themeMode: StateFlow<ThemeMode> = repository.themeModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.SYSTEM
    )

    val searchEngine: StateFlow<SearchEngine> = repository.searchEngineFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchEngine.GOOGLE
    )

    val searchEngineChosen: StateFlow<Boolean> = repository.searchEngineChosenFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val desktopMode: StateFlow<Boolean> = repository.desktopModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val javascriptEnabled: StateFlow<Boolean> = repository.javascriptEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val indianLanguage: StateFlow<IndianLanguage> = repository.indianLanguageFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IndianLanguage.ENGLISH
    )

    val isPrivateMode: StateFlow<Boolean> = repository.privateModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val antiTrackingEnabled: StateFlow<Boolean> = repository.antiTrackingFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val httpsOnlyEnabled: StateFlow<Boolean> = repository.httpsOnlyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val eulaAccepted: StateFlow<Boolean> = repository.eulaAcceptedFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val dataSaverEnabled: StateFlow<Boolean> = repository.dataSaverEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val strictAdBlockEnabled: StateFlow<Boolean> = repository.strictAdBlockEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val youtubeAdBlockEnabled: StateFlow<Boolean> = repository.youtubeAdBlockEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val youtubeEmbeddedAdBlockEnabled: StateFlow<Boolean> = repository.youtubeEmbeddedAdBlockEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val removeAnnotationsEnabled: StateFlow<Boolean> = repository.removeAnnotationsEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val unrestrictedSocialModeEnabled: StateFlow<Boolean> = repository.unrestrictedSocialModeEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val virusTotalEnabled: StateFlow<Boolean> = repository.virusTotalEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val autoUpdateExtensionsEnabled: StateFlow<Boolean> = repository.autoUpdateExtensionsEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val cookieAutoDestructDuration: StateFlow<CookieAutoDestructDuration> = repository.cookieAutoDestructDurationFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CookieAutoDestructDuration.OFF
    )

    val startupMode: StateFlow<StartupMode> = repository.startupModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StartupMode.OPEN_NEW_TAB
    )

    val customStartupUrl: StateFlow<String> = repository.customStartupUrlFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "https://www.google.co.in"
    )

    val showTrendingNews: StateFlow<Boolean> = repository.showTrendingNewsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val selectedNewsTopics: StateFlow<Set<String>> = repository.selectedNewsTopicsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = repository.defaultNewsTopics
    )

    val isNewsTopicSetupCompleted: StateFlow<Boolean> = repository.isNewsTopicSetupCompletedFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val isAutofillEnabled: StateFlow<Boolean> = repository.isAutofillEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val isAdultContentBlocked: StateFlow<Boolean> = repository.isAdultContentBlockedFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val blockedAdsCount: StateFlow<Int> = repository.blockedAdsCountFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val points: StateFlow<Int> = pointsRepository.pointsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 100
    )

    val activeUntilTimestamp: StateFlow<Long> = pointsRepository.activeUntilTimestampFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    val pointsHistory: StateFlow<List<PointsActivityTransaction>> = pointsRepository.pointsHistoryFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val cooldownEndMap: StateFlow<Map<Int, Long>> = pointsRepository.cooldownEndMapFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    val dailyClaimCount: StateFlow<Int> = pointsRepository.dailyClaimCountFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val lastRedeemedHours: StateFlow<Int> = pointsRepository.lastRedeemedHoursFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val lastDailyClaimDate: StateFlow<String> = pointsRepository.lastDailyClaimDateFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val streakDay: StateFlow<Int> = pointsRepository.streakDayFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1
    )

    val puzzlePieces: StateFlow<Int> = pointsRepository.puzzlePiecesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val completedPuzzles: StateFlow<Int> = pointsRepository.completedPuzzlesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val dailyAiQueriesCount: StateFlow<Int> = pointsRepository.dailyAiQueriesCountFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val dailyNewsCount: StateFlow<Int> = pointsRepository.dailyNewsCountFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val dailyBrowsingMins: StateFlow<Int> = pointsRepository.dailyBrowsingMinsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val isAppTourDone: StateFlow<Boolean> = pointsRepository.isAppTourDoneFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val isDefaultBrowserClaimed: StateFlow<Boolean> = pointsRepository.isDefaultBrowserClaimedFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val _isCheckingForUpdates = MutableStateFlow(false)
    val isCheckingForUpdates: StateFlow<Boolean> = _isCheckingForUpdates.asStateFlow()

    private val _updateAvailable = MutableStateFlow<Pair<String, String>?>(null)
    val updateAvailable: StateFlow<Pair<String, String>?> = _updateAvailable.asStateFlow()

    private val _showNoUpdateToast = MutableStateFlow(false)
    val showNoUpdateToast: StateFlow<Boolean> = _showNoUpdateToast.asStateFlow()

    init {
        refreshLiveNews()
        restoreTabSession()

        viewModelScope.launch {
            eulaAccepted.collect { accepted ->
                if (!accepted) {
                    _uiState.value = _uiState.value.copy(showEulaDialog = true)
                } else {
                    repository.searchEngineChosenFlow.collect { chosen ->
                        _uiState.value = _uiState.value.copy(showSearchEngineSelectionDialog = !chosen)
                    }
                }
            }
        }

        viewModelScope.launch {
            _uiState
                .map { it.searchQuery }
                .debounce(250)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.length >= 2) {
                        val live = SearchSuggestionRepository.fetchSuggestions(query)
                        _autocompleteSuggestions.value = live
                    } else if (query.isBlank()) {
                        _autocompleteSuggestions.value = SearchSuggestionRepository.trendingTags
                    }
                }
        }
    }

    fun onTabProcessTerminated(tabId: String, didCrash: Boolean, reason: String) {
        val targetId = if (tabId.isBlank()) _activeTabId.value else tabId
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == targetId) tab.copy(isCrashed = true) else tab
        }
    }

    fun recoverAndReloadTab(tabId: String, webView: WebView?) {
        val targetId = if (tabId.isBlank()) _activeTabId.value else tabId
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == targetId) tab.copy(isCrashed = false) else tab
        }
        val currentTab = _tabs.value.find { it.id == targetId }
        if (currentTab != null && currentTab.url.isNotBlank()) {
            webView?.loadUrl(currentTab.url)
        }
    }

    private fun restoreTabSession() {
        viewModelScope.launch {
            if (_tabs.value.size == 1 && _tabs.value.first().isAtHome && _tabs.value.first().url.isBlank()) {
                val (restoredList, activeId) = tabManager.restoreTabsSession()
                if (restoredList.isNotEmpty()) {
                    _tabs.value = restoredList.map {
                        WebTab(
                            id = it.id,
                            url = it.url,
                            title = it.title,
                            isAtHome = it.url.isBlank() || it.url == "swara://start",
                            isPrivate = it.isIncognito,
                            isCrashed = it.isCrashed
                        )
                    }
                    _activeTabId.value = activeId
                    val targetTab = _tabs.value.find { it.id == activeId }
                    if (targetTab != null) {
                        _uiState.value = _uiState.value.copy(
                            currentUrl = targetTab.url,
                            searchQuery = targetTab.url,
                            pageTitle = targetTab.title,
                            isAtHome = targetTab.isAtHome
                        )
                    }
                }
            }
        }
    }

    fun saveRedirectPolicy(domain: String, policy: String) {
        viewModelScope.launch {
            repository.saveRedirectPolicy(domain, policy)
        }
    }

    fun toggleTabAudioMute(tabId: String, webView: WebView?) {
        val currentId = _activeTabId.value
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == tabId) {
                val newMuted = !tab.isMuted
                if (tabId == currentId && webView != null) {
                    val js = "document.querySelectorAll('video, audio').forEach(function(m){ m.muted = $newMuted; });"
                    webView.evaluateJavascript(js, null)
                }
                tab.copy(isMuted = newMuted)
            } else {
                tab
            }
        }
    }

    fun addCustomShortcut(name: String, url: String) {
        viewModelScope.launch {
            repository.addCustomShortcut(name, url)
        }
    }

    fun removeCustomShortcut(id: String) {
        viewModelScope.launch {
            repository.removeCustomShortcut(id)
        }
    }

    fun reloadPage(webView: WebView?) {
        if (_uiState.value.isAtHome) {
            refreshLiveNews()
        } else {
            webView?.reload()
        }
    }

    fun redeemProtectionPackageWithRules(context: Context, hours: Int, pointCost: Int) {
        viewModelScope.launch {
            val result = pointsRepository.redeemProtectionPackageWithRules(hours, pointCost)
            val msg = when (result) {
                "SUCCESS" -> "🛡️ Activated ${if (hours == 0) "10m" else "${hours}h"} AstraVault Shield!"
                "SHIELD_ALREADY_ACTIVE" -> "🛡️ Shield currently active! Wait until current shield ends."
                "TIER_COOLDOWN_ACTIVE" -> "⏳ Cooldown active for this package! Please wait."
                "MAX_DAILY_CLAIMS" -> "⚠️ Maximum 4 package claims per day reached. Resets at midnight."
                "INSUFFICIENT_POINTS" -> "❌ Insufficient points balance."
                else -> "Failed to redeem package."
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun refreshLiveNews() {
        viewModelScope.launch {
            val live = TrendingNewsEngine.fetchLiveRssNews()
            _liveNewsList.value = live
            if (live.isNotEmpty()) {
                NewsNotificationManager.postNewsNotification(getApplication(), live.first())
            }
        }
    }

    fun setAdultContentBlocked(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAdultContentBlocked(enabled)
        }
    }

    fun openDownloadsScreen() {
        _uiState.value = _uiState.value.copy(showDownloadsScreen = true)
    }

    fun closeDownloadsScreen() {
        _uiState.value = _uiState.value.copy(showDownloadsScreen = false)
    }

    fun onVideoDetected(url: String) {
        _detectedVideoUrl.value = url
    }

    fun downloadDetectedVideo(context: Context) {
        val videoUrl = detectedVideoUrl.value ?: return
        val pageTitle = uiState.value.pageTitle
        val cleanTitle = pageTitle.take(25).replace("[^a-zA-Z0-9_]".toRegex(), "_")
        val fileName = "${cleanTitle}_${System.currentTimeMillis()}.mp4"
        val currentUrl = uiState.value.currentUrl
        try {
            val cookies = CookieManager.getInstance().getCookie(videoUrl)
            val userAgent = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

            val request = DownloadManager.Request(videoUrl.toUri()).apply {
                setMimeType("video/mp4")
                if (!cookies.isNullOrEmpty()) addRequestHeader("Cookie", cookies)
                addRequestHeader("User-Agent", userAgent)
                if (currentUrl.isNotBlank()) addRequestHeader("Referer", currentUrl)
                setTitle("Video: $fileName")
                setDescription("Direct media stream download via Swara Browser...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, fileName)
            }
            val dm = contextSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = dm.enqueue(request)
            SwaraDownloadTracker.recordDownload(
                context = context,
                downloadId = downloadId,
                fileName = fileName,
                mimeType = "video/mp4",
                url = videoUrl
            )
            Toast.makeText(context, "🎬 Video stream download started: $fileName", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun contextSystemService(name: String): Any {
        return getApplication<Application>().getSystemService(name)
    }

    fun setAutofillEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAutofillEnabled(enabled)
        }
    }

    fun claimAppTourBonus() {
        viewModelScope.launch {
            pointsRepository.claimAppTourBonus()
        }
    }

    fun claimDefaultBrowserBonus() {
        viewModelScope.launch {
            pointsRepository.claimDefaultBrowserBonus()
        }
    }

    fun saveNewsTopics(topics: Set<String>) {
        viewModelScope.launch {
            repository.updateSelectedNewsTopics(topics)
            repository.completeNewsTopicSetup()
        }
    }

    fun openTabManagerDialog() {
        _uiState.value = _uiState.value.copy(showTabManagerDialog = true)
    }

    fun closeTabManagerDialog() {
        _uiState.value = _uiState.value.copy(showTabManagerDialog = false)
    }

    fun selectTab(tabId: String) {
        val targetTab = _tabs.value.find { it.id == tabId } ?: return
        _activeTabId.value = tabId
        _detectedVideoUrl.value = null
        _uiState.value = _uiState.value.copy(
            currentUrl = targetTab.url,
            searchQuery = targetTab.url,
            pageTitle = targetTab.title,
            isAtHome = targetTab.isAtHome,
            showTabManagerDialog = false
        )
        tabManager.onTabSelected(tabId, targetTab.isPrivate)
    }

    fun closeTab(tabId: String) {
        val currentList = _tabs.value
        if (currentList.size <= 1) {
            newTab()
            return
        }
        val newList = currentList.filter { it.id != tabId }
        _tabs.value = newList
        tabManager.onTabClosed(tabId)
        if (_activeTabId.value == tabId) {
            selectTab(newList.last().id)
        }
    }

    fun newTab() {
        val isPrivate = isPrivateMode.value
        val tab = WebTab(id = "tab_${System.currentTimeMillis()}", isPrivate = isPrivate)
        _tabs.value = _tabs.value + tab
        selectTab(tab.id)
    }

    fun closeAllTabs() {
        val isPrivate = isPrivateMode.value
        val tab = WebTab(id = "tab_${System.currentTimeMillis()}", isPrivate = isPrivate)
        _tabs.value = listOf(tab)
        selectTab(tab.id)
    }

    fun updateCurrentTabInfo(url: String, title: String, isHome: Boolean) {
        val currentId = _activeTabId.value
        _tabs.value = _tabs.value.mapIndexed { index, tab ->
            if (tab.id == currentId) {
                val updated = tab.copy(url = url, title = title, isAtHome = isHome)
                tabManager.onTabUpdated(
                    TabModel(
                        id = updated.id,
                        url = updated.url,
                        title = updated.title,
                        isIncognito = updated.isPrivate,
                        isCrashed = updated.isCrashed
                    ),
                    index
                )
                updated
            } else {
                tab
            }
        }
    }

    fun openExternalUrl(url: String) {
        if (url.isNotBlank()) {
            viewModelScope.launch {
                val resolvedUrl = TrendingNewsEngine.resolveCanonicalNewsUrl(url)
                _detectedVideoUrl.value = null
                _uiState.value = _uiState.value.copy(
                    currentUrl = resolvedUrl,
                    searchQuery = resolvedUrl,
                    isLoading = true,
                    isAtHome = false
                )
                updateCurrentTabInfo(resolvedUrl, "Loading...", false)
            }
        }
    }

    fun openBrandUrl(url: String) {
        openExternalUrl(url)
    }

    fun toggleShowTrendingNews() {
        viewModelScope.launch {
            repository.setShowTrendingNews(!showTrendingNews.value)
        }
    }

    fun acceptEula() {
        viewModelScope.launch {
            repository.setEulaAccepted(true)
            _uiState.value = _uiState.value.copy(showEulaDialog = false)
        }
    }

    fun confirmSearchEngineChoice(engine: SearchEngine) {
        viewModelScope.launch {
            repository.setSearchEngine(engine)
            _uiState.value = _uiState.value.copy(showSearchEngineSelectionDialog = false)
        }
    }

    fun openEulaDialog() {
        _uiState.value = _uiState.value.copy(showEulaDialog = true)
    }

    fun closeEulaDialog() {
        if (eulaAccepted.value) {
            _uiState.value = _uiState.value.copy(showEulaDialog = false)
        }
    }

    fun openExtensionManagerDialog() {
        _uiState.value = _uiState.value.copy(showExtensionManagerDialog = true)
    }

    fun closeExtensionManagerDialog() {
        _uiState.value = _uiState.value.copy(showExtensionManagerDialog = false)
    }

    fun canClaimDailyBonus(): Boolean {
        return lastDailyClaimDate.value != pointsRepository.getTodayString()
    }

    fun claimDailyBonus() {
        viewModelScope.launch {
            pointsRepository.claimDailyBonus()
        }
    }

    fun redeemProtectionPackage(hours: Int, pointCost: Int) {
        viewModelScope.launch {
            pointsRepository.redeemProtectionPackageWithRules(hours, pointCost)
        }
    }

    fun openAstraRewardDialog() {
        _uiState.value = _uiState.value.copy(showAstraRewardDialog = true)
    }

    fun closeAstraRewardDialog() {
        _uiState.value = _uiState.value.copy(showAstraRewardDialog = false)
    }

    fun checkForUpdates(currentVersionName: String) {
        viewModelScope.launch {
            _isCheckingForUpdates.value = true
            val latestRelease = UpdateCheckerEngine.getLatestRelease()
            _isCheckingForUpdates.value = false

            if (latestRelease != null) {
                val latestTag = latestRelease.first.removePrefix("v")
                val currentTag = currentVersionName.removePrefix("v")

                if (isNewerVersion(latestTag, currentTag)) {
                    _updateAvailable.value = latestRelease
                } else {
                    _showNoUpdateToast.value = true
                }
            } else {
                _showNoUpdateToast.value = true
            }
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val length = max(latestParts.size, currentParts.size)
        for (i in 0 until length) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    fun dismissUpdateDialog() {
        _updateAvailable.value = null
    }

    fun resetNoUpdateToast() {
        _showNoUpdateToast.value = false
    }

    fun triggerEmergencySelfDestruct(webView: WebView? = null) {
        viewModelScope.launch {
            selfDestructManager.executeSelfDestructWipe(webView)
            _uiState.value = BrowserUiState()
            closeAllTabs()
        }
    }

    fun onAdBlocked() {
        viewModelScope.launch {
            repository.incrementBlockedAdsCount()
        }
    }

    fun openDefaultBrowserSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun submitSearch(input: String) {
        if (input.isBlank()) {
            onHomeClicked()
            return
        }
        val targetUrl = searchEngine.value.buildUrl(input)
        openExternalUrl(targetUrl)
    }

    fun onPageStarted(url: String) {
        val isHttps = url.startsWith("https://", ignoreCase = true)
        _detectedVideoUrl.value = null
        _uiState.value = _uiState.value.copy(
            currentUrl = url,
            searchQuery = url,
            isLoading = true,
            isSecure = isHttps,
            isAtHome = false
        )
        updateCurrentTabInfo(url, "Loading...", false)
    }

    fun onPageFinished(url: String) {
        val isHttps = url.startsWith("https://", ignoreCase = true)
        _uiState.value = _uiState.value.copy(
            currentUrl = url,
            isLoading = false,
            progress = 100,
            isSecure = isHttps
        )
        updateCurrentTabInfo(url, _uiState.value.pageTitle, false)
        viewModelScope.launch {
            repository.setLastVisitedUrl(url)
            pointsRepository.recordNewsRead()
        }
    }

    fun onProgressChanged(progress: Int) {
        _uiState.value = _uiState.value.copy(
            progress = progress,
            isLoading = progress < 100
        )
    }

    fun onTitleReceived(title: String) {
        if (title.isNotBlank()) {
            _uiState.value = _uiState.value.copy(pageTitle = title)
            updateCurrentTabInfo(_uiState.value.currentUrl, title, _uiState.value.isAtHome)
        }
    }

    fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.value = _uiState.value.copy(
            canGoBack = canGoBack,
            canGoForward = canGoForward
        )
    }

    fun onHomeClicked() {
        _detectedVideoUrl.value = null
        _uiState.value = _uiState.value.copy(
            currentUrl = "",
            searchQuery = "",
            isAtHome = true,
            isLoading = false
        )
        updateCurrentTabInfo("", "New Tab Page", true)
    }

    fun openSettingsDialog() {
        _uiState.value = _uiState.value.copy(showSettingsDialog = true)
    }

    fun closeSettingsDialog() {
        _uiState.value = _uiState.value.copy(showSettingsDialog = false)
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setSearchEngine(engine: SearchEngine) {
        viewModelScope.launch {
            repository.setSearchEngine(engine)
        }
    }

    fun setDesktopMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDesktopMode(enabled)
        }
    }

    fun setJavascriptEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setJavascriptEnabled(enabled)
        }
    }

    fun setIndianLanguage(language: IndianLanguage) {
        viewModelScope.launch {
            repository.setIndianLanguage(language)
        }
    }

    fun setPrivateMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setPrivateMode(enabled)
        }
    }

    fun setAntiTracking(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAntiTracking(enabled)
        }
    }

    fun setHttpsOnly(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHttpsOnly(enabled)
        }
    }

    fun setDataSaverEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDataSaverEnabled(enabled)
        }
    }

    fun setStrictAdBlockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setStrictAdBlockEnabled(enabled)
        }
    }

    fun setYoutubeAdBlockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setYoutubeAdBlockEnabled(enabled)
        }
    }

    fun setYoutubeEmbeddedAdBlockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setYoutubeEmbeddedAdBlockEnabled(enabled)
        }
    }

    fun setRemoveAnnotationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setRemoveAnnotationsEnabled(enabled)
        }
    }

    fun setUnrestrictedSocialModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setUnrestrictedSocialModeEnabled(enabled)
        }
    }

    fun setVirusTotalEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setVirusTotalEnabled(enabled)
        }
    }

    fun setAutoUpdateExtensionsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAutoUpdateExtensionsEnabled(enabled)
        }
    }

    fun setCookieAutoDestructDuration(duration: CookieAutoDestructDuration) {
        viewModelScope.launch {
            repository.setCookieAutoDestructDuration(duration)
        }
    }

    fun setStartupMode(mode: StartupMode) {
        viewModelScope.launch {
            repository.setStartupMode(mode)
        }
    }

    fun setCustomStartupUrl(url: String) {
        viewModelScope.launch {
            repository.setCustomStartupUrl(url)
        }
    }
}
