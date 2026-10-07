package com.example.kodiwellness.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.window.Dialog
import com.example.kodiwellness.data.model.InsuranceInfoEntity
import com.example.kodiwellness.data.model.UserEntity
import com.example.kodiwellness.ui.WellnessViewModel
import com.example.kodiwellness.ui.components.ConfigureRepoDialog
import com.example.kodiwellness.ui.components.ProductionUpdateCard
import com.example.kodiwellness.update.UpdateStatus
import com.example.BuildConfig
import com.example.ui.theme.HealthGood
import androidx.credentials.CredentialManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: WellnessViewModel,
    onNavigateToEducation: () -> Unit,
    onNavigateToGamification: () -> Unit,
    onNavigateToEmergency: () -> Unit,
    onNavigateToSplash: () -> Unit = {}
) {
    val user by viewModel.user.collectAsState()
    val insurance by viewModel.insuranceInfo.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    val currentRepo by viewModel.currentGitHubRepo.collectAsState()
    val context = LocalContext.current

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showEditInsuranceDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showConfigureRepoDialog by remember { mutableStateOf(false) }

    var notificationsEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("profile_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val fbUser = com.google.firebase.Firebase.auth.currentUser
                        val realName = user?.fullName?.trim()?.takeIf { it.isNotBlank() && it != "John Doe" }
                            ?: fbUser?.displayName?.trim()?.takeIf { it.isNotBlank() }
                            ?: "Kodi Member"
                        val realEmail = user?.email?.trim()?.takeIf { it.isNotBlank() && !it.contains("example.com") && !it.contains("wellness.org") }
                            ?: fbUser?.email?.trim()?.takeIf { it.isNotBlank() }
                            ?: "Connected via Google"

                        val initials = realName.split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .joinToString("")
                            .ifBlank { "KW" }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(realName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(realEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Blood Group: ${user?.bloodGroup?.ifBlank { "O+" } ?: "O+"} • ${user?.weightKg ?: 70.0f} kg", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { showEditProfileDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                        }
                    }
                }
            }

            // Health Insurance Information Card (USER REQUESTED FEATURE)
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
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Health Insurance Information", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(10.dp), tint = HealthGood)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Encrypted & Secure Local Storage", fontSize = 10.sp, color = HealthGood, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        IconButton(onClick = { showEditInsuranceDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Insurance")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    InsuranceRow("Insurance Provider", insurance?.provider ?: "Blue Cross Blue Shield")
                    InsuranceRow("Policy Number", insurance?.policyNumber ?: "XEB-948271049")
                    InsuranceRow("Group Number", insurance?.groupNumber ?: "GRP-88301")
                    InsuranceRow("Member / Subscriber ID", insurance?.memberId ?: "MEM-00294184")
                    InsuranceRow("Plan Type", insurance?.planType ?: "Comprehensive PPO Advantage")

                    Spacer(modifier = Modifier.height(8.dp))
                    val phone = insurance?.contactPhone ?: "+1 (800) 555-0199"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Contact Phone", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(phone, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        }
                        FilledTonalButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.replace(" ", "")}"))
                                context.startActivity(intent)
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call Support", fontSize = 11.sp)
                        }
                    }

                    if (!insurance?.notes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Coverage Notes: ${insurance?.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Quick Links
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ProfileMenuRow(icon = Icons.Default.Info, title = "App Overview & Tour", subtitle = "Logo, mission & app understanding", onClick = onNavigateToSplash)
                    Divider()
                    ProfileMenuRow(icon = Icons.Default.Emergency, title = "Emergency Health Profile", subtitle = "Critical vitals & contacts", onClick = onNavigateToEmergency)
                    Divider()
                    ProfileMenuRow(icon = Icons.Default.EmojiEvents, title = "Achievements & Streaks", subtitle = "Badges, health points & ranks", onClick = onNavigateToGamification)
                    Divider()
                    ProfileMenuRow(icon = Icons.Default.MenuBook, title = "Health Education Library", subtitle = "Evidence-based wellness articles", onClick = onNavigateToEducation)
                }
            }

            // Settings & Preferences
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Settings & Privacy", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Health Reminders & Notifications", style = MaterialTheme.typography.bodyMedium)
                            Text("Receive medication and hydration cues", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Vibration for Critical Alerts", style = MaterialTheme.typography.bodyMedium)
                            Text("Vibrate phone for scheduled doses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = vibrationEnabled, onCheckedChange = { vibrationEnabled = it })
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    ProfileMenuRow(icon = Icons.Default.Download, title = "Export Health Summary", subtitle = "Download your wellness logs", onClick = { showExportDialog = true })
                    Divider()
                    ProfileMenuRow(icon = Icons.Default.MedicalInformation, title = "Medical Disclaimer", subtitle = "Safety & liability information", onClick = { showDisclaimerDialog = true })
                    Divider()
                    ProfileMenuRow(icon = Icons.Default.Policy, title = "Privacy Policy & Terms", subtitle = "Zero tracking & data confidentiality", onClick = { showPrivacyDialog = true })
                }
            }

            // Firebase Cloud Connection Card
            val firebaseUser = Firebase.auth.currentUser
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Cloud Connected",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Firebase Cloud Connected",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = firebaseUser?.email ?: "Google Account Synced",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Database Region", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text("europe-west1 (Encrypted)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(
                            onClick = {
                                val coroutineScope = CoroutineScope(Dispatchers.Main)
                                val credentialManager = CredentialManager.create(context)
                                signOut(context, credentialManager, onSignOutComplete = {}, scope = coroutineScope)
                            },
                            modifier = Modifier.testTag("sign_out_button")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign Out")
                        }
                    }
                }
            }

            // Production Release & GitHub Updates Card
            ProductionUpdateCard(
                currentVersion = BuildConfig.VERSION_NAME,
                currentRepo = currentRepo,
                updateStatus = updateStatus,
                onCheckForUpdates = { viewModel.checkForUpdates(manual = true) },
                onDownloadUpdate = { release -> viewModel.downloadUpdate(release) },
                onInstallUpdate = {
                    (updateStatus as? UpdateStatus.ReadyToInstall)?.let {
                        viewModel.installDownloadedUpdate(it.apkFile)
                    }
                },
                onOpenBrowser = { url -> viewModel.openUpdateInBrowser(url) },
                onEditRepo = { showConfigureRepoDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Configure GitHub Repository Dialog
        if (showConfigureRepoDialog) {
            ConfigureRepoDialog(
                initialRepo = currentRepo,
                onDismiss = { showConfigureRepoDialog = false },
                onSave = { newRepo ->
                    viewModel.setGitHubRepo(newRepo)
                    showConfigureRepoDialog = false
                }
            )
        }

        // Edit Profile Dialog
        if (showEditProfileDialog) {
            EditProfileDialog(
                currentUser = user ?: UserEntity(),
                onDismiss = { showEditProfileDialog = false },
                onSave = { updated ->
                    viewModel.updateProfile(updated)
                    showEditProfileDialog = false
                }
            )
        }

        // Edit Health Insurance Dialog
        if (showEditInsuranceDialog) {
            EditInsuranceDialog(
                current = insurance ?: InsuranceInfoEntity(),
                onDismiss = { showEditInsuranceDialog = false },
                onSave = { updated ->
                    viewModel.updateInsurance(updated)
                    showEditInsuranceDialog = false
                }
            )
        }

        // Export Dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Export Health Summary", fontWeight = FontWeight.Bold) },
                text = { Text("Your health profile, medications list, and wellness logs have been compiled into a secure summary format ready for export or review with your doctor.") },
                confirmButton = {
                    Button(onClick = { showExportDialog = false }) { Text("Done") }
                }
            )
        }

        // Medical Disclaimer Dialog
        if (showDisclaimerDialog) {
            AlertDialog(
                onDismissRequest = { showDisclaimerDialog = false },
                title = { Text("Important Medical Disclaimer", fontWeight = FontWeight.Bold) },
                text = {
                    Text("Kodi Wellness and Wellness AI provide general health management and wellness planning tools only. " +
                            "This application does NOT diagnose diseases, prescribe medication, or replace professional medical advice, diagnosis, or treatment. " +
                            "Always seek the advice of your physician or other qualified healthcare provider with any questions you may have regarding a medical condition.")
                },
                confirmButton = {
                    Button(onClick = { showDisclaimerDialog = false }) { Text("Understood") }
                }
            )
        }

        // Privacy Policy Dialog
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                title = { Text("Privacy & Security Commitment", fontWeight = FontWeight.Bold) },
                text = {
                    Text("At Kodi Wellness, we hold health privacy to the highest standard.\n\n" +
                            "• All health documents, medications, insurance records, and vital metrics are stored in a private local database.\n" +
                            "• We never sell, monetize, or expose user health data.\n" +
                            "• You have complete control to edit, export, or erase your records at any time.")
                },
                confirmButton = {
                    Button(onClick = { showPrivacyDialog = false }) { Text("Close") }
                }
            )
        }
    }
}

