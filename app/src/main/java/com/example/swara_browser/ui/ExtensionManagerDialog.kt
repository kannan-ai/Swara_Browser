package com.example.swara_browser.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExtensionManagerDialog(
    youtubeAdBlockEnabled: Boolean,
    virusTotalEnabled: Boolean,
    unrestrictedSocialModeEnabled: Boolean,
    strictAdBlockEnabled: Boolean,
    dataSaverEnabled: Boolean,
    autoUpdateExtensionsEnabled: Boolean,
    onYoutubeAdBlockToggled: (Boolean) -> Unit,
    onVirusTotalToggled: (Boolean) -> Unit,
    onUnrestrictedSocialModeToggled: (Boolean) -> Unit,
    onStrictAdBlockToggled: (Boolean) -> Unit,
    onDataSaverToggled: (Boolean) -> Unit,
    onAutoUpdateExtensionsToggled: (Boolean) -> Unit,
    onCheckForUpdatesClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    var uBlockEnabled by remember { mutableStateOf(true) }
    var sponsorBlockEnabled by remember { mutableStateOf(true) }
    var darkReaderEnabled by remember { mutableStateOf(false) }
    var bypassPaywallsEnabled by remember { mutableStateOf(true) }
    var canvasAntiFingerprintEnabled by remember { mutableStateOf(true) }
    var httpsEverywhereEnabled by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Extension,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Extensions & Web Store",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Web Store")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Extension,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Manage")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (selectedTab == 0) {
                        Text(
                            text = "Featured Extension Catalog",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        WebStoreItem(
                            name = "uBlock Origin Lite",
                            developer = "Gorhill & Community",
                            rating = "4.9 ★ (820K Ratings)",
                            isInstalled = uBlockEnabled,
                            onInstallToggle = { uBlockEnabled = !uBlockEnabled }
                        )

                        WebStoreItem(
                            name = "SponsorBlock for YouTube",
                            developer = "Ajay Ramachandran",
                            rating = "4.9 ★ (510K Ratings)",
                            isInstalled = sponsorBlockEnabled,
                            onInstallToggle = { sponsorBlockEnabled = !sponsorBlockEnabled }
                        )

                        WebStoreItem(
                            name = "Dark Reader (OLED Dark Mode)",
                            developer = "Alexander Shutov",
                            rating = "4.8 ★ (640K Ratings)",
                            isInstalled = darkReaderEnabled,
                            onInstallToggle = { darkReaderEnabled = !darkReaderEnabled }
                        )

                        WebStoreItem(
                            name = "Bypass Paywalls Clean",
                            developer = "Magnolia1234",
                            rating = "4.8 ★ (320K Ratings)",
                            isInstalled = bypassPaywallsEnabled,
                            onInstallToggle = { bypassPaywallsEnabled = !bypassPaywallsEnabled }
                        )

                        WebStoreItem(
                            name = "Canvas Anti-Fingerprint Blocker",
                            developer = "Privacy Tools",
                            rating = "4.7 ★ (190K Ratings)",
                            isInstalled = canvasAntiFingerprintEnabled,
                            onInstallToggle = { canvasAntiFingerprintEnabled = !canvasAntiFingerprintEnabled }
                        )

                        WebStoreItem(
                            name = "HTTPS Everywhere Shield",
                            developer = "EFF & Tor Project",
                            rating = "4.9 ★ (920K Ratings)",
                            isInstalled = httpsEverywhereEnabled,
                            onInstallToggle = { httpsEverywhereEnabled = !httpsEverywhereEnabled }
                        )

                        WebStoreItem(
                            name = "YouTube Video AdBlocker (+ Anti-Preroll)",
                            developer = "Swara Community",
                            rating = "4.9 ★ (381.1K Ratings)",
                            isInstalled = youtubeAdBlockEnabled,
                            onInstallToggle = { onYoutubeAdBlockToggled(!youtubeAdBlockEnabled) }
                        )

                        WebStoreItem(
                            name = "VirusTotal Security Guard",
                            developer = "Swara Community",
                            rating = "4.8 ★ (120K Ratings)",
                            isInstalled = virusTotalEnabled,
                            onInstallToggle = { onVirusTotalToggled(!virusTotalEnabled) }
                        )

                    } else {
                        Text(
                            text = "Manage Installed Extensions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ExtensionManageRow(
                            name = "uBlock Origin Lite",
                            version = "v1.0.4",
                            enabled = uBlockEnabled,
                            onToggle = { uBlockEnabled = it }
                        )

                        ExtensionManageRow(
                            name = "SponsorBlock for YouTube",
                            version = "v5.4.1",
                            enabled = sponsorBlockEnabled,
                            onToggle = { sponsorBlockEnabled = it }
                        )

                        ExtensionManageRow(
                            name = "Dark Reader",
                            version = "v4.9.68",
                            enabled = darkReaderEnabled,
                            onToggle = { darkReaderEnabled = it }
                        )

                        ExtensionManageRow(
                            name = "Bypass Paywalls Clean",
                            version = "v3.6.2",
                            enabled = bypassPaywallsEnabled,
                            onToggle = { bypassPaywallsEnabled = it }
                        )

                        ExtensionManageRow(
                            name = "Canvas Anti-Fingerprint",
                            version = "v2.1.0",
                            enabled = canvasAntiFingerprintEnabled,
                            onToggle = { canvasAntiFingerprintEnabled = it }
                        )

                        ExtensionManageRow(
                            name = "HTTPS Everywhere",
                            version = "v2024.1.1",
                            enabled = httpsEverywhereEnabled,
                            onToggle = { httpsEverywhereEnabled = it }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Automatic Updates (Recommended)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Automatically update ad filters & malware rules",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = autoUpdateExtensionsEnabled,
                                onCheckedChange = onAutoUpdateExtensionsToggled
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = onCheckForUpdatesClicked,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Check for Extension Updates Now")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun WebStoreItem(
    name: String,
    developer: String,
    rating: String,
    isInstalled: Boolean,
    onInstallToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "by $developer • $rating",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onInstallToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isInstalled) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFFF9933)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isInstalled) "Installed" else "Get / Enable",
                    color = if (isInstalled) MaterialTheme.colorScheme.onSecondaryContainer else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ExtensionManageRow(
    name: String,
    version: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = version,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle
        )
    }
}
