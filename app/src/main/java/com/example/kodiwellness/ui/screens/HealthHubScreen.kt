package com.example.kodiwellness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.kodiwellness.data.model.HealthRecordEntity
import com.example.kodiwellness.ui.WellnessViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthHubScreen(
    viewModel: WellnessViewModel,
    onNavigateToMedications: () -> Unit,
    onNavigateToSymptoms: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Hydration, 1: Sleep, 2: Exercise, 3: Weight, 4: Records

    val tabs = listOf("Hydration", "Sleep", "Exercise", "Weight", "Records")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health & Vital Metrics", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToMedications) {
                        Icon(Icons.Default.Medication, contentDescription = "Medications")
                    }
                    IconButton(onClick = onNavigateToSymptoms) {
                        Icon(Icons.Default.Sick, contentDescription = "Symptoms")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("health_hub_screen")
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            when (selectedTab) {
                0 -> HydrationTab(viewModel)
                1 -> SleepTab(viewModel)
                2 -> ExerciseTab(viewModel)
                3 -> WeightTab(viewModel)
                4 -> HealthRecordsTab(viewModel)
            }
        }
    }
}

// ----------------- Hydration Tab -----------------
@Composable
private fun HydrationTab(viewModel: WellnessViewModel) {
    val waterLogs by viewModel.todayWaterLogs.collectAsState()
    val totalWater = waterLogs.sumOf { it.amountMl }
    val goal = 2000
    val percent = ((totalWater.toFloat() / goal.toFloat()) * 100).toInt()
    val remaining = (goal - totalWater).coerceAtLeast(0)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(HealthInfo.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$totalWater", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = HealthInfo)
                            Text("ml", style = MaterialTheme.typography.labelSmall, color = HealthInfo)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Daily Hydration Target: $goal ml", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(if (remaining > 0) "$remaining ml remaining to hit goal" else "🎉 Target achieved for today!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { (totalWater.toFloat() / goal.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        color = HealthInfo,
                        trackColor = HealthInfo.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Quick Log Hydration", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { viewModel.addWater(250) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+250 ml")
                        }
                        Button(
                            onClick = { viewModel.addWater(500) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+500 ml")
                        }
                        Button(
                            onClick = { viewModel.addWater(750) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+750 ml")
                        }
                    }
                }
            }
        }

        item {
            Text("Today's Hydration Entries", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (waterLogs.isEmpty()) {
            item { Text("No water logged today. Tap a button above to record your first glass.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(waterLogs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = HealthInfo, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("+${log.amountMl} ml", fontWeight = FontWeight.Bold)
                        }
                        Text(log.timeString, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ----------------- Sleep Tab -----------------
@Composable
private fun SleepTab(viewModel: WellnessViewModel) {
    val sleepLog by viewModel.todaySleepLog.collectAsState()
    val allSleeps by viewModel.allSleepLogs.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Last Night's Sleep", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Target: 8.0 hours", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.Bedtime, contentDescription = null, tint = HealthPurple, modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    val duration = sleepLog?.durationHours ?: 7.5f
                    Text("${duration}h", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = HealthPurple)
                    Text("Bedtime: ${sleepLog?.bedtime ?: "11:00 PM"} • Wake: ${sleepLog?.wakeTime ?: "06:30 AM"}", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Quality: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        val rating = sleepLog?.qualityRating ?: 4
                        repeat(5) { star ->
                            Text(if (star < rating) "★" else "☆", color = HealthWarning, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Log Sleep Record")
                    }
                }
            }
        }

        item {
            Text("Sleep Hygiene Guidance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("• Consistent sleep timing helps regulate circadian rhythms.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Avoid heavy caffeine intake within 6 hours of bedtime.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Turn off screens 45 minutes before sleep to support natural melatonin production.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    if (showDialog) {
        LogSleepDialog(
            onDismiss = { showDialog = false },
            onSave = { bedtime, wake, duration, rating, notes ->
                viewModel.logSleep(bedtime, wake, duration, rating, notes)
                showDialog = false
            }
        )
    }
}

// ----------------- Exercise Tab -----------------
@Composable
private fun ExerciseTab(viewModel: WellnessViewModel) {
    val exercises by viewModel.exerciseLogs.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val totalMins = exercises.sumOf { it.durationMinutes }
    val totalCalories = exercises.sumOf { it.caloriesBurned }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Exercise & Movement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("$totalMins mins", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Active Movement", style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("$totalCalories kcal", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = HealthWarning)
                            Text("Energy Burned", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Log Workout / Activity")
                    }
                }
            }
        }

        item {
            Text("Recent Exercise Activities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(exercises) { ex ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ex.activityType, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("${ex.durationMinutes} mins • ${ex.caloriesBurned} kcal • ${ex.dateString}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (ex.notes.isNotBlank()) {
                            Text(ex.notes, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        LogExerciseDialog(
            onDismiss = { showDialog = false },
            onSave = { act, mins, dist, cal, notes ->
                viewModel.logExercise(act, mins, dist, cal, notes)
                showDialog = false
            }
        )
    }
}

// ----------------- Weight Tab -----------------
@Composable
private fun WeightTab(viewModel: WellnessViewModel) {
    val weights by viewModel.weightLogs.collectAsState()
    val user by viewModel.user.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    val currentWeight = weights.firstOrNull()?.weightKg ?: (user?.weightKg ?: 74.5f)
    val heightM = (user?.heightCm ?: 178f) / 100f
    val bmi = (currentWeight / (heightM * heightM))
    val bmiCategory = when {
        bmi < 18.5 -> "Underweight"
        bmi < 25.0 -> "Normal weight"
        bmi < 30.0 -> "Overweight"
        else -> "Obese"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Current Weight", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("$currentWeight kg", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Height: ${user?.heightCm ?: 178f} cm", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BMI %.1f".format(bmi), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(bmiCategory, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "*BMI and weight metrics are observational estimates and not medical diagnoses. Consult a physician or dietitian for tailored weight targets.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Record Weight Entry")
                    }
                }
            }
        }

        item {
            Text("Weight Log History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(weights) { w ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${w.weightKg} kg", fontWeight = FontWeight.Bold)
                        if (w.notes.isNotBlank()) Text(w.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(w.dateString, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            var inputWeight by remember { mutableStateOf("$currentWeight") }
            var notes by remember { mutableStateOf("") }

            Card(modifier = Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Log Weight", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = inputWeight,
                        onValueChange = { inputWeight = it },
                        label = { Text("Weight (kg)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (e.g. morning fasting)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                inputWeight.toFloatOrNull()?.let {
                                    viewModel.logWeight(it, notes)
                                    showDialog = false
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

// ----------------- Health Records Tab -----------------
@Composable
private fun HealthRecordsTab(viewModel: WellnessViewModel) {
    val records by viewModel.healthRecords.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Laboratory Result", "Medical Report", "Prescription", "Vaccination", "Imaging", "Doctor Note")

    val filtered = records.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
        (searchQuery.isBlank() || it.documentName.contains(searchQuery, ignoreCase = true) || it.doctorOrHospital.contains(searchQuery, ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Personal Health Records", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                FilledTonalButton(
                    onClick = { showDialog = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload Doc")
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search records, labs, doctors...") },
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

        if (filtered.isEmpty()) {
            item {
                Text("No health records matching your search.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(filtered) { doc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(doc.documentName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text(doc.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Encrypted", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${doc.doctorOrHospital} • ${doc.dateString}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (doc.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(doc.notes, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddRecordDialog(
            onDismiss = { showDialog = false },
            onAdd = { name, cat, date, clinic, notes ->
                viewModel.addHealthRecord(name, cat, date, clinic, notes)
                showDialog = false
            }
        )
    }
}

// Dialogs
@Composable
private fun LogSleepDialog(onDismiss: () -> Unit, onSave: (String, String, Float, Int, String) -> Unit) {
    var bedtime by remember { mutableStateOf("11:00 PM") }
    var wakeTime by remember { mutableStateOf("07:00 AM") }
    var duration by remember { mutableStateOf("8.0") }
    var quality by remember { mutableIntStateOf(4) }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Log Sleep", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = bedtime, onValueChange = { bedtime = it }, label = { Text("Bedtime") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = wakeTime, onValueChange = { wakeTime = it }, label = { Text("Wake Time") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Hours Slept") }, modifier = Modifier.fillMaxWidth())
                Text("Quality Rating (1-5)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { quality = star }) {
                            Text(if (star <= quality) "★" else "☆", fontSize = 24.sp, color = HealthWarning)
                        }
                    }
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onSave(bedtime, wakeTime, duration.toFloatOrNull() ?: 8f, quality, notes) }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun LogExerciseDialog(onDismiss: () -> Unit, onSave: (String, Int, Float, Int, String) -> Unit) {
    var activity by remember { mutableStateOf("Walking") }
    var minutes by remember { mutableStateOf("30") }
    var distance by remember { mutableStateOf("2.5") }
    var calories by remember { mutableStateOf("150") }
    var notes by remember { mutableStateOf("") }

    val activities = listOf("Walking", "Running", "Cycling", "Gym", "Yoga", "Stretching", "Swimming", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Log Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Activity Type", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    activities.take(4).forEach { a ->
                        FilterChip(selected = activity == a, onClick = { activity = a }, label = { Text(a, fontSize = 11.sp) })
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    activities.drop(4).forEach { a ->
                        FilterChip(selected = activity == a, onClick = { activity = a }, label = { Text(a, fontSize = 11.sp) })
                    }
                }
                OutlinedTextField(value = minutes, onValueChange = { minutes = it }, label = { Text("Duration (minutes)") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = distance, onValueChange = { distance = it }, label = { Text("Distance (km)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = calories, onValueChange = { calories = it }, label = { Text("Calories (kcal)") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onSave(activity, minutes.toIntOrNull() ?: 30, distance.toFloatOrNull() ?: 0f, calories.toIntOrNull() ?: 0, notes) }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddRecordDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Laboratory Result") }
    var date by remember { mutableStateOf("2026-10-07") }
    var clinic by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("Laboratory Result", "Medical Report", "Prescription", "Vaccination", "Imaging", "Doctor Note")

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Upload Health Record", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Document Title *") }, modifier = Modifier.fillMaxWidth())
                Text("Category", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.take(3).forEach { c ->
                        FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c, fontSize = 10.sp) })
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.drop(3).forEach { c ->
                        FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c, fontSize = 10.sp) })
                    }
                }
                OutlinedTextField(value = clinic, onValueChange = { clinic = it }, label = { Text("Doctor / Lab / Facility") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Key Findings / Doctor's Notes") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) onAdd(name.trim(), category, date, clinic, notes)
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Text("Save Record")
                    }
                }
            }
        }
    }
}
