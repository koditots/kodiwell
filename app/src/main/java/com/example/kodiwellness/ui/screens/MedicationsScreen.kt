package com.example.kodiwellness.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.kodiwellness.data.model.MedicationEntity
import com.example.kodiwellness.ui.WellnessViewModel
import com.example.ui.theme.HealthAlert
import com.example.ui.theme.HealthGood
import com.example.ui.theme.HealthWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationsScreen(
    viewModel: WellnessViewModel,
    onBack: () -> Unit
) {
    val medications by viewModel.allMedications.collectAsState()
    val todayLogs by viewModel.todayMedLogs.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val takenCount = todayLogs.count { it.status == "TAKEN" }
    val adherencePercent = if (medications.isNotEmpty()) {
        ((takenCount.toFloat() / medications.size.toFloat()) * 100).toInt().coerceIn(0, 100)
    } else 100

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medication Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("add_medication_icon_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Medication")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_medication_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Medication")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Adherence Stats Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Today's Adherence", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$takenCount of ${medications.size} scheduled doses taken", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        }
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$adherencePercent%", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Active Prescriptions & Supplements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (medications.isEmpty()) {
                item {
                    Text("No medications logged yet. Tap '+' to add a medication.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(medications) { med ->
                    val status = todayLogs.firstOrNull { it.medicationId == med.id }?.status ?: "PENDING"
                    MedicationItemCard(
                        medication = med,
                        todayStatus = status,
                        onTake = { viewModel.logMedicationAction(med, "TAKEN") },
                        onSkip = { viewModel.logMedicationAction(med, "SKIPPED") },
                        onSnooze = { viewModel.logMedicationAction(med, "SNOOZE") }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddMedicationDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { newMed ->
                    viewModel.addMedication(newMed)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun MedicationItemCard(
    medication: MedicationEntity,
    todayStatus: String,
    onTake: () -> Unit,
    onSkip: () -> Unit,
    onSnooze: () -> Unit
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
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Medication, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(medication.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("${medication.dosage} • ${medication.form}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Status Badge
                val (badgeText, badgeColor) = when (todayStatus) {
                    "TAKEN" -> "Taken" to HealthGood
                    "SKIPPED" -> "Skipped" to HealthAlert
                    "SNOOZE" -> "Snoozed" to HealthWarning
                    else -> "Scheduled" to MaterialTheme.colorScheme.primary
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Schedule: ${medication.scheduledTimes} (${medication.frequency})", style = MaterialTheme.typography.bodySmall)
            }

            if (medication.instructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(medication.instructions, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (medication.doctorName.isNotBlank() || medication.pharmacy.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rx: ${medication.doctorName} • Pharmacy: ${medication.pharmacy}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTake,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (todayStatus == "TAKEN") HealthGood else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (todayStatus == "TAKEN") "Taken ✓" else "Take")
                }
                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier.weight(0.7f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Snooze")
                }
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(0.7f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Skip")
                }
            }
        }
    }
}

@Composable
private fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onAdd: (MedicationEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var form by remember { mutableStateOf("Tablet") }
    var frequency by remember { mutableStateOf("Once daily") }
    var time by remember { mutableStateOf("08:00 AM") }
    var instructions by remember { mutableStateOf("Take with a glass of water") }
    var doctor by remember { mutableStateOf("") }
    var pharmacy by remember { mutableStateOf("") }

    val forms = listOf("Tablet", "Capsule", "Syrup", "Injection", "Inhaler", "Drops")
    val frequencies = listOf("Once daily", "Twice daily", "Three times daily", "As needed")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Add New Medication", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medication Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("med_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage (e.g. 500mg, 10ml) *") },
                    modifier = Modifier.fillMaxWidth().testTag("med_dosage_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Form", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    forms.take(3).forEach { f ->
                        FilterChip(
                            selected = form == f,
                            onClick = { form = f },
                            label = { Text(f, fontSize = 11.sp) }
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    forms.drop(3).forEach { f ->
                        FilterChip(
                            selected = form == f,
                            onClick = { form = f },
                            label = { Text(f, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Scheduled Time") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = doctor,
                    onValueChange = { doctor = it },
                    label = { Text("Prescribing Doctor") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = pharmacy,
                    onValueChange = { pharmacy = it },
                    label = { Text("Pharmacy") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onAdd(
                                    MedicationEntity(
                                        name = name.trim(),
                                        dosage = dosage.ifBlank { "Standard dose" },
                                        form = form,
                                        frequency = frequency,
                                        scheduledTimes = time,
                                        startDate = "2026-10-07",
                                        instructions = instructions,
                                        doctorName = doctor,
                                        pharmacy = pharmacy
                                    )
                                )
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.testTag("save_medication_btn")
                    ) {
                        Text("Save Schedule")
                    }
                }
            }
        }
    }
}
