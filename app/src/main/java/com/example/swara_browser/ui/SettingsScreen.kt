package com.example.swara_browser.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Update
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swara_browser.data.CookieAutoDestructDuration
import com.example.swara_browser.data.IndianLanguage
import com.example.swara_browser.data.SearchEngine
import com.example.swara_browser.data.StartupMode
import com.example.swara_browser.ui.theme.ThemeMode

enum class SettingsSubPage {
    ROOT,
    SEARCH_ENGINE,
    PRIVACY,
    WEB_STORE,
    ABOUT
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    currentThemeMode: ThemeMode,
    currentSearchEngine: SearchEngine,
    currentLanguage: IndianLanguage,
    desktopMode: Boolean,
    javascriptEnabled: Boolean,
    isPrivateMode: Boolean,
    antiTrackingEnabled: Boolean,
    httpsOnlyEnabled: Boolean,
    dataSaverEnabled: Boolean,
    strictAdBlockEnabled: Boolean,
    youtubeAdBlockEnabled: Boolean,
    removeAnnotationsEnabled: Boolean,
    unrestrictedSocialModeEnabled: Boolean,
    isAutofillEnabled: Boolean,
    cookieAutoDestructDuration: CookieAutoDestructDuration,
    startupMode: StartupMode,
    customStartupUrl: String,
    selectedNewsTopics: Set<String>,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onSearchEngineSelected: (SearchEngine) -> Unit,
    onLanguageSelected: (IndianLanguage) -> Unit,
    onDesktopModeToggled: (Boolean) -> Unit,
    onJavascriptToggled: (Boolean) -> Unit,
    onPrivateModeToggled: (Boolean) -> Unit,
    onAntiTrackingToggled: (Boolean) -> Unit,
    onHttpsOnlyToggled: (Boolean) -> Unit,
    onDataSaverToggled: (Boolean) -> Unit,
    onStrictAdBlockToggled: (Boolean) -> Unit,
    onYoutubeAdBlockToggled: (Boolean) -> Unit,
    onRemoveAnnotationsToggled: (Boolean) -> Unit,
    onUnrestrictedSocialModeToggled: (Boolean) -> Unit,
    onAutofillToggled: (Boolean) -> Unit,
    onCookieAutoDestructDurationSelected: (CookieAutoDestructDuration) -> Unit,
    onStartupModeSelected: (StartupMode) -> Unit,
    onCustomStartupUrlChanged: (String) -> Unit,
    onSaveNewsTopics: (Set<String>) -> Unit,
    onOpenExtensionManagerClicked: () -> Unit,
    onSetAsDefaultBrowserClicked: () -> Unit,
    onOpenEulaClicked: () -> Unit,
    onEmergencySelfDestructClicked: () -> Unit,
    isCheckingForUpdates: Boolean,
    updateAvailable: Pair<String, String>?,
    onCheckForUpdates: (String) -> Unit,
    onDownloadUpdate: (String) -> Unit,
    onDismissUpdateDialog: () -> Unit,
    onDismiss: () -> Unit
) {
    var activeSubPage by remember { mutableStateOf(SettingsSubPage.ROOT) }

    val context = LocalContext.current
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.1"
        } catch (e: Exception) {
            "0.0.1"
        }
    }

    BackHandler {
        if (activeSubPage != SettingsSubPage.ROOT) {
            activeSubPage = SettingsSubPage.ROOT
        } else {
            onDismiss()
        }
    }

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showStartupDialog by remember { mutableStateOf(false) }
    var showCookieDurationDialog by remember { mutableStateOf(false) }
    var showSelfDestructConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (activeSubPage) {
                            SettingsSubPage.ROOT -> "Settings"
                            SettingsSubPage.SEARCH_ENGINE -> "Search Engine"
                            SettingsSubPage.PRIVACY -> "Privacy & Shields"
                            SettingsSubPage.WEB_STORE -> "Web Store & Extensions"
                            SettingsSubPage.ABOUT -> "About Swara & Mission"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (activeSubPage != SettingsSubPage.ROOT) {
                                activeSubPage = SettingsSubPage.ROOT
                            } else {
                                onDismiss()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when (activeSubPage) {
                SettingsSubPage.ROOT -> {
                    // Top 4-Pill Squircle Toggle Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PillSquircleToggle(
                            title = "Shield",
                            isActive = strictAdBlockEnabled,
                            onToggle = { onStrictAdBlockToggled(!strictAdBlockEnabled) },
                            modifier = Modifier.weight(1f)
                        )
                        PillSquircleToggle(
                            title = "AdBlock",
                            isActive = youtubeAdBlockEnabled,
                            onToggle = { onYoutubeAdBlockToggled(!youtubeAdBlockEnabled) },
                            modifier = Modifier.weight(1f)
                        )
                        PillSquircleToggle(
                            title = "Private",
                            isActive = isPrivateMode,
                            onToggle = { onPrivateModeToggled(!isPrivateMode) },
                            modifier = Modifier.weight(1f)
                        )
                        PillSquircleToggle(
                            title = "OLED",
                            isActive = currentThemeMode == ThemeMode.OLED,
                            onToggle = {
                                onThemeModeSelected(if (currentThemeMode == ThemeMode.OLED) ThemeMode.SYSTEM else ThemeMode.OLED)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grouped Card 1: Browser Basics
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            SettingsListTile(
                                title = "Search Engine",
                                subtitle = currentSearchEngine.displayName,
                                icon = Icons.Default.Search,
                                onClick = { activeSubPage = SettingsSubPage.SEARCH_ENGINE }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                            SettingsListTile(
                                title = "App Language",
                                subtitle = "${currentLanguage.nativeName} (${currentLanguage.englishName})",
                                icon = Icons.Default.Language,
                                onClick = { showLanguageDialog = true }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                            SettingsListTile(
                                title = "On Startup Behavior",
                                subtitle = startupMode.displayName,
                                icon = Icons.Default.Home,
                                onClick = { showStartupDialog = true }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                            SettingsListTile(
                                title = "Set Swara as Default Browser",
                                subtitle = "Manage system default apps",
                                icon = Icons.Default.Public,
                                onClick = onSetAsDefaultBrowserClicked
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grouped Card 2: Privacy, Shields & Sub-pages
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            SettingsListTile(
                                title = "Privacy, Shields & Anti-Tracking",
                                subtitle = "HTTPS-Only, Disappearing Cookies & Fingerprint Noise",
                                icon = Icons.Default.Security,
                                onClick = { activeSubPage = SettingsSubPage.PRIVACY }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                            SettingsListTile(
                                title = "Web Store & Extensions Catalog",
                                subtitle = "YouTube Video AdBlocker, Social Reader & Extensions",
                                icon = Icons.Default.Extension,
                                onClick = { activeSubPage = SettingsSubPage.WEB_STORE }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                            SettingsListTile(
                                title = "About Swara & Non-Profit Mission",
                                subtitle = "Non-Profit Policy, EULA Terms & Emergency Wipe",
                                icon = Icons.Default.Info,
                                onClick = { activeSubPage = SettingsSubPage.ABOUT }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Explicit Return To Browser Action Button
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "← Return to Browser",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // SUB-PAGE 1: SEARCH ENGINE
                SettingsSubPage.SEARCH_ENGINE -> {
                    Text(
                        text = "Choose Your Default Search Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    SearchEngine.entries.forEach { engine ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onSearchEngineSelected(engine)
                                    activeSubPage = SettingsSubPage.ROOT
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            RadioButton(
                                selected = currentSearchEngine == engine,
                                onClick = {
                                    onSearchEngineSelected(engine)
                                    activeSubPage = SettingsSubPage.ROOT
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = engine.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = engine.homeUrl,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // SUB-PAGE 2: PRIVACY & SHIELDS
                SettingsSubPage.PRIVACY -> {
                    SettingsSwitchTile(
                        title = "Swara Shield 100% AdBlocker",
                        subtitle = "Block doubleclick, banners & ad servers",
                        icon = Icons.Default.Block,
                        checked = strictAdBlockEnabled,
                        onCheckedChange = onStrictAdBlockToggled
                    )

                    SettingsSwitchTile(
                        title = "Hardware-Encrypted Autofill",
                        subtitle = "Secure on-device password vault via Android KeyStore",
                        icon = Icons.Default.Key,
                        checked = isAutofillEnabled,
                        onCheckedChange = onAutofillToggled
                    )

                    SettingsSwitchTile(
                        title = "Incognito / Private Mode",
                        subtitle = "No history, cookies or cache saved",
                        icon = Icons.Default.Security,
                        checked = isPrivateMode,
                        onCheckedChange = onPrivateModeToggled
                    )

                    SettingsSwitchTile(
                        title = "Block Third-Party Trackers & Canvas Noise",
                        subtitle = "Prevent web fingerprinting & ad tracking",
                        icon = Icons.Default.Security,
                        checked = antiTrackingEnabled,
                        onCheckedChange = onAntiTrackingToggled
                    )

                    SettingsSwitchTile(
                        title = "HTTPS-Only Mode",
                        subtitle = "Enforce encrypted SSL connections",
                        icon = Icons.Default.Security,
                        checked = httpsOnlyEnabled,
                        onCheckedChange = onHttpsOnlyToggled
                    )

                    SettingsListTile(
                        title = "Disappearing Cookies",
                        subtitle = "Auto-destruct timer: ${cookieAutoDestructDuration.displayName}",
                        icon = Icons.Default.Timer,
                        onClick = { showCookieDurationDialog = true }
                    )
                }

                // SUB-PAGE 3: WEB STORE
                SettingsSubPage.WEB_STORE -> {
                    Button(
                        onClick = onOpenExtensionManagerClicked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open Web Store Extension Catalog",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingsSwitchTile(
                        title = "YouTube Video AdBlocker & Anti-Preroll",
                        subtitle = "Removes pre-roll ads and sponsored overlays",
                        icon = Icons.Default.Extension,
                        checked = youtubeAdBlockEnabled,
                        onCheckedChange = onYoutubeAdBlockToggled
                    )

                    SettingsSwitchTile(
                        title = "Unrestricted Social Reader",
                        subtitle = "Bypass forced login popups on X, Instagram, Reddit & Quora",
                        icon = Icons.Default.Share,
                        checked = unrestrictedSocialModeEnabled,
                        onCheckedChange = onUnrestrictedSocialModeToggled
                    )
                }

                // SUB-PAGE 4: ABOUT SWARA & MISSION
                SettingsSubPage.ABOUT -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "🕊️ Non-Profit Mission Policy",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Providing free, clean, ad-free, 100% Zero-Logs Private web access for education, entertainment, and knowledge for all.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "🔒 100% Zero Data Tracking • Swara Community OpenSource",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onOpenEulaClicked,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Read Full EULA Terms & Community Guidelines")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { onCheckForUpdates(versionName) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCheckingForUpdates
                    ) {
                        Icon(
                            imageVector = Icons.Default.Update,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCheckingForUpdates) "Checking for updates..." else "Check for Updates (v$versionName)",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showSelfDestructConfirm = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Emergency Self-Destruct Wipe",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Dialog 1: Language Picker
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("App Language / भाषा", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    IndianLanguage.entries.forEach { language ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onLanguageSelected(language)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                        ) {
                            RadioButton(
                                selected = currentLanguage == language,
                                onClick = {
                                    onLanguageSelected(language)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${language.nativeName} (${language.englishName})",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog 2: Startup Mode Picker
    if (showStartupDialog) {
        AlertDialog(
            onDismissRequest = { showStartupDialog = false },
            title = { Text("On Startup Behavior", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    StartupMode.entries.forEach { mode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onStartupModeSelected(mode)
                                    if (mode != StartupMode.SPECIFIC_PAGE) {
                                        showStartupDialog = false
                                    }
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                        ) {
                            RadioButton(
                                selected = startupMode == mode,
                                onClick = {
                                    onStartupModeSelected(mode)
                                    if (mode != StartupMode.SPECIFIC_PAGE) {
                                        showStartupDialog = false
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = mode.displayName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    if (startupMode == StartupMode.SPECIFIC_PAGE) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customStartupUrl,
                            onValueChange = onCustomStartupUrlChanged,
                            label = { Text("Custom Startup URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStartupDialog = false }) { Text("Done") }
            }
        )
    }

    // Dialog 3: Cookie Duration Picker
    if (showCookieDurationDialog) {
        AlertDialog(
            onDismissRequest = { showCookieDurationDialog = false },
            title = { Text("Disappearing Cookies Timer", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    CookieAutoDestructDuration.entries.forEach { duration ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onCookieAutoDestructDurationSelected(duration)
                                    showCookieDurationDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                        ) {
                            RadioButton(
                                selected = cookieAutoDestructDuration == duration,
                                onClick = {
                                    onCookieAutoDestructDurationSelected(duration)
                                    showCookieDurationDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = duration.displayName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCookieDurationDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog 4: Self-Destruct Confirm
    if (showSelfDestructConfirm) {
        AlertDialog(
            onDismissRequest = { showSelfDestructConfirm = false },
            title = { Text("Trigger Emergency Self-Destruct?", fontWeight = FontWeight.Bold) },
            text = { Text("All local browsing history, cookies, cache, web storage, and Swara points will be permanently erased immediately.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSelfDestructConfirm = false
                        onEmergencySelfDestructClicked()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("YES, WIPE EVERYTHING") }
            },
            dismissButton = {
                TextButton(onClick = { showSelfDestructConfirm = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog 5: Update Available
    if (updateAvailable != null) {
        AlertDialog(
            onDismissRequest = onDismissUpdateDialog,
            title = { Text("Update Available!", fontWeight = FontWeight.Bold) },
            text = { Text("A newer version (${updateAvailable.first}) of Swara Browser is available to download.") },
            confirmButton = {
                Button(onClick = { onDownloadUpdate(updateAvailable.second) }) {
                    Text("Download Update")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissUpdateDialog) { Text("Later") }
            }
        )
    }
}

@Composable
private fun PillSquircleToggle(
    title: String,
    isActive: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() }
            .border(
                width = 1.dp,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isActive) "ON" else "OFF",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsListTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsSwitchTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
