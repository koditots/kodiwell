package com.example.kodiwellness.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.kodiwellness.data.model.SymptomLogEntity
import com.example.kodiwellness.ui.WellnessViewModel
import com.example.ui.theme.HealthAlert
import com.example.ui.theme.HealthGood
import com.example.ui.theme.HealthWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomTrackerScreen(
    viewModel: WellnessViewModel,
    onBack: () -> Unit
) {
    val symptoms by viewModel.symptoms.collectAsState()
    val correlationPromptSymptom by viewModel.showCorrelationPrompt.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingCorrelation.collectAsState()

    // Daily Symptom Logging Form State
    var symptomName by remember { mutableStateOf("Headache") }
    var customSymptomText by remember { mutableStateOf("") }
    var severitySlider by remember { mutableFloatStateOf(4f) }
    var dateStamp by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var timeStamp by remember {
        mutableStateOf(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()))
    }
    var duration by remember { mutableStateOf("1 hour") }
    var notes by remember { mutableStateOf("") }
    var formSuccessMessage by remember { mutableStateOf<String?>(null) }

    val commonSymptoms = listOf(
        "Headache", "Fatigue", "Back Pain", "Fever",
        "Cough", "Nausea", "Dizziness", "Stomach Ache",
        "Joint Pain", "Other"
    )

    val durationOptions = listOf("< 30 mins", "1–2 hours", "Half day", "Persistent")

    val severityInt = severitySlider.toInt()
    val severityLabel = when {
        severityInt <= 3 -> "Mild"
        severityInt <= 6 -> "Moderate"
        else -> "Severe"
    }

    val severityColor = when {
        severityInt <= 3 -> HealthGood
        severityInt <= 6 -> HealthWarning
        else -> HealthAlert
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Symptom Tracker", fontWeight = FontWeight.Bold) },
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
                .testTag("symptom_tracker_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Medical Disclaimer Notice
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Daily symptom logs are stored in your local Room database for personal tracking and doctor consultation. Not a medical diagnosis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // PRIMARY SIMPLE FORM: Log Daily Symptom with Severity Scale & Date Stamp
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_symptom_form"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(severityColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Sick, contentDescription = null, tint = severityColor, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Log Daily Symptom", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Room Database Storage", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            // Live Date Stamp Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(dateStamp, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }

                        // Symptom Quick Select Chips
                        Text("Select Symptom", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            commonSymptoms.chunked(4).forEach { rowList ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    rowList.forEach { s ->
                                        FilterChip(
                                            selected = symptomName == s,
                                            onClick = { symptomName = s },
                                            label = { Text(s, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }

                        if (symptomName == "Other") {
                            OutlinedTextField(
                                value = customSymptomText,
                                onValueChange = { customSymptomText = it },
                                label = { Text("Specify Symptom Name *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_symptom_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Severity Scale with Live Color Indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Severity Scale", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = severityColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$severityInt / 10 ($severityLabel)",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = severityColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Slider(
                            value = severitySlider,
                            onValueChange = { severitySlider = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = severityColor,
                                activeTrackColor = severityColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_severity_slider")
                        )

                        // Date Stamp & Time Stamp Inputs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = dateStamp,
                                onValueChange = { dateStamp = it },
                                label = { Text("Date Stamp (YYYY-MM-DD)") },
                                leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("date_stamp_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = timeStamp,
                                onValueChange = { timeStamp = it },
                                label = { Text("Time Stamp") },
                                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("time_stamp_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Duration Chips
                        Text("Duration", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            durationOptions.forEach { d ->
                                FilterChip(
                                    selected = duration == d,
                                    onClick = { duration = d },
                                    label = { Text(d, fontSize = 11.sp) }
                                )
                            }
                        }

                        // Notes Field
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes & Triggers (optional)") },
                            placeholder = { Text("e.g. after screen time, missed hydration") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("symptom_notes_input"),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 3
                        )

                        // Emergency Warning Banner if severity is critical
                        if (severityInt >= 8) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = HealthAlert.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = HealthAlert, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "High severity rating ($severityInt/10). If experiencing chest pain, difficulty breathing, or sudden numbness, call 911 immediately.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HealthAlert
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = formSuccessMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = HealthGood.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = formSuccessMessage ?: "",
                                    color = HealthGood,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        // Primary Save Button
                        Button(
                            onClick = {
                                val finalName = if (symptomName == "Other" && customSymptomText.isNotBlank()) {
                                    customSymptomText.trim()
                                } else {
                                    symptomName
                                }

                                viewModel.logSymptom(
                                    symptomName = finalName,
                                    severity = severityInt,
                                    severityLabel = severityLabel,
                                    duration = duration,
                                    notes = notes
                                )

                                formSuccessMessage = "Saved to Room Database: $finalName ($dateStamp)"
                                notes = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_daily_symptom_btn"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Daily Symptom (Room DB)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // History Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saved Symptoms History (${symptoms.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (symptoms.isEmpty()) {
                item {
                    Text(
                        "No symptom history recorded yet. Use the form above to log your first daily symptom.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(symptoms) { item ->
                    SymptomCard(
                        symptom = item,
                        onAnalyzeCorrelations = {
                            viewModel.analyzeCorrelationsForSymptom(item)
                        }
                    )
                }
            }
        }

        // Post-log AI Correlation Prompt Modal
        correlationPromptSymptom?.let { loggedItem ->
            CorrelationPromptDialog(
                symptom = loggedItem,
                isAnalyzing = isAnalyzing,
                onDismiss = { viewModel.dismissCorrelationPrompt() },
                onAnalyze = { viewModel.analyzeCorrelationsForSymptom(loggedItem) }
            )
        }
    }
}

@Composable
private fun SymptomCard(
    symptom: SymptomLogEntity,
    onAnalyzeCorrelations: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when {
                                    symptom.severity >= 7 -> HealthAlert.copy(alpha = 0.15f)
                                    symptom.severity >= 4 -> HealthWarning.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Sick,
                            contentDescription = null,
                            tint = when {
                                symptom.severity >= 7 -> HealthAlert
                                symptom.severity >= 4 -> HealthWarning
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(symptom.symptomName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Date: ${symptom.dateString} • ${symptom.timeString}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        symptom.severity >= 7 -> HealthAlert.copy(alpha = 0.15f)
                        symptom.severity >= 4 -> HealthWarning.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = "Severity ${symptom.severity}/10",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = when {
                            symptom.severity >= 7 -> HealthAlert
                            symptom.severity >= 4 -> HealthWarning
                            else -> MaterialTheme.colorScheme.primary
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            if (symptom.duration.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Duration: ${symptom.duration}", style = MaterialTheme.typography.bodySmall)
            }

            if (symptom.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Notes: ${symptom.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // AI Correlation Analysis Section
            Spacer(modifier = Modifier.height(10.dp))
            if (symptom.potentialCorrelations.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lifestyle Correlation Analysis", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(symptom.potentialCorrelations, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onAnalyzeCorrelations,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Analyze Lifestyle Correlations with AI")
                }
            }
        }
    }
}

@Composable
private fun CorrelationPromptDialog(
    symptom: SymptomLogEntity,
    isAnalyzing: Boolean,
    onDismiss: () -> Unit,
    onAnalyze: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Analyze Potential Correlations?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "Would you like Wellness AI to inspect your recently logged hydration, sleep, activities, and medications for potential lifestyle correlations with your ${symptom.symptomName}?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Example: 'You've logged headaches after periods of low water intake. Consider increasing your hydration.'\n\n*Not a medical diagnosis.*",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(10.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, enabled = !isAnalyzing) {
                        Text("Not Now")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onAnalyze,
                        enabled = !isAnalyzing,
                        modifier = Modifier.testTag("confirm_analyze_correlation_btn")
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analyzing...")
                        } else {
                            Text("Analyze Correlations")
                        }
                    }
                }
            }
        }
    }
}
