package com.example.swara_browser.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swara_browser.data.AstraVaultEngine
import com.example.swara_browser.data.PointsActivityTransaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstraRewardScreen(
    points: Int,
    activeUntilTimestamp: Long,
    canClaimDailyBonus: Boolean,
    streakDay: Int,
    puzzlePieces: Int,
    completedPuzzles: Int,
    dailyAiQueriesCount: Int,
    dailyNewsCount: Int,
    dailyBrowsingMins: Int,
    isAppTourDone: Boolean,
    isDefaultBrowserClaimed: Boolean,
    pointsHistory: List<PointsActivityTransaction> = emptyList(),
    cooldownEndMap: Map<Int, Long> = emptyMap(),
    dailyClaimCount: Int = 0,
    lastRedeemedHours: Int = 0,
    onClaimDailyBonus: () -> Unit,
    onAskAiClicked: () -> Unit,
    onClaimAppTourBonus: () -> Unit,
    onClaimDefaultBrowserBonus: () -> Unit,
    onRedeemPackage: (hours: Int, pointCost: Int) -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    val isShieldActive = AstraVaultEngine.isProtectionActive(activeUntilTimestamp)
    val remainingTimeText = AstraVaultEngine.getRemainingTimeFormatted(activeUntilTimestamp)
    val now = System.currentTimeMillis()

    var isQuestsExpanded by remember { mutableStateOf(true) }
    var isRedeemExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rewards Hub",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            text = "$points PTS",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp
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
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Active Protection Summary Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isShieldActive) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Surface(
                                color = Color(0xFFFF9933),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Gold Shield Member",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            if (isShieldActive) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "🛡️ Active • $remainingTimeText",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Text(
                                    text = "🛡️ AstraShield Standby",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFFFFB703),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$points PTS",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6-Piece Grand Puzzle Board
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Extension,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Grand Puzzle Stamp Board ($puzzlePieces/6)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (completedPuzzles > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🏆 x$completedPuzzles",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF9933),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.height(110.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items((1..6).toList()) { index ->
                        val isCollected = index <= puzzlePieces
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCollected) Color(0xFF00C853).copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainer
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .border(
                                    width = 1.dp,
                                    color = if (isCollected) Color(0xFF00C853) else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (isCollected) {
                                    Text(
                                        text = "🧩 #$index",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF00C853)
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Slot #$index",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // 7-Day Streak Stepper
            Text(
                text = "7-Day Login Streak Ladder",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                val streakLadder = listOf(
                    1 to "+5",
                    2 to "+10",
                    3 to "+20",
                    4 to "+40",
                    5 to "+80",
                    6 to "+160",
                    7 to "+200 🧩"
                )

                streakLadder.forEach { (day, pts) ->
                    val isClaimed = day < streakDay || (day == streakDay && !canClaimDailyBonus)
                    val isToday = day == streakDay

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    color = if (isClaimed) Color(0xFF00C853) else if (isToday) Color(0xFFFF9933) else MaterialTheme.colorScheme.surfaceContainer
                                )
                                .border(
                                    width = if (isToday) 2.dp else 1.dp,
                                    color = if (isClaimed) Color(0xFF00C853) else if (isToday) Color(0xFFFF9933) else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                        ) {
                            if (isClaimed) {
                                Text(
                                    text = "✔",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "D$day",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isClaimed) "Redeemed" else pts,
                            fontSize = 8.sp,
                            color = if (isClaimed) Color(0xFF00C853) else if (isToday) Color(0xFFFF9933) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (canClaimDailyBonus) {
                Button(
                    onClick = onClaimDailyBonus,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9933)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Claim Streak Bonus (Day $streakDay)",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Daily Streak Claimed Today ✔")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // SECTION 1: DAILY QUESTS (6) ACCORDION
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isQuestsExpanded = !isQuestsExpanded }
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = "🎯 Daily Quests (6)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isQuestsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isQuestsExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = isQuestsExpanded,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(6.dp))

                    RewardQuestCard(
                        icon = Icons.Default.AutoAwesome,
                        title = "Ask Swara AI Assistant",
                        subtitle = "$dailyAiQueriesCount/3 Used Today • +5 PTS per query",
                        badgeText = "+5 PTS",
                        actionText = if (dailyAiQueriesCount >= 3) "Completed ✔" else "Ask AI",
                        isCompleted = dailyAiQueriesCount >= 3,
                        onAction = {
                            onDismiss()
                            onAskAiClicked()
                        }
                    )

                    RewardQuestCard(
                        icon = Icons.Default.Timer,
                        title = "Active Web Browsing",
                        subtitle = "$dailyBrowsingMins/20 mins • +10 PTS per 5 mins",
                        badgeText = "+10 PTS",
                        actionText = "Browse",
                        isCompleted = dailyBrowsingMins >= 20,
                        onAction = onDismiss
                    )

                    RewardQuestCard(
                        icon = Icons.Default.Newspaper,
                        title = "Trending News Reader",
                        subtitle = "$dailyNewsCount/5 Read Today • +5 PTS each",
                        badgeText = "+5 PTS",
                        actionText = "Read News",
                        isCompleted = dailyNewsCount >= 5,
                        onAction = onDismiss
                    )

                    RewardQuestCard(
                        icon = Icons.Default.Security,
                        title = "Privacy Micro-Lesson",
                        subtitle = "Learn 1-min safety tips & DoH rules",
                        badgeText = "+20 PTS",
                        actionText = "Learn",
                        isCompleted = false,
                        onAction = onDismiss
                    )

                    RewardQuestCard(
                        icon = Icons.Default.Smartphone,
                        title = "Complete App Tour",
                        subtitle = "One-time bonus for exploring settings",
                        badgeText = "+50 PTS",
                        actionText = if (isAppTourDone) "Claimed ✔" else "Claim +50",
                        isCompleted = isAppTourDone,
                        onAction = onClaimAppTourBonus
                    )

                    RewardQuestCard(
                        icon = Icons.Default.Public,
                        title = "Set Swara as Default Browser",
                        subtitle = "One-time bonus for default browser",
                        badgeText = "+100 PTS",
                        actionText = if (isDefaultBrowserClaimed) "Claimed ✔" else "Claim +100",
                        isCompleted = isDefaultBrowserClaimed,
                        onAction = onClaimDefaultBrowserBonus
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // SECTION 2: REDEEM SHIELD (5) ACCORDION
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isRedeemExpanded = !isRedeemExpanded }
                    .padding(vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🛡️ Redeem Shield (5)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Daily: $dailyClaimCount/4",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = if (isRedeemExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isRedeemExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = isRedeemExpanded,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(6.dp))

                    val packages = listOf(
                        Triple("10 Min AstraVault Shield", 0, 10),
                        Triple("1 Hour AstraVault Shield", 1, 50),
                        Triple("3 Hours AstraVault Shield", 3, 100),
                        Triple("12 Hours AstraVault Shield", 12, 250),
                        Triple("24 Hours (Full Day) Shield", 24, 450)
                    )

                    packages.forEach { (title, hours, cost) ->
                        val tierCooldownEnd = cooldownEndMap[hours] ?: 0L
                        val isTierInCooldown = tierCooldownEnd > now
                        val cdRemainingMs = tierCooldownEnd - now
                        val cdRemainingMins = if (cdRemainingMs > 0) (cdRemainingMs / 60000L).toInt() else 0

                        AstraRedeemItem(
                            title = title,
                            cost = cost,
                            userPoints = points,
                            isShieldActive = isShieldActive,
                            isTierInCooldown = isTierInCooldown,
                            cdRemainingMins = cdRemainingMins,
                            isDailyMax = dailyClaimCount >= 4,
                            onRedeem = { onRedeemPackage(hours, cost) }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // SECTION 3: POINTS ACTIVITY HISTORY TILE ROW (Launches PointsHistoryScreen)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNavigateToHistory() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Points Activity History",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "View all earned and redeemed points transactions",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "View History",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RewardQuestCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String,
    actionText: String,
    isCompleted: Boolean,
    onAction: () -> Unit
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        color = Color(0xFFFF9933),
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onAction,
                enabled = !isCompleted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) Color(0xFF00C853) else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = actionText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AstraRedeemItem(
    title: String,
    cost: Int,
    userPoints: Int,
    isShieldActive: Boolean,
    isTierInCooldown: Boolean,
    cdRemainingMins: Int,
    isDailyMax: Boolean,
    onRedeem: () -> Unit
) {
    val canAfford = userPoints >= cost
    val isEnabled = canAfford && !isShieldActive && !isTierInCooldown && !isDailyMax

    val buttonLabel = when {
        isShieldActive -> "🛡️ Active"
        isDailyMax -> "Max 4/4 Today"
        isTierInCooldown -> "Wait ${cdRemainingMins}m"
        !canAfford -> "Redeem"
        else -> "Redeem"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$cost Points",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = onRedeem,
            enabled = isEnabled,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = buttonLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