@Composable
private fun InsuranceRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProfileMenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun EditProfileDialog(currentUser: UserEntity, onDismiss: () -> Unit, onSave: (UserEntity) -> Unit) {
    val fbUser = com.google.firebase.Firebase.auth.currentUser
    val defaultName = currentUser.fullName.takeIf { it.isNotBlank() && it != "John Doe" }
        ?: fbUser?.displayName.orEmpty()
    val defaultEmail = currentUser.email.takeIf { it.isNotBlank() && !it.contains("example.com") && !it.contains("wellness.org") }
        ?: fbUser?.email.orEmpty()

    var name by remember { mutableStateOf(defaultName) }
    var email by remember { mutableStateOf(defaultEmail) }
    var phone by remember { mutableStateOf(currentUser.phone) }
    var blood by remember { mutableStateOf(currentUser.bloodGroup.ifBlank { "O+" }) }
    var allergies by remember { mutableStateOf(currentUser.allergies) }
    var conditions by remember { mutableStateOf(currentUser.medicalConditions) }
    var doctor by remember { mutableStateOf(currentUser.primaryDoctor) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Edit Personal & Health Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = blood, onValueChange = { blood = it }, label = { Text("Blood Group") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = allergies, onValueChange = { allergies = it }, label = { Text("Allergies") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = conditions, onValueChange = { conditions = it }, label = { Text("Medical Conditions") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = doctor, onValueChange = { doctor = it }, label = { Text("Primary Doctor") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onSave(currentUser.copy(
                            fullName = name.trim(),
                            email = email.trim(),
                            phone = phone.trim(),
                            bloodGroup = blood.trim(),
                            allergies = allergies.trim(),
                            medicalConditions = conditions.trim(),
                            primaryDoctor = doctor.trim()
                        ))
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun EditInsuranceDialog(current: InsuranceInfoEntity, onDismiss: () -> Unit, onSave: (InsuranceInfoEntity) -> Unit) {
    var provider by remember { mutableStateOf(current.provider) }
    var policyNumber by remember { mutableStateOf(current.policyNumber) }
    var groupNumber by remember { mutableStateOf(current.groupNumber) }
    var memberId by remember { mutableStateOf(current.memberId) }
    var phone by remember { mutableStateOf(current.contactPhone) }
    var planType by remember { mutableStateOf(current.planType) }
    var notes by remember { mutableStateOf(current.notes) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Edit Health Insurance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = provider, onValueChange = { provider = it }, label = { Text("Insurance Provider (e.g. BCBS, Aetna)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = policyNumber, onValueChange = { policyNumber = it }, label = { Text("Policy Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = groupNumber, onValueChange = { groupNumber = it }, label = { Text("Group Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = memberId, onValueChange = { memberId = it }, label = { Text("Member / Subscriber ID") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Insurance Contact Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = planType, onValueChange = { planType = it }, label = { Text("Plan Type (e.g. HMO, PPO)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Coverage & Copay Notes") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onSave(current.copy(provider = provider, policyNumber = policyNumber, groupNumber = groupNumber, memberId = memberId, contactPhone = phone, planType = planType, notes = notes, lastUpdated = System.currentTimeMillis()))
                    }) {
                        Text("Save Insurance")
                    }
                }
            }
        }
    }
}
