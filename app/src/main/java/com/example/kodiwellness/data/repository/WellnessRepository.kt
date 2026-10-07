package com.example.kodiwellness.data.repository

import com.example.kodiwellness.data.local.WellnessDao
import com.example.kodiwellness.data.model.*
import com.example.kodiwellness.data.remote.GeminiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WellnessRepository(
    private val dao: WellnessDao,
    private val geminiService: GeminiService = GeminiService()
) {
    // Current date helper
    fun getTodayString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    fun getCurrentTimeString(): String =
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

    // User & Profile
    val user: Flow<UserEntity?> = dao.getUser()
    suspend fun saveUser(user: UserEntity) = dao.insertOrUpdateUser(user)

    // Health Insurance
    val insuranceInfo: Flow<InsuranceInfoEntity?> = dao.getInsuranceInfo()
    suspend fun saveInsuranceInfo(info: InsuranceInfoEntity) = dao.insertOrUpdateInsurance(info)

    // Medications
    val activeMedications: Flow<List<MedicationEntity>> = dao.getActiveMedications()
    val allMedications: Flow<List<MedicationEntity>> = dao.getAllMedications()
    suspend fun addMedication(med: MedicationEntity): Long = dao.insertMedication(med)
    suspend fun updateMedication(med: MedicationEntity) = dao.updateMedication(med)
    suspend fun deleteMedication(med: MedicationEntity) = dao.deleteMedication(med)

    // Medication Logs & Adherence
    fun getMedicationLogsForDate(date: String): Flow<List<MedicationLogEntity>> =
        dao.getMedicationLogsForDate(date)
    val recentMedicationLogs: Flow<List<MedicationLogEntity>> = dao.getRecentMedicationLogs()

    suspend fun logMedicationAction(
        medicationId: Long,
        medicationName: String,
        scheduledTime: String,
        status: String
    ) {
        dao.insertMedicationLog(
            MedicationLogEntity(
                medicationId = medicationId,
                medicationName = medicationName,
                scheduledTime = scheduledTime,
                dateString = getTodayString(),
                status = status,
                loggedAt = System.currentTimeMillis()
            )
        )
        if (status == "TAKEN") {
            awardPoints(25, "med_dose")
            checkMedicationStreak()
        }
    }

    // Appointments
    val allAppointments: Flow<List<AppointmentEntity>> = dao.getAllAppointments()
    val upcomingAppointments: Flow<List<AppointmentEntity>> = dao.getUpcomingAppointments()
    suspend fun addAppointment(appt: AppointmentEntity): Long = dao.insertAppointment(appt)
    suspend fun updateAppointment(appt: AppointmentEntity) = dao.updateAppointment(appt)
    suspend fun deleteAppointment(appt: AppointmentEntity) = dao.deleteAppointment(appt)

    // Wellness Tasks
    fun getWellnessTasksForDate(date: String): Flow<List<WellnessTaskEntity>> =
        dao.getWellnessTasksForDate(date)
    val allWellnessTasks: Flow<List<WellnessTaskEntity>> = dao.getAllWellnessTasks()
    suspend fun addWellnessTask(task: WellnessTaskEntity): Long = dao.insertWellnessTask(task)
    suspend fun addWellnessTasks(tasks: List<WellnessTaskEntity>) = dao.insertWellnessTasks(tasks)
    suspend fun updateWellnessTask(task: WellnessTaskEntity) {
        dao.updateWellnessTask(task)
        if (task.status == "COMPLETED") {
            awardPoints(task.points, "task_done")
        }
    }
    suspend fun deleteWellnessTask(task: WellnessTaskEntity) = dao.deleteWellnessTask(task)

    // Symptoms & Correlation Analysis
    val allSymptoms: Flow<List<SymptomLogEntity>> = dao.getAllSymptoms()
    fun getSymptomsForDate(date: String): Flow<List<SymptomLogEntity>> = dao.getSymptomsForDate(date)

    suspend fun logSymptom(
        symptomName: String,
        severity: Int,
        severityLabel: String,
        duration: String,
        notes: String
    ): Long {
        val id = dao.insertSymptom(
            SymptomLogEntity(
                symptomName = symptomName,
                severity = severity,
                severityLabel = severityLabel,
                dateString = getTodayString(),
                timeString = getCurrentTimeString(),
                duration = duration,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
        )
        awardPoints(10, "symptom_logged")
        return id
    }

    suspend fun analyzeAndSaveCorrelations(symptomLog: SymptomLogEntity): String {
        val today = getTodayString()
        val waters = dao.getWaterLogsForDate(today).firstOrNull() ?: emptyList()
        val totalWater = waters.sumOf { it.amountMl }
        val sleep = dao.getSleepLogForDate(today).firstOrNull()
        val sleepHours = sleep?.durationHours ?: 7.0f
        val exercises = dao.getExerciseLogsForDate(today).firstOrNull() ?: emptyList()
        val exerciseMins = exercises.sumOf { it.durationMinutes }
        val meds = dao.getActiveMedications().firstOrNull() ?: emptyList()
        val medNames = meds.map { it.name }

        val correlationResult = geminiService.analyzeSymptomCorrelations(
            symptom = symptomLog.symptomName,
            severity = symptomLog.severity,
            recentWaterMl = totalWater,
            recentSleepHours = sleepHours,
            recentExerciseMinutes = exerciseMins,
            recentMedications = medNames
        )

        dao.updateSymptom(symptomLog.copy(potentialCorrelations = correlationResult))
        return correlationResult
    }

    // Health Records
    val allHealthRecords: Flow<List<HealthRecordEntity>> = dao.getAllHealthRecords()
    suspend fun addHealthRecord(record: HealthRecordEntity): Long = dao.insertHealthRecord(record)
    suspend fun deleteHealthRecord(record: HealthRecordEntity) = dao.deleteHealthRecord(record)

    // Water Logs
    fun getWaterLogsForDate(date: String): Flow<List<WaterLogEntity>> = dao.getWaterLogsForDate(date)
    suspend fun logWater(amountMl: Int): Long {
        val id = dao.insertWaterLog(
            WaterLogEntity(
                amountMl = amountMl,
                dateString = getTodayString(),
                timeString = getCurrentTimeString(),
                timestamp = System.currentTimeMillis()
            )
        )
        awardPoints(10, "water_drink")
        checkHydrationStreak()
        return id
    }

    // Sleep Logs
    val allSleepLogs: Flow<List<SleepLogEntity>> = dao.getAllSleepLogs()
    fun getSleepLogForDate(date: String): Flow<SleepLogEntity?> = dao.getSleepLogForDate(date)
    suspend fun logSleep(
        bedtime: String,
        wakeTime: String,
        durationHours: Float,
        qualityRating: Int,
        notes: String
    ): Long {
        val id = dao.insertSleepLog(
            SleepLogEntity(
                dateString = getTodayString(),
                bedtime = bedtime,
                wakeTime = wakeTime,
                durationHours = durationHours,
                qualityRating = qualityRating,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
        )
        awardPoints(15, "sleep_logged")
        return id
    }

    // Exercise Logs
    val allExerciseLogs: Flow<List<ExerciseLogEntity>> = dao.getAllExerciseLogs()
    fun getExerciseLogsForDate(date: String): Flow<List<ExerciseLogEntity>> = dao.getExerciseLogsForDate(date)
    suspend fun logExercise(
        activityType: String,
        durationMinutes: Int,
        distanceKm: Float,
        caloriesBurned: Int,
        notes: String
    ): Long {
        val id = dao.insertExerciseLog(
            ExerciseLogEntity(
                activityType = activityType,
                durationMinutes = durationMinutes,
                distanceKm = distanceKm,
                caloriesBurned = caloriesBurned,
                dateString = getTodayString(),
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
        )
        awardPoints(25, "exercise_done")
        return id
    }

    // Weight Logs
    val allWeightLogs: Flow<List<WeightLogEntity>> = dao.getAllWeightLogs()
    suspend fun logWeight(weightKg: Float, notes: String): Long {
        val id = dao.insertWeightLog(
            WeightLogEntity(
                weightKg = weightKg,
                dateString = getTodayString(),
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
        )
        // Also update profile weight
        val currentUser = dao.getUser().firstOrNull()
        if (currentUser != null) {
            dao.insertOrUpdateUser(currentUser.copy(weightKg = weightKg))
        }
        awardPoints(10, "weight_logged")
        return id
    }

    // Reminders
    val allReminders: Flow<List<ReminderEntity>> = dao.getAllReminders()
    suspend fun addReminder(reminder: ReminderEntity): Long = dao.insertReminder(reminder)
    suspend fun updateReminder(reminder: ReminderEntity) = dao.updateReminder(reminder)
    suspend fun deleteReminder(reminder: ReminderEntity) = dao.deleteReminder(reminder)

    // Gamification System
    val gamification: Flow<GamificationEntity?> = dao.getGamification()

    suspend fun awardPoints(pointsToAdd: Int, actionTag: String) {
        val current = dao.getGamification().firstOrNull() ?: GamificationEntity()
        val newPoints = current.totalPoints + pointsToAdd
        val newLevel = (newPoints / 100) + 1

        val currentBadges = current.unlockedBadges.split(",").filter { it.isNotBlank() }.toMutableSet()
        currentBadges.add("first_step")
        if (newPoints >= 200) currentBadges.add("wellness_pioneer")
        if (newPoints >= 500) currentBadges.add("century_master")
        if (newPoints >= 1000) currentBadges.add("legendary_health")

        dao.insertOrUpdateGamification(
            current.copy(
                totalPoints = newPoints,
                currentLevel = newLevel,
                unlockedBadges = currentBadges.joinToString(","),
                lastActiveDate = getTodayString()
            )
        )
    }

    private suspend fun checkHydrationStreak() {
        val current = dao.getGamification().firstOrNull() ?: GamificationEntity()
        val newStreak = current.hydrationStreak + 1
        val badges = current.unlockedBadges.split(",").filter { it.isNotBlank() }.toMutableSet()
        if (newStreak >= 7) badges.add("hydration_hero")
        dao.insertOrUpdateGamification(current.copy(hydrationStreak = newStreak, unlockedBadges = badges.joinToString(",")))
    }

    private suspend fun checkMedicationStreak() {
        val current = dao.getGamification().firstOrNull() ?: GamificationEntity()
        val newStreak = current.medicationStreak + 1
        val badges = current.unlockedBadges.split(",").filter { it.isNotBlank() }.toMutableSet()
        if (newStreak >= 14) badges.add("medication_champion")
        if (newStreak >= 30) badges.add("first_month_adherence")
        dao.insertOrUpdateGamification(current.copy(medicationStreak = newStreak, unlockedBadges = badges.joinToString(",")))
    }

    // Chat / Wellness AI
    val chatMessages: Flow<List<ChatMessageEntity>> = dao.getAllChatMessages()

    suspend fun sendChatMessage(userText: String): Pair<String, Boolean> {
        // Save user message
        dao.insertChatMessage(
            ChatMessageEntity(
                role = "user",
                content = userText,
                timestamp = System.currentTimeMillis()
            )
        )

        val history = (dao.getAllChatMessages().firstOrNull() ?: emptyList())
            .map { it.role to it.content }

        val currentUser = dao.getUser().firstOrNull()
        val userName = currentUser?.fullName?.takeIf { it.isNotBlank() && it != "John Doe" } ?: ""

        // Generate response
        val (responseContent, isEmergency) = geminiService.generateChatResponse(userText, history, userName)

        // Save AI message
        dao.insertChatMessage(
            ChatMessageEntity(
                role = "model",
                content = responseContent,
                timestamp = System.currentTimeMillis(),
                isEmergencyFlagged = isEmergency
            )
        )

        return Pair(responseContent, isEmergency)
    }

    suspend fun clearChat() = dao.clearChatMessages()

    // Daily AI Wellness Plan Generator
    suspend fun generateAndApplyAiPlan(): Int {
        val userProfile = dao.getUser().firstOrNull()
        val focusAreas = userProfile?.focusAreas ?: "General Wellness"
        val planItems = geminiService.generateDailyWellnessPlan(focusAreas)
        val today = getTodayString()

        val tasks = planItems.map {
            WellnessTaskEntity(
                title = it.title,
                category = it.category,
                timeOfDay = it.timeOfDay,
                dateString = today,
                durationMinutes = it.durationMinutes,
                points = it.points,
                status = "PENDING",
                isAiGenerated = true
            )
        }
        dao.insertWellnessTasks(tasks)
        awardPoints(20, "ai_plan_created")
        return tasks.size
    }
}
