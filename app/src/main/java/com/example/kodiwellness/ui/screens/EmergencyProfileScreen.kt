package com.example.kodiwellness.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kodiwellness.ui.WellnessViewModel
import com.example.ui.theme.HealthAlert

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyProfileScreen(
    viewModel: WellnessViewModel,
    onBack: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val activeMeds by viewModel.activeMedications.collectAsState()
    val context = LocalContext.current

    var showEmergencyConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Health Profile", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("emergency_profile_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // High Visibility SOS Action Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = HealthAlert.copy(alpha = 0.12f)),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(HealthAlert),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Emergency, contentDescription = null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Emergency Medical Dispatch (911)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = HealthAlert)
                    Text("In life-threatening situations, dial local emergency services immediately.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showEmergencyConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = HealthAlert),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("call_911_btn")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dial Emergency Services (911)", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Emergency Contacts Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContactPhone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Primary Emergency Contact", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(user?.emergencyContactName ?: "Sarah Doe", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Text("Relationship: ${user?.emergencyRelationship ?: "Spouse"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Phone: ${user?.emergencyContactPhone ?: "+1 (555) 987-6543"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    FilledTonalButton(
                        onClick = {
                            val phone = user?.emergencyContactPhone ?: "+15559876543"
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call Emergency Contact")
                    }
                }
            }

            // Critical Vitals Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Critical Medical Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    EmergencyDetailRow("Blood Type", user?.bloodGroup ?: "O+", isBold = true)
                    EmergencyDetailRow("Allergies", user?.allergies ?: "Penicillin, Peanuts", isAlert = true)
                    EmergencyDetailRow("Medical Conditions", user?.medicalConditions ?: "Mild Asthma, Hypertension (controlled)")
                    EmergencyDetailRow("Primary Care Physician", user?.primaryDoctor ?: "Dr. Robert Vance, MD")
                    EmergencyDetailRow("Organ Donor Status", if (user?.organDonor == true) "Registered Organ Donor" else "Not Specified")
                }
            }

            // Current Active Medications
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Active Prescription Medications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (activeMeds.isEmpty()) {
                        Text("None recorded", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        activeMeds.forEach { med ->
                            Text("• ${med.name} (${med.dosage}) - ${med.scheduledTimes}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        // Accidental Call Prevention Dialog
        if (showEmergencyConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showEmergencyConfirmDialog = false },
                title = { Text("Confirm Emergency Call", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to dial 911? This will open your device dialer with the emergency dispatcher number.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showEmergencyConfirmDialog = false
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HealthAlert)
                    ) {
                        Text("Yes, Dial 911")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEmergencyConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun EmergencyDetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isAlert: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold || isAlert) FontWeight.Bold else FontWeight.Normal,
            color = if (isAlert) HealthAlert else MaterialTheme.colorScheme.onSurface
        )
    }
}
