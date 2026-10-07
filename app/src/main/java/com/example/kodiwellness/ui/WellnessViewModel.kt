package com.example.kodiwellness.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kodiwellness.data.local.AppDatabase
import com.example.kodiwellness.data.model.*
import com.example.kodiwellness.data.repository.WellnessRepository
import com.example.kodiwellness.notifications.NotificationHelper
import com.example.kodiwellness.update.AppReleaseInfo
import com.example.kodiwellness.update.GitHubUpdateManager
import com.example.kodiwellness.update.UpdateStatus
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class WellnessViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WellnessRepository
    private val todayStr: String
    val updateManager = GitHubUpdateManager(application)

    val updateStatus: StateFlow<UpdateStatus> = updateManager.updateStatus
    private val _currentGitHubRepo = MutableStateFlow(updateManager.getRepository())
    val currentGitHubRepo: StateFlow<String> = _currentGitHubRepo.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = WellnessRepository(db.wellnessDao())
        todayStr = repository.getTodayString()
        NotificationHelper.createNotificationChannels(application)

        // Sync with currently authenticated Google Firebase user
        syncFirebaseUser()

        // Automatically check for GitHub updates in background on app startup
        viewModelScope.launch {
            updateManager.checkForUpdates(manual = false)
        }
    }

    /**
     * Synchronizes current Google account credentials from Firebase Auth with local Room DB
     * and Firestore cloud database, ensuring the user's real name and details address them everywhere.
     */
    fun syncFirebaseUser() {
        val fbUser = com.google.firebase.Firebase.auth.currentUser ?: return
        viewModelScope.launch {
            val existing = repository.user.firstOrNull()
            val googleName = fbUser.displayName?.trim().orEmpty()
            val googleEmail = fbUser.email?.trim().orEmpty()
            val googlePhoto = fbUser.photoUrl?.toString().orEmpty()

            val updatedUser = if (existing == null) {
                UserEntity(
                    id = 1,
                    fullName = googleName.ifBlank { "User" },
                    email = googleEmail,
                    photoUrl = googlePhoto,
                    isOnboarded = googleName.isNotBlank()
                )
            } else {
                existing.copy(
                    fullName = if (existing.fullName.isBlank() || existing.fullName == "John Doe") {
                        googleName.ifBlank { "User" }
                    } else existing.fullName,
                    email = if (existing.email.isBlank() || existing.email.contains("example.com") || existing.email.contains("wellness.org")) {
                        googleEmail.ifBlank { existing.email }
                    } else existing.email,
                    photoUrl = if (existing.photoUrl.isBlank()) googlePhoto else existing.photoUrl
                )
            }
            repository.saveUser(updatedUser)

            // Sync with Firestore profile in background
            try {
                val firestoreRepo = com.example.kodiwellness.data.firestore.FirestoreHealthRepository(getApplication())
                firestoreRepo.saveUserProfile(
                    com.example.kodiwellness.data.firestore.FirestoreUserProfile(
                        userId = fbUser.uid,
                        displayName = updatedUser.fullName,
                        email = updatedUser.email
                    )
                )
            } catch (e: Exception) {
                android.util.Log.w("WellnessVM", "Firestore profile sync note: ${e.message}")
            }
        }
    }

    // State flows
    val user: StateFlow<UserEntity?> = repository.user
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val insuranceInfo: StateFlow<InsuranceInfoEntity?> = repository.insuranceInfo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeMedications: StateFlow<List<MedicationEntity>> = repository.activeMedications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMedications: StateFlow<List<MedicationEntity>> = repository.allMedications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayMedLogs: StateFlow<List<MedicationLogEntity>> = repository.getMedicationLogsForDate(todayStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingAppointments: StateFlow<List<AppointmentEntity>> = repository.upcomingAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAppointments: StateFlow<List<AppointmentEntity>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTasks: StateFlow<List<WellnessTaskEntity>> = repository.getWellnessTasksForDate(todayStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val symptoms: StateFlow<List<SymptomLogEntity>> = repository.allSymptoms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val healthRecords: StateFlow<List<HealthRecordEntity>> = repository.allHealthRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayWaterLogs: StateFlow<List<WaterLogEntity>> = repository.getWaterLogsForDate(todayStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todaySleepLog: StateFlow<SleepLogEntity?> = repository.getSleepLogForDate(todayStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allSleepLogs: StateFlow<List<SleepLogEntity>> = repository.allSleepLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exerciseLogs: StateFlow<List<ExerciseLogEntity>> = repository.allExerciseLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weightLogs: StateFlow<List<WeightLogEntity>> = repository.allWeightLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderEntity>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gamification: StateFlow<GamificationEntity?> = repository.gamification
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Interactive States
    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _showCorrelationPrompt = MutableStateFlow<SymptomLogEntity?>(null)
    val showCorrelationPrompt: StateFlow<SymptomLogEntity?> = _showCorrelationPrompt.asStateFlow()

    private val _isAnalyzingCorrelation = MutableStateFlow(false)
    val isAnalyzingCorrelation: StateFlow<Boolean> = _isAnalyzingCorrelation.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    // Daily Wellness Score (0 - 100%)
    val wellnessScore: StateFlow<Int> = combine(
        todayTasks,
        todayWaterLogs,
        todaySleepLog,
        todayMedLogs,
        activeMedications
    ) { tasks, waters, sleep, medLogs, meds ->
        var score = 0
        // Tasks completion (up to 35 pts)
        if (tasks.isNotEmpty()) {
            val completed = tasks.count { it.status == "COMPLETED" }
            score += ((completed.toFloat() / tasks.size) * 35).toInt()
        } else {
            score += 25
        }
        // Hydration (up to 25 pts, goal 2000ml)
        val waterTotal = waters.sumOf { it.amountMl }
        score += ((waterTotal.coerceAtMost(2000) / 2000f) * 25).toInt()
        // Sleep (up to 20 pts, goal 8h)
        val sleepHours = sleep?.durationHours ?: 7.0f
        score += ((sleepHours.coerceAtMost(8f) / 8f) * 20).toInt()
        // Medication adherence (up to 20 pts)
        if (meds.isNotEmpty()) {
            val taken = medLogs.count { it.status == "TAKEN" }
            score += ((taken.coerceAtMost(meds.size).toFloat() / meds.size) * 20).toInt()
        } else {
            score += 20
        }
        score.coerceIn(10, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 85)

    // Actions
    fun completeOnboarding(
        name: String,
        email: String,
        phone: String,
        dob: String,
        gender: String,
        bloodGroup: String,
        height: Float,
        weight: Float,
        emergencyName: String,
        emergencyPhone: String,
        emergencyRel: String,
        focusAreas: List<String>
    ) {
        viewModelScope.launch {
            val updated = (user.value ?: UserEntity()).copy(
                fullName = name,
                email = email,
                phone = phone,
                dateOfBirth = dob,
                gender = gender,
                bloodGroup = bloodGroup,
                heightCm = height,
                weightKg = weight,
                emergencyContactName = emergencyName,
                emergencyContactPhone = emergencyPhone,
                emergencyRelationship = emergencyRel,
                focusAreas = focusAreas.joinToString(", "),
                isOnboarded = true
            )
            repository.saveUser(updated)
            repository.awardPoints(50, "onboarding_completed")
            _snackbarMessage.value = "Welcome to Kodi Wellness! Your profile is ready."
        }
    }

    fun updateProfile(user: UserEntity) {
        viewModelScope.launch {
            repository.saveUser(user)
            _snackbarMessage.value = "Profile updated successfully."
        }
    }

    fun updateInsurance(info: InsuranceInfoEntity) {
        viewModelScope.launch {
            repository.saveInsuranceInfo(info)
            _snackbarMessage.value = "Health insurance information saved securely."
        }
    }

    // Medication Actions
    fun addMedication(med: MedicationEntity) {
        viewModelScope.launch {
            repository.addMedication(med)
            NotificationHelper.showGeneralNotification(
                getApplication(),
                101,
                "Medication Schedule Added",
                "${med.name} (${med.dosage}) scheduled for ${med.scheduledTimes}."
            )
            _snackbarMessage.value = "Added medication: ${med.name}"
        }
    }

    fun deleteMedication(med: MedicationEntity) {
        viewModelScope.launch {
            repository.deleteMedication(med)
            _snackbarMessage.value = "Removed ${med.name} from medications."
        }
    }

    fun logMedicationAction(med: MedicationEntity, status: String) {
        viewModelScope.launch {
            repository.logMedicationAction(
                medicationId = med.id,
                medicationName = med.name,
                scheduledTime = med.scheduledTimes,
                status = status
            )
            val msg = when (status) {
                "TAKEN" -> "Logged ${med.name} as Taken (+25 pts)!"
                "SKIPPED" -> "Marked ${med.name} as Skipped."
                else -> "Snoozed ${med.name} reminder for 30 minutes."
            }
            _snackbarMessage.value = msg
        }
    }

    // Appointment Actions
    fun addAppointment(appt: AppointmentEntity) {
        viewModelScope.launch {
            repository.addAppointment(appt)
            _snackbarMessage.value = "Appointment scheduled with ${appt.doctorName}."
        }
    }

    fun updateAppointmentStatus(appt: AppointmentEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateAppointment(appt.copy(status = newStatus))
            _snackbarMessage.value = "Appointment updated to $newStatus."
        }
    }

    // Wellness Tasks
    fun addWellnessTask(title: String, category: String, time: String, duration: Int) {
        viewModelScope.launch {
            repository.addWellnessTask(
                WellnessTaskEntity(
                    title = title,
                    category = category,
                    timeOfDay = time,
                    dateString = todayStr,
                    durationMinutes = duration,
                    points = 15,
                    status = "PENDING"
                )
            )
            _snackbarMessage.value = "Added wellness activity."
        }
    }

    fun toggleTaskStatus(task: WellnessTaskEntity) {
        viewModelScope.launch {
            val newStatus = if (task.status == "COMPLETED") "PENDING" else "COMPLETED"
            repository.updateWellnessTask(task.copy(status = newStatus))
            if (newStatus == "COMPLETED") {
                _snackbarMessage.value = "Task completed (+${task.points} pts)!"
            }
        }
    }

    // Symptom Tracker & Correlation Analysis
    fun logSymptom(
        symptomName: String,
        severity: Int,
        severityLabel: String,
        duration: String,
        notes: String
    ) {
        viewModelScope.launch {
            val id = repository.logSymptom(symptomName, severity, severityLabel, duration, notes)
            val newEntry = SymptomLogEntity(
                id = id,
                symptomName = symptomName,
                severity = severity,
                severityLabel = severityLabel,
                dateString = todayStr,
                timeString = repository.getCurrentTimeString(),
                duration = duration,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
            // Trigger prompt to user: "Would you like to analyze potential correlations?"
            _showCorrelationPrompt.value = newEntry
            _snackbarMessage.value = "Symptom logged. Correlation analysis ready."
        }
    }

    fun dismissCorrelationPrompt() {
        _showCorrelationPrompt.value = null
    }

    fun analyzeCorrelationsForSymptom(symptom: SymptomLogEntity) {
        viewModelScope.launch {
            _isAnalyzingCorrelation.value = true
            val result = repository.analyzeAndSaveCorrelations(symptom)
            _isAnalyzingCorrelation.value = false
            _showCorrelationPrompt.value = null
            _snackbarMessage.value = "Correlation analysis completed!"
        }
    }

    // Hydration
    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.logWater(amountMl)
            _snackbarMessage.value = "Added +$amountMl ml water (+10 pts)!"
        }
    }

    // Sleep
    fun logSleep(bedtime: String, wakeTime: String, durationHours: Float, quality: Int, notes: String) {
        viewModelScope.launch {
            repository.logSleep(bedtime, wakeTime, durationHours, quality, notes)
            _snackbarMessage.value = "Logged sleep: $durationHours hours (+15 pts)!"
        }
    }

    // Exercise
    fun logExercise(activity: String, duration: Int, distance: Float, calories: Int, notes: String) {
        viewModelScope.launch {
            repository.logExercise(activity, duration, distance, calories, notes)
            _snackbarMessage.value = "Logged $duration mins of $activity (+25 pts)!"
        }
    }

    // Weight
    fun logWeight(weight: Float, notes: String) {
        viewModelScope.launch {
            repository.logWeight(weight, notes)
            _snackbarMessage.value = "Recorded weight: $weight kg."
        }
    }

    // Health Records
    fun addHealthRecord(
        documentName: String,
        category: String,
        date: String,
        doctorClinic: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addHealthRecord(
                HealthRecordEntity(
                    documentName = documentName,
                    category = category,
                    dateString = date,
                    doctorOrHospital = doctorClinic,
                    notes = notes,
                    fileType = "PDF",
                    isSecure = true
                )
            )
            _snackbarMessage.value = "Medical document stored securely."
        }
    }

    // Reminders
    fun addReminder(title: String, category: String, time: String, freq: String) {
        viewModelScope.launch {
            repository.addReminder(
                ReminderEntity(
                    title = title,
                    category = category,
                    timeString = time,
                    frequency = freq,
                    isEnabled = true
                )
            )
            _snackbarMessage.value = "Reminder set for $time."
        }
    }

    // Wellness AI Chat
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isAiThinking.value = true
            try {
                repository.sendChatMessage(text)
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChat()
            _snackbarMessage.value = "Chat history cleared."
        }
    }

    // Generate AI Daily Wellness Plan
    fun generateAiDailyPlan() {
        viewModelScope.launch {
            _isAiThinking.value = true
            try {
                val count = repository.generateAndApplyAiPlan()
                _snackbarMessage.value = "Generated and added $count daily wellness activities (+20 pts)!"
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    // GitHub In-App Updater Operations
    fun checkForUpdates(manual: Boolean = true) {
        viewModelScope.launch {
            val result = updateManager.checkForUpdates(manual = manual)
            if (manual) {
                when (result) {
                    is UpdateStatus.UpToDate -> {
                        _snackbarMessage.value = "You are on the latest production release (v${result.currentVersion})."
                    }
                    is UpdateStatus.UpdateAvailable -> {
                        _snackbarMessage.value = "New production update found: v${result.release.versionName}!"
                    }
                    is UpdateStatus.Error -> {
                        _snackbarMessage.value = "Update check failed: ${result.message}"
                    }
                    else -> {}
                }
            }
        }
    }

    fun downloadUpdate(release: AppReleaseInfo) {
        viewModelScope.launch {
            val downloadResult = updateManager.downloadUpdate(release) { progress, downloaded, total ->
                // Progress updated inside manager StateFlow
            }
            if (downloadResult.isSuccess) {
                val file = downloadResult.getOrThrow()
                _snackbarMessage.value = "Update v${release.versionName} downloaded! Tap to install."
                updateManager.installUpdate(file)
            } else {
                _snackbarMessage.value = "Download failed: ${downloadResult.exceptionOrNull()?.message}"
            }
        }
    }

    fun installDownloadedUpdate(file: File) {
        val success = updateManager.installUpdate(file)
        if (!success) {
            _snackbarMessage.value = "Please grant permission to install updates from Kodi Wellness in Settings."
        }
    }

    fun openUpdateInBrowser(url: String) {
        updateManager.openInBrowser(url)
    }

    fun setGitHubRepo(repo: String) {
        updateManager.setRepository(repo)
        _currentGitHubRepo.value = updateManager.getRepository()
        _snackbarMessage.value = "Connected repository updated to: ${_currentGitHubRepo.value}"
        checkForUpdates(manual = true)
    }

    fun dismissUpdatePrompt() {
        updateManager.dismissUpdate()
    }
}
