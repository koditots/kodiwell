package com.example.kodiwellness.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kodiwellness.ui.components.KodiWellnessLogo
import com.example.ui.theme.HealthGood
import com.example.ui.theme.HealthInfo

@Composable
fun SplashScreen(
    onContinue: () -> Unit
) {
    var selectedFeatureIndex by remember { mutableIntStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val understandingCards = listOf(
        AppFeatureItem(
            title = "Daily Symptom Journal",
            subtitle = "Track severity with date stamps",
            description = "Easily log discomfort, severity from 1 to 10, duration, and let AI discover lifestyle correlations with hydration, sleep, and meals.",
            icon = Icons.Default.Sick,
            accentColor = Color(0xFF0284C7)
        ),
        AppFeatureItem(
            title = "Medication Management",
            subtitle = "Never miss a scheduled dose",
            description = "Set prescription schedules, track taken/skipped history, and maintain adherence percentages with reliable reminders.",
            icon = Icons.Default.Medication,
            accentColor = Color(0xFF16A34A)
        ),
        AppFeatureItem(
            title = "Wellness Planner & AI",
            subtitle = "Structured daily routines",
            description = "Generate personalized daily wellness schedules for fitness, hydration, and nutrition with Wellness AI assistance.",
            icon = Icons.Default.AutoAwesome,
            accentColor = Color(0xFF8B5CF6)
        ),
        AppFeatureItem(
            title = "Appointments & Emergency SOS",
            subtitle = "Care at your fingertips",
            description = "Manage doctor consultations, keep critical medical records, and access your emergency profile with one-tap dialing.",
            icon = Icons.Default.MedicalServices,
            accentColor = Color(0xFFEF4444)
        ),
        AppFeatureItem(
            title = "100% Private Local Storage",
            subtitle = "Protected Room database",
            description = "All medical documents, symptoms, vitals, and notes stay safe on your device. Works completely offline with zero tracking.",
            icon = Icons.Default.Shield,
            accentColor = Color(0xFF0D9488)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF0FDF4),
                        Color(0xFFF8FAFC),
                        Color(0xFFE0F2FE)
                    )
                )
            )
            .testTag("splash_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                // Animated Brand Logo
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .padding(bottom = 6.dp)
                ) {
                    KodiWellnessLogo(
                        iconSize = 120,
                        showTagline = true
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE0F2FE),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "App Overview & Features Guide",
                        color = Color(0xFF0369A1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Understanding Interactive Showcase
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val current = understandingCards[selectedFeatureIndex]

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(22.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(current.accentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = current.icon,
                                    contentDescription = null,
                                    tint = current.accentColor,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = current.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = current.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = current.accentColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Text(
                            text = current.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )

                        // Feature Indicators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            understandingCards.indices.forEach { index ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .height(6.dp)
                                        .width(if (index == selectedFeatureIndex) 20.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == selectedFeatureIndex) current.accentColor
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                        )
                                        .clickable { selectedFeatureIndex = index }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Feature Pill Selector
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(understandingCards) { index, item ->
                        FilterChip(
                            selected = selectedFeatureIndex == index,
                            onClick = { selectedFeatureIndex = index },
                            label = { Text(item.title, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(item.icon, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("enter_dashboard_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0284C7)
                    )
                ) {
                    Text(
                        text = "Enter Kodi Wellness",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = onContinue,
                    modifier = Modifier.testTag("skip_splash_btn")
                ) {
                    Text(
                        "Skip Tour & Go to Home",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

private data class AppFeatureItem(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color
)
