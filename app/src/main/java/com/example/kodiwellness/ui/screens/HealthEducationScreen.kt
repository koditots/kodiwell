package com.example.kodiwellness.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthEducationScreen(
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedArticle by remember { mutableStateOf<HealthArticle?>(null) }

    val categories = listOf("All", "Nutrition", "Sleep", "Exercise", "Hydration", "Medication Safety", "Preventive Care")

    val articles = remember {
        listOf(
            HealthArticle(
                title = "10 Golden Rules for Safe Medication Management",
                category = "Medication Safety",
                readTime = "4 min read",
                summary = "Why medication timing matters, using pill organizers, and avoiding adverse food interactions.",
                content = "Proper medication adherence is one of the most impactful factors in managing chronic conditions and recovery.\n\n" +
                        "1. Take doses at consistent times every day.\n" +
                        "2. Never alter or double doses if you miss one without speaking to your pharmacist.\n" +
                        "3. Store pills away from moisture, extreme heat, and direct sunlight.\n" +
                        "4. Keep an updated list of all prescription drugs, over-the-counter remedies, and vitamins in Kodi Wellness.\n" +
                        "5. Check for food-drug interactions—for example, grapefruit juice interacts with certain blood pressure medications.\n\n" +
                        "Always speak with your doctor or pharmacist if you experience unexpected side effects."
            ),
            HealthArticle(
                title = "The Science of Restorative Sleep & Circadian Rhythm",
                category = "Sleep",
                readTime = "5 min read",
                summary = "Optimize your biological clock, minimize sleep fragmentation, and wake up refreshed.",
                content = "Quality sleep allows the brain to flush metabolic waste and repairs cellular tissues.\n\n" +
                        "• Keep consistent wake times, even on weekends.\n" +
                        "• Get 10–15 minutes of natural morning sunlight into your eyes to anchor your circadian rhythm.\n" +
                        "• Keep the bedroom environment between 65–68°F (18–20°C).\n" +
                        "• Avoid blue light emissions from smartphones 45 minutes before sleep."
            ),
            HealthArticle(
                title = "Hydration and Cognitive Performance",
                category = "Hydration",
                readTime = "3 min read",
                summary = "Even mild dehydration of 1-2% impairs attention, mood, and triggers tension headaches.",
                content = "Water makes up roughly 60% of our body weight and is critical for joint lubrication, blood volume, and brain function.\n\n" +
                        "• Thirst is a lagging indicator: by the time you feel thirsty, mild dehydration is already present.\n" +
                        "• Aim for baseline fluid intake of 2,000 to 2,500 ml daily.\n" +
                        "• Increase water consumption on hot days or following vigorous cardiovascular workouts."
            ),
            HealthArticle(
                title = "Nutrition for Heart Health & Steady Energy",
                category = "Nutrition",
                readTime = "4 min read",
                summary = "Balancing complex carbohydrates, healthy omega fats, and lean proteins.",
                content = "Emphasize Mediterranean-style eating patterns with plenty of leafy greens, berries, legumes, extra virgin olive oil, and cold-water fatty fish (salmon, sardines).\n\n" +
                        "Limiting ultra-processed sodium preserves endothelial arterial elasticity and supports healthy blood pressure."
            ),
            HealthArticle(
                title = "Preventive Health Screenings by Decade",
                category = "Preventive Care",
                readTime = "6 min read",
                summary = "Essential routine checkups, lipid panels, cancer screenings, and vaccines.",
                content = "Preventive medicine identifies potential physiological anomalies before symptomatic manifestation.\n\n" +
                        "• Annual blood pressure and fasting lipid panel tests.\n" +
                        "• Routine dental cleanings every 6 months to reduce systemic cardiovascular inflammation.\n" +
                        "• Keeping tetanus and seasonal influenza immunizations up-to-date."
            )
        )
    }

    val filtered = articles.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
        (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.summary.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Education", fontWeight = FontWeight.Bold) },
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
                .testTag("health_education_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search articles & wellness topics...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }

            items(filtered) { article ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedArticle = article },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    article.category,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(article.readTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(article.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Read Article →", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        selectedArticle?.let { art ->
            Dialog(onDismissRequest = { selectedArticle = null }) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                art.category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Text(art.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(art.readTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Divider()

                        Text(art.content, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)

                        Divider()
                        Text(
                            text = "Disclaimer: Educational content is for informational purposes only and does not replace medical advice.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(onClick = { selectedArticle = null }) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class HealthArticle(
    val title: String,
    val category: String,
    val readTime: String,
    val summary: String,
    val content: String
)
