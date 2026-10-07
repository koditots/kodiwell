package com.example.kodiwellness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.window.Dialog
import com.example.kodiwellness.ui.WellnessViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: WellnessViewModel,
    onBack: () -> Unit
) {
    val medications by viewModel.allMedications.collectAsState()
    val appointments by viewModel.allAppointments.collectAsState()
    val tasks by viewModel.todayTasks.collectAsState()
    val reminders by viewModel.reminders.collectAsState()

    var viewMode by remember { mutableStateOf("Day View") } // "Day View", "Week View", "Month View"
    var selectedEvent by remember { mutableStateOf<CalendarItem?>(null) }
    val primaryColor = MaterialTheme.colorScheme.primary

    // Combine all events
    val calendarItems = remember(medications, appointments, tasks, reminders, primaryColor) {
        val list = mutableListOf<CalendarItem>()

        // Appointments
        appointments.forEach { appt ->
            list.add(
                CalendarItem(
                    id = "appt_${appt.id}",
                    title = appt.reason,
                    subtitle = "${appt.doctorName} • ${appt.hospitalClinic}",
                    time = appt.appointmentTime,
                    date = appt.appointmentDate,
                    category = "Appointment",
                    icon = Icons.Default.MedicalServices,
                    color = HealthInfo,
                    details = "Doctor: ${appt.doctorName}\nClinic: ${appt.hospitalClinic}\nDepartment: ${appt.department}\nStatus: ${appt.status}\nNotes: ${appt.notes}"
                )
            )
        }

        // Medications
        medications.forEach { med ->
            list.add(
                CalendarItem(
                    id = "med_${med.id}",
                    title = "Take ${med.name} (${med.dosage})",
                    subtitle = "${med.form} • ${med.instructions}",
                    time = med.scheduledTimes,
                    date = "Today & Daily",
                    category = "Medication",
                    icon = Icons.Default.Medication,
                    color = primaryColor,
                    details = "Medication: ${med.name}\nDosage: ${med.dosage}\nFrequency: ${med.frequency}\nInstructions: ${med.instructions}\nDoctor: ${med.doctorName}\nPharmacy: ${med.pharmacy}"
                )
            )
        }

        // Tasks
        tasks.forEach { t ->
            list.add(
                CalendarItem(
                    id = "task_${t.id}",
                    title = t.title,
                    subtitle = "${t.category} • ${t.durationMinutes} mins",
                    time = t.timeOfDay,
                    date = t.dateString,
                    category = "Wellness Task",
                    icon = Icons.Default.Checklist,
                    color = HealthGood,
                    details = "Task: ${t.title}\nCategory: ${t.category}\nPoints: +${t.points} pts\nStatus: ${t.status}"
                )
            )
        }

        // Reminders
        reminders.forEach { r ->
            list.add(
                CalendarItem(
                    id = "rem_${r.id}",
                    title = r.title,
                    subtitle = "${r.category} • ${r.frequency}",
                    time = r.timeString,
                    date = "Recurring",
                    category = "Reminder",
                    icon = Icons.Default.NotificationsActive,
                    color = HealthWarning,
                    details = "Reminder: ${r.title}\nCategory: ${r.category}\nScheduled: ${r.timeString}\nFrequency: ${r.frequency}"
                )
            )
        }

        list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Calendar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("calendar_screen")
        ) {
            // View Mode Selector (Day / Week / Month)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Day View", "Week View", "Month View").forEach { mode ->
                    FilterChip(
                        selected = viewMode == mode,
                        onClick = { viewMode = mode },
                        label = { Text(mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Legend Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendBadge(name = "Medication", color = MaterialTheme.colorScheme.primary)
                LegendBadge(name = "Doctor Visit", color = HealthInfo)
                LegendBadge(name = "Wellness", color = HealthGood)
                LegendBadge(name = "Reminder", color = HealthWarning)
            }

            Divider(modifier = Modifier.padding(vertical = 6.dp))

            // Calendar Events Timeline
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (calendarItems.isEmpty()) {
                    item {
                        Text("No scheduled items found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(calendarItems) { item ->
                        CalendarItemCard(
                            item = item,
                            onClick = { selectedEvent = item }
                        )
                    }
                }
            }
        }

        // Event Detail Dialog
        selectedEvent?.let { item ->
            Dialog(onDismissRequest = { selectedEvent = null }) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(item.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(item.icon, contentDescription = null, tint = item.color)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${item.category} • ${item.time}", style = MaterialTheme.typography.labelSmall, color = item.color, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Divider()
                        Text(item.details, style = MaterialTheme.typography.bodyMedium)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(onClick = { selectedEvent = null }) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendBadge(name: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CalendarItemCard(
    item: CalendarItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = item.color)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = item.color.copy(alpha = 0.12f)
            ) {
                Text(
                    text = item.time,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = item.color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

private data class CalendarItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val time: String,
    val date: String,
    val category: String,
    val icon: ImageVector,
    val color: Color,
    val details: String
)
