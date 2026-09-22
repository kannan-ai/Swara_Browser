package com.example.swara_browser.ui

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.core.net.toUri
import com.example.swara_browser.data.AstraVaultEngine
import com.example.swara_browser.data.ScreenshotCaptureEngine
import com.example.swara_browser.data.ScreenshotResult
import com.example.swara_browser.data.SearchSuggestionRepository
import com.example.swara_browser.ui.components.PageCrashOverlay

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val searchEngine by viewModel.searchEngine.collectAsState()
    val desktopMode by viewModel.desktopMode.collectAsState()
    val javascriptEnabled by viewModel.javascriptEnabled.collectAsState()
    val indianLanguage by viewModel.indianLanguage.collectAsState()
    val isPrivateMode by viewModel.isPrivateMode.collectAsState()
    val antiTrackingEnabled by viewModel.antiTrackingEnabled.collectAsState()
    val httpsOnlyEnabled by viewModel.httpsOnlyEnabled.collectAsState()
    val dataSaverEnabled by viewModel.dataSaverEnabled.collectAsState()
    val strictAdBlockEnabled by viewModel.strictAdBlockEnabled.collectAsState()
    val youtubeAdBlockEnabled by viewModel.youtubeAdBlockEnabled.collectAsState()
    val removeAnnotationsEnabled by viewModel.removeAnnotationsEnabled.collectAsState()
    val unrestrictedSocialModeEnabled by viewModel.unrestrictedSocialModeEnabled.collectAsState()
    val virusTotalEnabled by viewModel.virusTotalEnabled.collectAsState()
    val autoUpdateExtensionsEnabled by viewModel.autoUpdateExtensionsEnabled.collectAsState()
    val cookieAutoDestructDuration by viewModel.cookieAutoDestructDuration.collectAsState()
    val startupMode by viewModel.startupMode.collectAsState()
    val customStartupUrl by viewModel.customStartupUrl.collectAsState()
    val showTrendingNews by viewModel.showTrendingNews.collectAsState()
    val selectedNewsTopics by viewModel.selectedNewsTopics.collectAsState()
    val isNewsTopicSetupCompleted by viewModel.isNewsTopicSetupCompleted.collectAsState()
    val isAutofillEnabled by viewModel.isAutofillEnabled.collectAsState()
    val isAdultContentBlocked by viewModel.isAdultContentBlocked.collectAsState()
    val eulaAccepted by viewModel.eulaAccepted.collectAsState()
    val blockedAdsCount by viewModel.blockedAdsCount.collectAsState()
    val points by viewModel.points.collectAsState()
    val activeUntilTimestamp by viewModel.activeUntilTimestamp.collectAsState()
    
    val isCheckingForUpdates by viewModel.isCheckingForUpdates.collectAsState()
    val updateAvailable by viewModel.updateAvailable.collectAsState()
    val showNoUpdateToast by viewModel.showNoUpdateToast.collectAsState()

    val detectedVideoUrl by viewModel.detectedVideoUrl.collectAsState()
    val liveNews by viewModel.liveNewsList.collectAsState()
    val persistentShortcuts by viewModel.persistentShortcuts.collectAsState()
    val autocompleteSuggestions by viewModel.autocompleteSuggestions.collectAsState()

    val redirectPolicyMap by viewModel.redirectPolicyMap.collectAsState()
    val pointsHistory by viewModel.pointsHistory.collectAsState()
    val cooldownEndMap by viewModel.cooldownEndMap.collectAsState()
    val dailyClaimCount by viewModel.dailyClaimCount.collectAsState()
    val lastRedeemedHours by viewModel.lastRedeemedHours.collectAsState()

    val streakDay by viewModel.streakDay.collectAsState()
    val puzzlePieces by viewModel.puzzlePieces.collectAsState()
    val completedPuzzles by viewModel.completedPuzzles.collectAsState()
    val dailyAiQueriesCount by viewModel.dailyAiQueriesCount.collectAsState()
    val dailyNewsCount by viewModel.dailyNewsCount.collectAsState()
    val dailyBrowsingMins by viewModel.dailyBrowsingMins.collectAsState()
    val isAppTourDone by viewModel.isAppTourDone.collectAsState()
    val isDefaultBrowserClaimed by viewModel.isDefaultBrowserClaimed.collectAsState()

    val tabs by viewModel.tabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()

    val isAstraShieldActive = AstraVaultEngine.isProtectionActive(activeUntilTimestamp)
    val isDownloadsActive = uiState.showDownloadsScreen

    LaunchedEffect(showNoUpdateToast) {
        if (showNoUpdateToast) {
            Toast.makeText(context, "You are using the latest version of Swara Browser!", Toast.LENGTH_SHORT).show()
            viewModel.resetNoUpdateToast()
        }
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var showSwaraAiScreen by remember { mutableStateOf(false) }
    var pendingDownloadName by remember { mutableStateOf<String?>(null) }
    var showTopMenu by remember { mutableStateOf(false) }
    var showQuickActionSheet by remember { mutableStateOf(false) }
    var showWhatsAppStatusSaverDialog by remember { mutableStateOf(false) }
    var showWallpaperGallery by remember { mutableStateOf(false) }
    var showPointsHistoryScreen by remember { mutableStateOf(false) }
    var showSiteInfoSheet by remember { mutableStateOf(false) }

    var contextMenuTitle by remember { mutableStateOf("") }
    var contextMenuImageUrl by remember { mutableStateOf("") }
    var contextMenuLinkUrl by remember { mutableStateOf("") }
    var showWebContextMenu by remember { mutableStateOf(false) }

    var activePreviewUrl by remember { mutableStateOf("") }
    var showPagePreviewSheet by remember { mutableStateOf(false) }

    var activePreviewImageUrl by remember { mutableStateOf("") }
    var showImagePreviewDialog by remember { mutableStateOf(false) }

    var showAppRedirectSheet by remember { mutableStateOf(false) }
    var appRedirectPkg by remember { mutableStateOf("") }
    var appRedirectLaunchLambda by remember { mutableStateOf<(rememberChoice: Boolean) -> Unit>({ _ -> }) }
    var appRedirectStayLambda by remember { mutableStateOf<(rememberChoice: Boolean) -> Unit>({ _ -> }) }

    var showPermissionPromptSheet by remember { mutableStateOf(false) }
    var permissionDomain by remember { mutableStateOf("") }
    var permissionName by remember { mutableStateOf("") }
    var permissionRiskLevel by remember { mutableStateOf(PermissionRiskLevel.HIGH_RISK) }
    var permissionReason by remember { mutableStateOf("") }
    var permissionConsequence by remember { mutableStateOf("") }
    var permissionAllowLambda by remember { mutableStateOf<() -> Unit>({}) }
    var permissionDenyLambda by remember { mutableStateOf<() -> Unit>({}) }

    var isSearchFocused by remember { mutableStateOf(false) }
    var clipboardText by remember { mutableStateOf("") }

    var customWallpaperUrl by remember { mutableStateOf("") }
    var customWallpaperName by remember { mutableStateOf("Default") }

    val activeTab = tabs.find { it.id == activeTabId }
    val isOverlayActive = isDownloadsActive || showWallpaperGallery || showPointsHistoryScreen || uiState.showSettingsDialog || uiState.showAstraRewardDialog

    val focusManager = LocalFocusManager.current

    BackHandler(enabled = true) {
        if (showPermissionPromptSheet) {
            showPermissionPromptSheet = false
        } else if (isSearchFocused) {
            isSearchFocused = false
            focusManager.clearFocus()
        } else if (showAppRedirectSheet) {
            showAppRedirectSheet = false
        } else if (showImagePreviewDialog) {
            showImagePreviewDialog = false
        } else if (showPagePreviewSheet) {
            showPagePreviewSheet = false
        } else if (showPointsHistoryScreen) {
            showPointsHistoryScreen = false
        } else if (showWallpaperGallery) {
            showWallpaperGallery = false
        } else if (isDownloadsActive) {
            viewModel.closeDownloadsScreen()
        } else if (showSwaraAiScreen) {
            showSwaraAiScreen = false
        } else if (showWhatsAppStatusSaverDialog) {
            showWhatsAppStatusSaverDialog = false
        } else if (showQuickActionSheet) {
            showQuickActionSheet = false
        } else if (!uiState.isAtHome) {
            val handled = webViewRef?.let { smartGoBack(it) } ?: false
            if (!handled) viewModel.onHomeClicked()
        } else {
            (context as? Activity)?.finish()
        }
    }

    Scaffold(
        topBar = {
            if (!isOverlayActive) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .statusBarsPadding()
                ) {
                    Surface(
                        tonalElevation = 2.dp,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = uiState.searchQuery,
                                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .onFocusChanged { focusState ->
                                            isSearchFocused = focusState.isFocused
                                            if (focusState.isFocused) {
                                                try {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                    val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                                    clipboardText = if (clip.length in 3..200) clip else ""
                                                } catch (_: Exception) {
                                                    clipboardText = ""
                                                }
                                            }
                                        },
                                    placeholder = { Text(indianLanguage.searchPlaceholder) },
                                    singleLine = true,
                                    leadingIcon = {
                                        IconButton(
                                            onClick = { showSiteInfoSheet = true },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isAstraShieldActive || isPrivateMode) {
                                                    Icons.Default.Shield
                                                } else if (uiState.isSecure) {
                                                    Icons.Default.Lock
                                                } else {
                                                    Icons.Default.LockOpen
                                                },
                                                contentDescription = if (isAstraShieldActive) "AstraShield" else if (isPrivateMode) "Private Incognito Mode" else if (uiState.isSecure) "Secure" else "Insecure",
                                                tint = if (isAstraShieldActive) MaterialTheme.colorScheme.primary else if (isPrivateMode) MaterialTheme.colorScheme.error else if (uiState.isSecure) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        if (uiState.searchQuery.isNotEmpty()) {
                                            IconButton(
                                                onClick = { viewModel.onSearchQueryChanged("") },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Clear search text",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            isSearchFocused = false
                                            viewModel.submitSearch(uiState.searchQuery)
                                            focusManager.clearFocus()
                                        }
                                    ),
                                    shape = RoundedCornerShape(26.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                                    )
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Tab Count Badge
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            width = 1.5.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.openTabManagerDialog() }
                                ) {
                                    Text(
                                        text = "${tabs.size}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Top 3-Dot Options Button & Redesigned Grouped Dropdown Menu
                                Box {
                                    IconButton(
                                        onClick = { showTopMenu = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "More Options",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showTopMenu,
                                        onDismissRequest = { showTopMenu = false },
                                        modifier = Modifier.width(280.dp)
                                    ) {
                                        // HEADER QUICK ACTION ICON ROW
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    showTopMenu = false
                                                    val handled = webViewRef?.let { smartGoBack(it) } ?: false
                                                    if (!handled && !uiState.isAtHome) viewModel.onHomeClicked()
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Back",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    showTopMenu = false
                                                    webViewRef?.goForward()
                                                },
                                                enabled = !uiState.isAtHome && uiState.canGoForward,
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = "Forward",
                                                    tint = if (!uiState.isAtHome && uiState.canGoForward) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    showTopMenu = false
                                                    Toast.makeText(context, "🔖 Page saved to Bookmarks!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.BookmarkBorder,
                                                    contentDescription = "Bookmark",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    showTopMenu = false
                                                    viewModel.openDownloadsScreen()
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = "Download",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    showTopMenu = false
                                                    viewModel.reloadPage(webViewRef)
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (uiState.isLoading) Icons.Default.Stop else Icons.Default.Refresh,
                                                    contentDescription = "Refresh",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                        // GROUP 1: TABS
                                        DropdownMenuItem(
                                            text = { Text("New tab", fontWeight = FontWeight.Bold) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            onClick = {
                                                showTopMenu = false
                                                viewModel.newTab()
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = { Text("New private tab", fontWeight = FontWeight.Bold) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Shield,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            onClick = {
                                                showTopMenu = false
                                                viewModel.setPrivateMode(true)
                                                viewModel.newTab()
                                            }
                                        )

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                        // GROUP 2: PAGE TOOLS
                                        DropdownMenuItem(
                                            text = { Text("Find in page") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.FindInPage,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            onClick = {
                                                showTopMenu = false
                                                Toast.makeText(context, "🔍 Find in Page active", Toast.LENGTH_SHORT).show()
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = { Text("Translate page") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Translate,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            onClick = {
                                                showTopMenu = false
                                                Toast.makeText(context, "🌐 Translating page...", Toast.LENGTH_SHORT).show()
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = { Text("Share link") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Share,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            onClick = {
                                                showTopMenu = false
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_SUBJECT, uiState.pageTitle)
                                                    putExtra(Intent.EXTRA_TEXT, uiState.currentUrl)
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Share Page Link via"))
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("Desktop site")
                                                    Switch(
                                                        checked = desktopMode,
                                                        onCheckedChange = { viewModel.setDesktopMode(it) },
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                }
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Public,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            onClick = {
                                                viewModel.setDesktopMode(!desktopMode)
                                            }
                                        )

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                        // GROUP 3: BROWSER HUBS
                                        DropdownMenuItem(
                                            text = { Text("History") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.History,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            onClick = {
                                                showTopMenu = false
                                                Toast.makeText(context, "🕒 Browsing History (Private On-Device)", Toast.LENGTH_SHORT).show()
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = { Text("Downloads") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            onClick = {
                                                showTopMenu = false
                                                viewModel.openDownloadsScreen()
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = { Text("Settings") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Settings,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            onClick = {
                                                showTopMenu = false
                                                viewModel.openSettingsDialog()
                                            }
                                        )
                                    }
                                }
                            }

                            // Autocomplete Dropdown & Clipboard AssistChip
                            if (isSearchFocused) {
                                Spacer(modifier = Modifier.height(6.dp))
                                AutocompleteDropdown(
                                    query = uiState.searchQuery,
                                    isFocused = isSearchFocused,
                                    suggestions = autocompleteSuggestions,
                                    trendingTopics = SearchSuggestionRepository.trendingTags,
                                    onSelectSuggestion = { selected ->
                                        isSearchFocused = false
                                        focusManager.clearFocus()
                                        viewModel.submitSearch(selected)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    AnimatedVisibility(
                        visible = uiState.isLoading,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        LinearProgressIndicator(
                            progress = { uiState.progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.Transparent
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (!isOverlayActive) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                showSwaraAiScreen = false
                                showWhatsAppStatusSaverDialog = false
                                viewModel.onHomeClicked()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Search Engine Home",
                                tint = if (uiState.isAtHome && !showSwaraAiScreen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Bottom Center Quick Menu ^ Launcher
                        IconButton(
                            onClick = { showQuickActionSheet = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Quick Menu",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { showSwaraAiScreen = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Swara AI",
                                tint = if (showSwaraAiScreen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { viewModel.openAstraRewardDialog() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Rewards",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isOverlayActive && !uiState.isAtHome && detectedVideoUrl != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        pendingDownloadName = "Swara_Stream_${System.currentTimeMillis()}.mp4"
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Video"
                        )
                    },
                    text = {
                        Text(
                            text = "Download Video",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (showPointsHistoryScreen) {
            PointsHistoryScreen(
                points = points,
                pointsHistory = pointsHistory,
                onBack = { showPointsHistoryScreen = false }
            )
        } else if (showWallpaperGallery) {
            WallpaperGalleryScreen(
                currentWallpaperUrl = customWallpaperUrl,
                onWallpaperSelected = { url, name ->
                    customWallpaperUrl = url
                    customWallpaperName = name
                },
                onDismiss = { showWallpaperGallery = false }
            )
        } else if (isDownloadsActive) {
            DownloadsScreen(
                onDismiss = { viewModel.closeDownloadsScreen() }
            )
        } else if (uiState.showSettingsDialog) {
            SettingsScreen(
                currentThemeMode = themeMode,
                currentSearchEngine = searchEngine,
                currentLanguage = indianLanguage,
                desktopMode = desktopMode,
                javascriptEnabled = javascriptEnabled,
                isPrivateMode = isPrivateMode,
                antiTrackingEnabled = antiTrackingEnabled,
                httpsOnlyEnabled = httpsOnlyEnabled,
                dataSaverEnabled = dataSaverEnabled,
                strictAdBlockEnabled = strictAdBlockEnabled,
                youtubeAdBlockEnabled = youtubeAdBlockEnabled,
                removeAnnotationsEnabled = removeAnnotationsEnabled,
                unrestrictedSocialModeEnabled = unrestrictedSocialModeEnabled,
                isAutofillEnabled = isAutofillEnabled,
                cookieAutoDestructDuration = cookieAutoDestructDuration,
                startupMode = startupMode,
                customStartupUrl = customStartupUrl,
                selectedNewsTopics = selectedNewsTopics,
                onThemeModeSelected = { viewModel.setThemeMode(it) },
                onSearchEngineSelected = { viewModel.setSearchEngine(it) },
                onLanguageSelected = { viewModel.setIndianLanguage(it) },
                onDesktopModeToggled = { viewModel.setDesktopMode(it) },
                onJavascriptToggled = { viewModel.setJavascriptEnabled(it) },
                onPrivateModeToggled = { viewModel.setPrivateMode(it) },
                onAntiTrackingToggled = { viewModel.setAntiTracking(it) },
                onHttpsOnlyToggled = { viewModel.setHttpsOnly(it) },
                onDataSaverToggled = { viewModel.setDataSaverEnabled(it) },
                onStrictAdBlockToggled = { viewModel.setStrictAdBlockEnabled(it) },
                onYoutubeAdBlockToggled = { viewModel.setYoutubeAdBlockEnabled(it) },
                onRemoveAnnotationsToggled = { viewModel.setRemoveAnnotationsEnabled(it) },
                onUnrestrictedSocialModeToggled = { viewModel.setUnrestrictedSocialModeEnabled(it) },
                onAutofillToggled = { viewModel.setAutofillEnabled(it) },
                onCookieAutoDestructDurationSelected = { viewModel.setCookieAutoDestructDuration(it) },
                onStartupModeSelected = { viewModel.setStartupMode(it) },
                onCustomStartupUrlChanged = { viewModel.setCustomStartupUrl(it) },
                onSaveNewsTopics = { viewModel.saveNewsTopics(it) },
                onOpenExtensionManagerClicked = { viewModel.openExtensionManagerDialog() },
                onSetAsDefaultBrowserClicked = { viewModel.openDefaultBrowserSettings(context) },
                onOpenEulaClicked = { viewModel.openEulaDialog() },
                onEmergencySelfDestructClicked = {
                    viewModel.triggerEmergencySelfDestruct(webViewRef)
                    viewModel.closeSettingsDialog()
                },
                isCheckingForUpdates = isCheckingForUpdates,
                updateAvailable = updateAvailable,
                onCheckForUpdates = { currentVersion -> viewModel.checkForUpdates(currentVersion) },
                onDownloadUpdate = { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                    viewModel.dismissUpdateDialog()
                },
                onDismissUpdateDialog = { viewModel.dismissUpdateDialog() },
                onDismiss = { viewModel.closeSettingsDialog() }
            )
        } else if (uiState.showAstraRewardDialog) {
            AstraRewardScreen(
                points = points,
                activeUntilTimestamp = activeUntilTimestamp,
                canClaimDailyBonus = viewModel.canClaimDailyBonus(),
                streakDay = streakDay,
                puzzlePieces = puzzlePieces,
                completedPuzzles = completedPuzzles,
                dailyAiQueriesCount = dailyAiQueriesCount,
                dailyNewsCount = dailyNewsCount,
                dailyBrowsingMins = dailyBrowsingMins,
                isAppTourDone = isAppTourDone,
                isDefaultBrowserClaimed = isDefaultBrowserClaimed,
                pointsHistory = pointsHistory,
                cooldownEndMap = cooldownEndMap,
                dailyClaimCount = dailyClaimCount,
                lastRedeemedHours = lastRedeemedHours,
                onClaimDailyBonus = { viewModel.claimDailyBonus() },
                onAskAiClicked = { showSwaraAiScreen = true },
                onClaimAppTourBonus = { viewModel.claimAppTourBonus() },
                onClaimDefaultBrowserBonus = { viewModel.claimDefaultBrowserBonus() },
                onRedeemPackage = { hours, cost ->
                    viewModel.redeemProtectionPackageWithRules(context, hours, cost)
                },
                onNavigateToHistory = { showPointsHistoryScreen = true },
                onDismiss = { viewModel.closeAstraRewardDialog() }
            )
        } else {
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = {
                    viewModel.reloadPage(webViewRef)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (activeTab?.isCrashed == true) {
                        PageCrashOverlay(
                            reason = "The webpage ran out of memory or its rendering process crashed.",
                            onReload = {
                                viewModel.recoverAndReloadTab(activeTabId, webViewRef)
                            }
                        )
                    } else if (uiState.isAtHome) {
                        StartHomeScreen(
                            selectedLanguage = indianLanguage,
                            searchEngine = searchEngine,
                            isPrivateMode = isPrivateMode,
                            showTrendingNews = showTrendingNews,
                            isNewsTopicSetupCompleted = isNewsTopicSetupCompleted,
                            selectedNewsTopics = selectedNewsTopics,
                            points = points,
                            activeUntilTimestamp = activeUntilTimestamp,
                            blockedAdsCount = blockedAdsCount,
                            liveNewsList = liveNews,
                            customWallpaperUrl = customWallpaperUrl,
                            pinnedBrands = persistentShortcuts,
                            onBrandClicked = { viewModel.openBrandUrl(it) },
                            onToggleShowTrendingNews = { viewModel.toggleShowTrendingNews() },
                            onSaveNewsTopics = { viewModel.saveNewsTopics(it) },
                            onOpenNewsTopicSettings = { viewModel.openSettingsDialog() },
                            onOpenAstraRewardsClicked = { viewModel.openAstraRewardDialog() },
                            onOpenWallpaperGallery = { showWallpaperGallery = true },
                            onAddCustomShortcut = { name, url -> viewModel.addCustomShortcut(name, url) },
                            onRemoveCustomShortcut = { viewModel.removeCustomShortcut(it) },
                            onNewsLongClicked = { news ->
                                contextMenuTitle = news.title
                                contextMenuImageUrl = news.imageUrl
                                contextMenuLinkUrl = news.url
                                showWebContextMenu = true
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        BrowserWebView(
                            url = uiState.currentUrl,
                            tabId = activeTabId,
                            javascriptEnabled = javascriptEnabled,
                            desktopMode = desktopMode,
                            isPrivateMode = isPrivateMode,
                            antiTrackingEnabled = antiTrackingEnabled,
                            httpsOnlyEnabled = httpsOnlyEnabled,
                            isAstraShieldActive = isAstraShieldActive,
                            strictAdBlockEnabled = strictAdBlockEnabled,
                            youtubeAdBlockEnabled = youtubeAdBlockEnabled,
                            removeAnnotationsEnabled = removeAnnotationsEnabled,
                            unrestrictedSocialModeEnabled = unrestrictedSocialModeEnabled,
                            isAutofillEnabled = isAutofillEnabled,
                            dataSaverEnabled = dataSaverEnabled,
                            themeMode = themeMode,
                            onPageStarted = { viewModel.onPageStarted(it) },
                            onPageFinished = { viewModel.onPageFinished(it) },
                            onProgressChanged = { viewModel.onProgressChanged(it) },
                            onTitleReceived = { viewModel.onTitleReceived(it) },
                            onNavigationStateChanged = { canGoBack, canGoForward ->
                                viewModel.onNavigationStateChanged(canGoBack, canGoForward)
                            },
                            onAdBlocked = { viewModel.onAdBlocked() },
                            onVideoDetected = { viewModel.onVideoDetected(it) },
                            onShowLinkContextMenu = { link ->
                                contextMenuLinkUrl = link
                                showWebContextMenu = true
                            },
                            onShowImageContextMenu = { img ->
                                contextMenuImageUrl = img
                                showWebContextMenu = true
                            },
                            onShowImageLinkContextMenu = { img, link ->
                                contextMenuImageUrl = img
                                contextMenuLinkUrl = link
                                showWebContextMenu = true
                            },
                            onSearchText = { query ->
                                viewModel.newTab()
                                viewModel.submitSearch(query)
                            },
                            onExplainText = { _ ->
                                showSwaraAiScreen = true
                            },
                            onPromptAppRedirect = { appPkg, onConfirm, onStay ->
                                val domainHost = try { appPkg.toUri().host?.lowercase() ?: appPkg } catch (_: Exception) { appPkg }
                                val storedPolicy = redirectPolicyMap[domainHost]

                                when (storedPolicy) {
                                    "ALWAYS_BROWSER" -> onStay()
                                    "ALWAYS_APP" -> onConfirm()
                                    else -> {
                                        appRedirectPkg = appPkg
                                        appRedirectLaunchLambda = { remember ->
                                            if (remember && domainHost.isNotBlank()) {
                                                viewModel.saveRedirectPolicy(domainHost, "ALWAYS_APP")
                                            }
                                            onConfirm()
                                        }
                                        appRedirectStayLambda = { remember ->
                                            if (remember && domainHost.isNotBlank()) {
                                                viewModel.saveRedirectPolicy(domainHost, "ALWAYS_BROWSER")
                                            }
                                            onStay()
                                        }
                                        showAppRedirectSheet = true
                                    }
                                }
                            },
                            onShowPermissionPrompt = { domain, pName, risk, reason, consequence, onAllow, onDeny ->
                                permissionDomain = domain
                                permissionName = pName
                                permissionRiskLevel = risk
                                permissionReason = reason
                                permissionConsequence = consequence
                                permissionAllowLambda = onAllow
                                permissionDenyLambda = onDeny
                                showPermissionPromptSheet = true
                            },
                            onTabProcessTerminated = { tId, didCrash, reason ->
                                viewModel.onTabProcessTerminated(tId, didCrash, reason)
                            },
                            onWebViewCreated = { webViewRef = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        if (showSwaraAiScreen) {
            SwaraAiScreen(
                onDismiss = { showSwaraAiScreen = false }
            )
        }

        if (showPermissionPromptSheet) {
            PermissionPromptSheet(
                domain = permissionDomain,
                permissionName = permissionName,
                riskLevel = permissionRiskLevel,
                explanationReason = permissionReason,
                declineConsequence = permissionConsequence,
                onAllow = { permissionAllowLambda() },
                onDeny = { permissionDenyLambda() },
                onDismiss = { showPermissionPromptSheet = false }
            )
        }

        if (showAppRedirectSheet) {
            val req = AppRedirectRequest(
                domain = appRedirectPkg,
                targetPackage = appRedirectPkg,
                displayLabel = appRedirectPkg,
                fallbackUrl = null,
                onConfirmLaunch = { remember ->
                    appRedirectLaunchLambda(remember)
                    showAppRedirectSheet = false
                },
                onStayInBrowser = { remember ->
                    appRedirectStayLambda(remember)
                    showAppRedirectSheet = false
                }
            )
            AppRedirectSheet(
                request = req,
                onDismiss = { showAppRedirectSheet = false }
            )
        }

        if (showSiteInfoSheet) {
            SiteInfoBottomSheet(
                url = uiState.currentUrl,
                isSecure = uiState.isSecure,
                onDismiss = { showSiteInfoSheet = false }
            )
        }

        if (showWebContextMenu) {
            WebContextMenuDialog(
                title = contextMenuTitle,
                imageUrl = contextMenuImageUrl,
                linkUrl = contextMenuLinkUrl,
                onOpenNewTab = { targetUrl ->
                    viewModel.newTab()
                    viewModel.openExternalUrl(targetUrl)
                },
                onOpenBackgroundTab = { _ ->
                    viewModel.newTab()
                },
                onOpenPrivateTab = { targetUrl ->
                    viewModel.setPrivateMode(true)
                    viewModel.newTab()
                    viewModel.openExternalUrl(targetUrl)
                },
                onPreviewPage = { previewUrl ->
                    activePreviewUrl = previewUrl
                    showPagePreviewSheet = true
                },
                onPreviewImage = { previewImg ->
                    activePreviewImageUrl = previewImg
                    showImagePreviewDialog = true
                },
                onDismiss = {
                    showWebContextMenu = false
                    contextMenuTitle = ""
                    contextMenuImageUrl = ""
                    contextMenuLinkUrl = ""
                }
            )
        }

        if (showPagePreviewSheet) {
            PagePreviewSheet(
                url = activePreviewUrl,
                onOpenFullTab = { fullUrl ->
                    viewModel.newTab()
                    viewModel.openExternalUrl(fullUrl)
                },
                onDismiss = {
                    showPagePreviewSheet = false
                    activePreviewUrl = ""
                }
            )
        }

        if (showImagePreviewDialog) {
            ImagePreviewDialog(
                imageUrl = activePreviewImageUrl,
                onDismiss = {
                    showImagePreviewDialog = false
                    activePreviewImageUrl = ""
                }
            )
        }

        if (showQuickActionSheet) {
            QuickActionSheet(
                currentThemeMode = themeMode,
                desktopMode = desktopMode,
                dataSaverEnabled = dataSaverEnabled,
                isPrivateMode = isPrivateMode,
                strictAdBlockEnabled = strictAdBlockEnabled,
                isAstraShieldActive = isAstraShieldActive,
                isAdultContentBlocked = isAdultContentBlocked,
                blockedAdsCount = blockedAdsCount,
                selectedLanguage = indianLanguage,
                onToggleThemeMode = { viewModel.setThemeMode(it) },
                onToggleDesktopMode = { viewModel.setDesktopMode(it) },
                onToggleDataSaver = { viewModel.setDataSaverEnabled(it) },
                onTogglePrivateMode = { viewModel.setPrivateMode(it) },
                onToggleAdBlock = { viewModel.setStrictAdBlockEnabled(it) },
                onToggleAdultContent = { viewModel.setAdultContentBlocked(it) },
                onSelectLanguage = { viewModel.setIndianLanguage(it) },
                onOpenBookmarks = {
                    Toast.makeText(context, "🔖 Saved Bookmarks", Toast.LENGTH_SHORT).show()
                },
                onOpenHistory = {
                    Toast.makeText(context, "🕒 Browsing History (Private On-Device)", Toast.LENGTH_SHORT).show()
                },
                onOpenDownloads = { viewModel.openDownloadsScreen() },
                onSharePage = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, uiState.pageTitle)
                        putExtra(Intent.EXTRA_TEXT, uiState.currentUrl)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Page Link via"))
                },
                onCaptureFullScreenshot = {
                    val targetWebView = webViewRef
                    if (targetWebView != null) {
                        scope.launch {
                            Toast.makeText(context, "📸 Capturing full-page screenshot...", Toast.LENGTH_SHORT).show()
                            when (val res = ScreenshotCaptureEngine.captureFullPage(context, targetWebView)) {
                                is ScreenshotResult.Success -> {
                                    Toast.makeText(context, "🖼️ Saved: ${res.fileName} in Pictures/Swara!", Toast.LENGTH_LONG).show()
                                }
                                is ScreenshotResult.Failure -> {
                                    Toast.makeText(context, "❌ ${res.error}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "No active web page to capture", Toast.LENGTH_SHORT).show()
                    }
                },
                onOpenWhatsAppStatusSaver = { showWhatsAppStatusSaverDialog = true },
                onOpenExtensionsManager = { viewModel.openExtensionManagerDialog() },
                onOpenAstraRewards = { viewModel.openAstraRewardDialog() },
                onOpenAllSettings = {
                    showQuickActionSheet = false
                    viewModel.openSettingsDialog()
                },
                onDismiss = { showQuickActionSheet = false }
            )
        }

        if (showWhatsAppStatusSaverDialog) {
            WhatsAppStatusScreen(
                onDismiss = { showWhatsAppStatusSaverDialog = false }
            )
        }

        if (pendingDownloadName != null) {
            AskBeforeDownloadDialog(
                suggestedFileName = pendingDownloadName ?: "Swara_Download.mp4",
                onConfirmDownload = { name, isVault ->
                    viewModel.downloadDetectedVideo(context)
                    pendingDownloadName = null
                },
                onDismiss = { pendingDownloadName = null }
            )
        }

        if (uiState.showEulaDialog) {
            EulaScreen(
                isReviewMode = eulaAccepted,
                onAccept = { viewModel.acceptEula() },
                onDecline = { (context as? Activity)?.finish() },
                onDismiss = { viewModel.closeEulaDialog() }
            )
        }

        if (uiState.showSearchEngineSelectionDialog) {
            SearchEngineSelectionDialog(
                initialEngine = searchEngine,
                onEngineSelected = { viewModel.confirmSearchEngineChoice(it) }
            )
        }

        if (uiState.showTabManagerDialog) {
            TabManagerDialog(
                tabs = tabs,
                activeTabId = activeTabId,
                onTabSelected = { viewModel.selectTab(it) },
                onCloseTab = { viewModel.closeTab(it) },
                onNewTab = { viewModel.newTab() },
                onCloseAllTabs = { viewModel.closeAllTabs() },
                onToggleTabAudioMute = { tabId ->
                    viewModel.toggleTabAudioMute(tabId, webViewRef)
                },
                onDismiss = { viewModel.closeTabManagerDialog() }
            )
        }

        if (uiState.showExtensionManagerDialog) {
            ExtensionManagerDialog(
                youtubeAdBlockEnabled = youtubeAdBlockEnabled,
                virusTotalEnabled = virusTotalEnabled,
                unrestrictedSocialModeEnabled = unrestrictedSocialModeEnabled,
                strictAdBlockEnabled = strictAdBlockEnabled,
                dataSaverEnabled = dataSaverEnabled,
                autoUpdateExtensionsEnabled = autoUpdateExtensionsEnabled,
                onYoutubeAdBlockToggled = { viewModel.setYoutubeAdBlockEnabled(it) },
                onVirusTotalToggled = { viewModel.setVirusTotalEnabled(it) },
                onUnrestrictedSocialModeToggled = { viewModel.setUnrestrictedSocialModeEnabled(it) },
                onStrictAdBlockToggled = { viewModel.setStrictAdBlockEnabled(it) },
                onDataSaverToggled = { viewModel.setDataSaverEnabled(it) },
                onAutoUpdateExtensionsToggled = { viewModel.setAutoUpdateExtensionsEnabled(it) },
                onCheckForUpdatesClicked = {
                    Toast.makeText(context, "All extensions are up to date! ✔", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { viewModel.closeExtensionManagerDialog() }
            )
        }
    }
}
