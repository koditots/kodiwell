package com.example.kodiwellness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kodiwellness.ui.WellnessViewModel
import com.example.ui.theme.HealthGood
import com.example.ui.theme.HealthWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamificationScreen(
    viewModel: WellnessViewModel,
    onBack: () -> Unit
) {
    val gamification by viewModel.gamification.collectAsState()
    val user by viewModel.user.collectAsState()
    var isLeaderboardEnabled by remember { mutableStateOf(true) }

    val points = gamification?.totalPoints ?: 380
    val level = gamification?.currentLevel ?: 4
    val nextLevelPoints = level * 100
    val progress = ((points % 100).toFloat() / 100f).coerceIn(0f, 1f)

    val unlockedBadges = remember(gamification) {
        gamification?.unlockedBadges?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    }

    val badgesList = listOf(
        BadgeInfo("first_step", "First Step", "Logged first wellness task", Icons.Default.CheckCircle, true),
        BadgeInfo("hydration_hero", "7-Day Hydration Streak", "Drank 2,000ml for 7 consecutive days", Icons.Default.WaterDrop, unlockedBadges.contains("hydration_hero")),
        BadgeInfo("med_streak", "Medication Champion", "14-Day uninterrupted medication adherence", Icons.Default.Medication, unlockedBadges.contains("med_streak")),
        BadgeInfo("first_month_adherence", "First Month of Adherence", "Completed 30 days of prescribed medication schedules", Icons.Default.MilitaryTech, unlockedBadges.contains("first_month_adherence")),
        BadgeInfo("wellness_explorer", "Wellness Pioneer", "Earned 200+ healthy habit points", Icons.Default.Explore, unlockedBadges.contains("wellness_explorer") || points >= 200),
        BadgeInfo("century_master", "Century Master", "Accumulated 500+ wellness points", Icons.Default.WorkspacePremium, unlockedBadges.contains("century_master") || points >= 500),
        BadgeInfo("mindful_master", "Mindful Master", "Completed 5 meditation and breathing sessions", Icons.Default.SelfImprovement, false),
        BadgeInfo("movement_guru", "Movement Guru", "Logged 5 active workout or walking sessions", Icons.Default.DirectionsRun, false)
    )

    val friendLeaderboard = listOf(
        LeaderboardEntry("1", "Sarah D.", 620, "Level 7", false),
        LeaderboardEntry("2", "Marcus K.", 490, "Level 5", false),
        LeaderboardEntry("3", "${user?.fullName ?: "John Doe"} (You)", points, "Level $level", true),
        LeaderboardEntry("4", "Emma R.", 310, "Level 3", false),
        LeaderboardEntry("5", "David L.", 280, "Level 3", false)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements & Streaks", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("gamification_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Level & Points Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Wellness Rank", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                Text("Level $level Champion", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(32.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("$points total health points", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${100 - (points % 100)} pts needed to reach Level ${level + 1}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                }
            }

            // Streaks Overview
            item {
                Text("Active Health Streaks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StreakCard(
                        title = "Hydration",
                        streak = "${gamification?.hydrationStreak ?: 5} Days",
                        icon = Icons.Default.WaterDrop,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StreakCard(
                        title = "Medication",
                        streak = "${gamification?.medicationStreak ?: 12} Days",
                        icon = Icons.Default.Medication,
                        color = HealthGood,
                        modifier = Modifier.weight(1f)
                    )
                    StreakCard(
                        title = "Daily Plan",
                        streak = "${gamification?.overallDailyStreak ?: 6} Days",
                        icon = Icons.Default.LocalFireDepartment,
                        color = HealthWarning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Badges Section
            item {
                Text("Achievement Badges", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(badgesList) { badge ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (badge.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (badge.isUnlocked) 2.dp else 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (badge.isUnlocked) HealthWarning.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                badge.icon,
                                contentDescription = null,
                                tint = if (badge.isUnlocked) HealthWarning else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(badge.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                if (badge.isUnlocked) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Unlocked", tint = HealthGood, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(badge.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Optional Social Leaderboard for Friends
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Community & Friends Leaderboard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Encourage and compare weekly healthy habits", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isLeaderboardEnabled,
                        onCheckedChange = { isLeaderboardEnabled = it }
                    )
                }
            }

            if (isLeaderboardEnabled) {
                items(friendLeaderboard) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (entry.isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("#${entry.rank}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(32.dp))
                                Column {
                                    Text(entry.name, fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Medium)
                                    Text(entry.level, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text("${entry.points} pts", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakCard(title: String, streak: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(streak, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class BadgeInfo(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean
)

private data class LeaderboardEntry(
    val rank: String,
    val name: String,
    val points: Int,
    val level: String,
    val isCurrentUser: Boolean
)
