package com.example.swara_browser.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.swara_browser.data.AstraVaultEngine
import com.example.swara_browser.data.IndianBrand
import com.example.swara_browser.data.IndianLanguage
import com.example.swara_browser.data.NewsItem
import com.example.swara_browser.data.SearchEngine
import com.example.swara_browser.data.TrendingNewsEngine
import com.example.swara_browser.ui.theme.ThemeMode

enum class WallpaperMode {
    OLED_BLACK,
    NATURE_LANDSCAPE,
    SYSTEM_DEFAULT
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun StartHomeScreen(
    selectedLanguage: IndianLanguage,
    searchEngine: SearchEngine,
    isPrivateMode: Boolean,
    showTrendingNews: Boolean,
    isNewsTopicSetupCompleted: Boolean,
    selectedNewsTopics: Set<String>,
    points: Int,
    activeUntilTimestamp: Long,
    blockedAdsCount: Int,
    liveNewsList: List<NewsItem> = emptyList(),
    customWallpaperUrl: String = "",
    pinnedBrands: List<IndianBrand> = emptyList(),
    onBrandClicked: (String) -> Unit,
    onToggleShowTrendingNews: () -> Unit,
    onSaveNewsTopics: (Set<String>) -> Unit,
    onOpenNewsTopicSettings: () -> Unit,
    onOpenAstraRewardsClicked: () -> Unit,
    onOpenWallpaperGallery: () -> Unit = {},
    onAddCustomShortcut: (String, String) -> Unit = { _, _ -> },
    onRemoveCustomShortcut: (String) -> Unit = {},
    onNewsLongClicked: (NewsItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var newsDisplayLimit by remember { mutableIntStateOf(4) }
    val newsItems = remember(liveNewsList) {
        if (liveNewsList.isNotEmpty()) liveNewsList else TrendingNewsEngine.getDefaultCombinedFeed()
    }

    var showAddShortcutDialog by remember { mutableStateOf(false) }
    var selectedShortcutForDelete by remember { mutableStateOf<IndianBrand?>(null) }

    var showShortcuts by remember { mutableStateOf(true) }
    var wallpaperMode by remember { mutableStateOf(WallpaperMode.SYSTEM_DEFAULT) }
    var showPageSettingsPopover by remember { mutableStateOf(false) }

    val defaultShortcuts = remember {
        listOf(
            IndianBrand("google", "Google", "G", "Search", Color(0xFF4285F4), "https://www.google.com"),
            IndianBrand("youtube", "YouTube", "Y", "Media", Color(0xFFFF0000), "https://www.youtube.com"),
            IndianBrand("wikipedia", "Wikipedia", "W", "Reference", Color(0xFF636466), "https://www.wikipedia.org"),
            IndianBrand("facebook", "Facebook", "F", "Social", Color(0xFF1877F2), "https://www.facebook.com"),
            IndianBrand("irctc", "IRCTC", "I", "Travel", Color(0xFF003366), "https://www.irctc.co.in"),
            IndianBrand("flipkart", "Flipkart", "F", "Shopping", Color(0xFF2874F0), "https://www.flipkart.com")
        )
    }

    val displayShortcuts = remember(pinnedBrands) {
        if (pinnedBrands.isEmpty()) defaultShortcuts else pinnedBrands + defaultShortcuts
    }

    val isAstraShieldActive = AstraVaultEngine.isProtectionActive(activeUntilTimestamp)
    val remainingTimeText = AstraVaultEngine.getRemainingTimeFormatted(activeUntilTimestamp)

    val backgroundColor = when (wallpaperMode) {
        WallpaperMode.OLED_BLACK -> Color.Black
        WallpaperMode.NATURE_LANDSCAPE -> Color(0xFF102A12)
        WallpaperMode.SYSTEM_DEFAULT -> MaterialTheme.colorScheme.background
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        if (customWallpaperUrl.isNotBlank() && wallpaperMode != WallpaperMode.OLED_BLACK) {
            AsyncImage(
                model = customWallpaperUrl,
                contentDescription = "Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Compact Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.size(28.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SWARA BROWSER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFFFF9933),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "🇮🇳",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = { showPageSettingsPopover = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Page Settings",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (isAstraShieldActive) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "🛡️ Shield Active • $remainingTimeText",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (isPrivateMode) {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedLanguage.privateModeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            if (showShortcuts) {
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showAddShortcutDialog = true }
                                .padding(4.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Shortcut",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    items(displayShortcuts, key = { it.id }) { brand ->
                        IndianBrandCard(
                            brand = brand,
                            onClick = { onBrandClicked(brand.url) },
                            onLongClick = {
                                if (brand.id.startsWith("custom_")) {
                                    selectedShortcutForDelete = brand
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!showTrendingNews) {
                OutlinedButton(
                    onClick = onToggleShowTrendingNews,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Newspaper,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "📰 Show News Feed",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Newspaper,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Top Stories",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        val visibleCount = minOf(newsDisplayLimit, newsItems.size)

                        newsItems.take(visibleCount).forEachIndexed { index, news ->
                            if (index == 0 && news.imageUrl.isNotBlank()) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .combinedClickable(
                                            onClick = { onBrandClicked(news.url) },
                                            onLongClick = { onNewsLongClicked(news) }
                                        )
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(170.dp)
                                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        ) {
                                            AsyncImage(
                                                model = news.imageUrl,
                                                contentDescription = news.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )

                                            Surface(
                                                color = Color.Black.copy(alpha = 0.65f),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = "3/3",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Column(
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .background(Color.Black.copy(alpha = 0.65f))
                                                    .fillMaxWidth()
                                                    .padding(10.dp)
                                            ) {
                                                Text(
                                                    text = "${news.source} • ${news.timeAgo}",
                                                    color = Color.White.copy(alpha = 0.85f),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = news.title,
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .combinedClickable(
                                            onClick = { onBrandClicked(news.url) },
                                            onLongClick = { onNewsLongClicked(news) }
                                        )
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(10.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            val categoryTag = when (news.category.name) {
                                                "FOOD_SAFETY" -> "Food Safety"
                                                "REVEALED_TRUTHS" -> "Investigation"
                                                "TECH_HARDWARE" -> "Tech"
                                                "FACT_CHECK" -> "Fact Check"
                                                "WAR_GEOPOLITICS" -> "World"
                                                else -> news.category.displayName
                                            }

                                            Text(
                                                text = "${news.source} • $categoryTag • ${news.timeAgo}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = news.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (news.imageUrl.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(10.dp))

                                            Box(
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                            ) {
                                                AsyncImage(
                                                    model = news.imageUrl,
                                                    contentDescription = news.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = {
                                    newsDisplayLimit = when (newsDisplayLimit) {
                                        4 -> 8
                                        8 -> 12
                                        12 -> 18
                                        18 -> 25
                                        else -> 4
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (newsDisplayLimit >= newsItems.size) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (newsDisplayLimit >= newsItems.size) "Collapse to Top 4" else "Show More ($visibleCount/${newsItems.size})",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            TextButton(onClick = onToggleShowTrendingNews) {
                                Text(text = "Hide News", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showPageSettingsPopover) {
        PageCustomizationSheet(
            showShortcuts = showShortcuts,
            showTrendingNews = showTrendingNews,
            currentThemeMode = if (wallpaperMode == WallpaperMode.OLED_BLACK) ThemeMode.OLED else ThemeMode.SYSTEM,
            selectedWallpaperName = if (customWallpaperUrl.isNotBlank()) "Applied Wallpaper" else "Default",
            onToggleShowShortcuts = { showShortcuts = it },
            onToggleShowTrendingNews = { onToggleShowTrendingNews() },
            onToggleThemeMode = {
                wallpaperMode = if (it == ThemeMode.OLED) WallpaperMode.OLED_BLACK else WallpaperMode.SYSTEM_DEFAULT
            },
            onOpenWallpaperGallery = {
                showPageSettingsPopover = false
                onOpenWallpaperGallery()
            },
            onDismiss = { showPageSettingsPopover = false }
        )
    }

    if (showAddShortcutDialog) {
        AddShortcutDialog(
            onAddCustom = { name, url ->
                onAddCustomShortcut(name, url)
                showAddShortcutDialog = false
                Toast.makeText(context, "Shortcut added to Quick Access! 📌", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showAddShortcutDialog = false }
        )
    }

    if (selectedShortcutForDelete != null) {
        val brand = selectedShortcutForDelete!!
        AlertDialog(
            onDismissRequest = { selectedShortcutForDelete = null },
            title = { Text("Delete Shortcut", fontWeight = FontWeight.Bold) },
            text = { Text("Remove '${brand.name}' from Quick Access shortcuts?") },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveCustomShortcut(brand.id)
                        selectedShortcutForDelete = null
                        Toast.makeText(context, "Shortcut removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedShortcutForDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AddShortcutDialog(
    onAddCustom: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var customName by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Quick Shortcut",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Enter website shortcut details:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Shortcut Name (e.g. YouTube)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customUrl,
                    onValueChange = { customUrl = it },
                    label = { Text("Website URL (e.g. youtube.com)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customName.isNotBlank() && customUrl.isNotBlank()) {
                        onAddCustom(customName, customUrl)
                    }
                },
                enabled = customName.isNotBlank() && customUrl.isNotBlank()
            ) {
                Text("Add Shortcut")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IndianBrandCard(
    brand: IndianBrand,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val faviconUrl = "https://www.google.com/s2/favicons?domain=${brand.url}&sz=128"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(2.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color = brand.accentColor)
        ) {
            AsyncImage(
                model = faviconUrl,
                contentDescription = brand.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = brand.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
