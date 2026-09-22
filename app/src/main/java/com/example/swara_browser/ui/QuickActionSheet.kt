package com.example.swara_browser.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swara_browser.data.IndianLanguage
import com.example.swara_browser.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionSheet(
    currentThemeMode: ThemeMode,
    desktopMode: Boolean = false,
    dataSaverEnabled: Boolean = false,
    isPrivateMode: Boolean,
    strictAdBlockEnabled: Boolean,
    isAstraShieldActive: Boolean,
    isAdultContentBlocked: Boolean = true,
    blockedAdsCount: Int = 0,
    selectedLanguage: IndianLanguage,
    onToggleThemeMode: (ThemeMode) -> Unit,
    onToggleDesktopMode: (Boolean) -> Unit = {},
    onToggleDataSaver: (Boolean) -> Unit = {},
    onTogglePrivateMode: (Boolean) -> Unit,
    onToggleAdBlock: (Boolean) -> Unit,
    onToggleAdultContent: (Boolean) -> Unit = {},
    onSelectLanguage: (IndianLanguage) -> Unit = {},
    onOpenBookmarks: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenDownloads: () -> Unit = {},
    onSharePage: () -> Unit = {},
    onCaptureFullScreenshot: () -> Unit = {},
    onOpenWhatsAppStatusSaver: () -> Unit = {},
    onOpenExtensionsManager: () -> Unit = {},
    onOpenAstraRewards: () -> Unit,
    onOpenAllSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    var showLanguagePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Pull handle",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            // SECTION 1: TOP STATUS ROW (4 Squircles)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PillStatusToggle(
                    title = "Astra",
                    subtitle = if (isAstraShieldActive) "Active" else "Standby",
                    icon = Icons.Default.Shield,
                    isActive = isAstraShieldActive,
                    onClick = {
                        onDismiss()
                        onOpenAstraRewards()
                    },
                    modifier = Modifier.weight(1f)
                )

                PillStatusToggle(
                    title = "AdBlock",
                    subtitle = if (strictAdBlockEnabled) "ON" else "OFF",
                    icon = Icons.Default.Block,
                    isActive = strictAdBlockEnabled,
                    onClick = { onToggleAdBlock(!strictAdBlockEnabled) },
                    modifier = Modifier.weight(1f)
                )

                PillStatusToggle(
                    title = "Private",
                    subtitle = if (isPrivateMode) "ON" else "OFF",
                    icon = Icons.Default.Security,
                    isActive = isPrivateMode,
                    onClick = { onTogglePrivateMode(!isPrivateMode) },
                    modifier = Modifier.weight(1f)
                )

                PillStatusToggle(
                    title = "OLED",
                    subtitle = if (currentThemeMode == ThemeMode.OLED) "ON" else "OFF",
                    icon = Icons.Default.Bolt,
                    isActive = currentThemeMode == ThemeMode.OLED,
                    onClick = {
                        onToggleThemeMode(if (currentThemeMode == ThemeMode.OLED) ThemeMode.SYSTEM else ThemeMode.OLED)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 2 & 3: UNIFIED CIRCULAR ACTION GRID
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // ROW 1: CORE BROWSING HUBS
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularActionIcon(
                        label = "Bookmarks",
                        icon = Icons.Default.Bookmarks,
                        onClick = {
                            onDismiss()
                            onOpenBookmarks()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    CircularActionIcon(
                        label = "History",
                        icon = Icons.Default.History,
                        onClick = {
                            onDismiss()
                            onOpenHistory()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    CircularActionIcon(
                        label = "Downloads",
                        icon = Icons.Default.Download,
                        onClick = {
                            onDismiss()
                            onOpenDownloads()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    CircularActionIcon(
                        label = "Share",
                        icon = Icons.Default.Share,
                        onClick = {
                            onDismiss()
                            onSharePage()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // ROW 2: UTILITIES & TOOLS
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularActionIcon(
                        label = "Screenshot",
                        icon = Icons.Default.CameraAlt,
                        isActive = true,
                        onClick = {
                            onDismiss()
                            onCaptureFullScreenshot()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    CircularActionIcon(
                        label = "Desktop",
                        icon = Icons.Default.DesktopWindows,
                        isActive = desktopMode,
                        onClick = { onToggleDesktopMode(!desktopMode) },
                        modifier = Modifier.weight(1f)
                    )

                    CircularActionIcon(
                        label = "Status Saver",
                        icon = Icons.Default.DownloadForOffline,
                        isActive = true,
                        activeTint = Color(0xFF25D366),
                        onClick = {
                            onDismiss()
                            onOpenWhatsAppStatusSaver()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    CircularActionIcon(
                        label = "Extensions",
                        icon = Icons.Default.Extension,
                        isActive = true,
                        onClick = {
                            onDismiss()
                            onOpenExtensionsManager()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

            // SECTION 4: BOTTOM SETTINGS ANCHOR
            Button(
                onClick = {
                    onDismiss()
                    onOpenAllSettings()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings ⚙️",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showLanguagePicker) {
        AlertDialog(
            onDismissRequest = { showLanguagePicker = false },
            title = { Text("Choose Language / भाषा", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    IndianLanguage.entries.forEach { lang ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onSelectLanguage(lang)
                                    showLanguagePicker = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedLanguage == lang,
                                onClick = {
                                    onSelectLanguage(lang)
                                    showLanguagePicker = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${lang.nativeName} (${lang.englishName})",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguagePicker = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun PillStatusToggle(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CircularActionIcon(
    label: String,
    icon: ImageVector,
    isActive: Boolean = false,
    activeTint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Surface(
            color = if (isActive) activeTint.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) activeTint else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (isActive) activeTint else MaterialTheme.colorScheme.onSurface
        )
    }
}
