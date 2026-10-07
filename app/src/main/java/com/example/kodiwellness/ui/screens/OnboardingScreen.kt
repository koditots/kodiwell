package com.example.kodiwellness.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kodiwellness.ui.WellnessViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: WellnessViewModel,
    onComplete: () -> Unit
) {
    val currentUserState by viewModel.user.collectAsState()
    val fbUser = Firebase.auth.currentUser

    val defaultDisplayName = fbUser?.displayName?.takeIf { it.isNotBlank() } ?: ""
    val defaultEmailAddress = fbUser?.email?.takeIf { it.isNotBlank() } ?: ""

    val initialName: String = if (!currentUserState?.fullName.isNullOrBlank() && currentUserState?.fullName != "John Doe") {
        currentUserState?.fullName ?: ""
    } else {
        defaultDisplayName
    }

    val initialEmail: String = if (!currentUserState?.email.isNullOrBlank() && !currentUserState!!.email.contains("example.com") && !currentUserState!!.email.contains("wellness.org")) {
        currentUserState?.email ?: ""
    } else {
        defaultEmailAddress
    }

    var step by remember { mutableStateOf(0) }

    // User Form State initialized with real account details
    var fullName by remember(initialName) { mutableStateOf(initialName) }
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }
    var phone by remember { mutableStateOf(currentUserState?.phone.orEmpty()) }
    var dob by remember { mutableStateOf(currentUserState?.dateOfBirth.orEmpty().ifBlank { "1995-01-01" }) }
    var gender by remember { mutableStateOf(currentUserState?.gender.orEmpty().ifBlank { "Prefer not to say" }) }
    var bloodGroup by remember { mutableStateOf(currentUserState?.bloodGroup.orEmpty().ifBlank { "O+" }) }
    var height by remember { mutableStateOf((currentUserState?.heightCm ?: 175f).toInt().toString()) }
    var weight by remember { mutableStateOf((currentUserState?.weightKg ?: 70.0f).toString()) }
    var emergencyName by remember { mutableStateOf(currentUserState?.emergencyContactName.orEmpty()) }
    var emergencyPhone by remember { mutableStateOf(currentUserState?.emergencyContactPhone.orEmpty()) }
    var emergencyRel by remember { mutableStateOf(currentUserState?.emergencyRelationship.orEmpty().ifBlank { "Family" }) }

    val focusOptions = listOf(
        "Medication management",
        "Fitness",
        "Nutrition",
        "Sleep",
        "Mental wellness",
        "Weight management",
        "General wellness",
        "Medical appointments"
    )
    val selectedFocuses = remember { mutableStateListOf("Medication management", "General wellness", "Sleep", "Nutrition") }

    val slides = listOf(
        SlideContent(
            title = "Your Personal Wellness Companion",
            subtitle = "Kodi Wellness",
            description = "Welcome to your all-in-one medical assistance and daily health planning platform designed for longevity, clarity, and peace of mind.",
            icon = Icons.Default.HealthAndSafety
        ),
        SlideContent(
            title = "Stay on Top of Medications & Care",
            subtitle = "Adherence & Schedules",
            description = "Never miss a dose or doctor appointment again with smart reminders, refill tracking, and integrated medical calendars.",
            icon = Icons.Default.Medication
        ),
        SlideContent(
            title = "Build Healthier Daily Habits",
            subtitle = "Mindful Planning",
            description = "Track your water intake, restful sleep, daily movement, and balanced nutrition with personalized wellness plans.",
            icon = Icons.Default.SelfImprovement
        ),
        SlideContent(
            title = "Track Your Wellness Progress",
            subtitle = "Earn Badges & Insights",
            description = "See correlations between habits and symptoms, earn streak badges, and maintain personal health records in one place.",
            icon = Icons.Default.AutoGraph
        ),
        SlideContent(
            title = "Your Health Data Stays Private",
            subtitle = "Secure & Confidential",
            description = "Protected local storage with zero advertising and accessible emergency health information ready when you need it.",
            icon = Icons.Default.Lock
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kodi Wellness", fontWeight = FontWeight.Bold) },
                actions = {
                    if (step < 5) {
                        TextButton(
                            onClick = { step = 5 },
                            modifier = Modifier.testTag("skip_onboarding_btn")
                        ) {
                            Text("Skip")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (step < 5) {
                // Slides 0 to 4
                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = slides[step].icon,
                        contentDescription = slides[step].title,
                        modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = slides[step].subtitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = slides[step].title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = slides[step].description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))
                // Progress Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(6) { index ->
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (index == step) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == step) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = { step++ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("next_onboarding_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (step == 4) "Get Started" else "Continue", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                // Step 5: Profile & Health Focus Selection
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Personalize Your Care",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Set your profile details and select your key wellness priorities.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("name_input"),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = bloodGroup,
                        onValueChange = { bloodGroup = it },
                        label = { Text("Blood Group") },
                        modifier = Modifier.weight(1f).testTag("blood_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (kg)") },
                        modifier = Modifier.weight(1f).testTag("weight_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = emergencyName,
                    onValueChange = { emergencyName = it },
                    label = { Text("Emergency Contact Name") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("emergency_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = emergencyPhone,
                    onValueChange = { emergencyPhone = it },
                    label = { Text("Emergency Contact Phone") },
                    modifier = Modifier.fillMaxWidth().testTag("emergency_phone_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "What would you like to focus on?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Focus area chips
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    focusOptions.chunked(2).forEach { rowItems ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowItems.forEach { option ->
                                val selected = selectedFocuses.contains(option)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        if (selected) selectedFocuses.remove(option)
                                        else selectedFocuses.add(option)
                                    },
                                    label = { Text(option) },
                                    leadingIcon = if (selected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = {
                        viewModel.completeOnboarding(
                            name = fullName,
                            email = email,
                            phone = phone,
                            dob = dob,
                            gender = gender,
                            bloodGroup = bloodGroup,
                            height = height.toFloatOrNull() ?: 178f,
                            weight = weight.toFloatOrNull() ?: 74.5f,
                            emergencyName = emergencyName,
                            emergencyPhone = emergencyPhone,
                            emergencyRel = emergencyRel,
                            focusAreas = selectedFocuses.toList()
                        )
                        onComplete()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("complete_onboarding_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Enter Kodi Wellness", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

private data class SlideContent(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector
)
