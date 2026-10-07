package com.example.kodiwellness.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kodiwellness.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        UserEntity::class,
        InsuranceInfoEntity::class,
        MedicationEntity::class,
        MedicationLogEntity::class,
        AppointmentEntity::class,
        WellnessTaskEntity::class,
        SymptomLogEntity::class,
        HealthRecordEntity::class,
        WaterLogEntity::class,
        SleepLogEntity::class,
        ExerciseLogEntity::class,
        WeightLogEntity::class,
        ReminderEntity::class,
        GamificationEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wellnessDao(): WellnessDao
    abstract fun medicationDao(): MedicationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kodi_wellness_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialSampleData(database.wellnessDao())
                    }
                }
            }

            private suspend fun populateInitialSampleData(dao: WellnessDao) {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // 1. Initial User will be created dynamically when user registers or signs in with Google


                // 2. Health Insurance Information
                dao.insertOrUpdateInsurance(
                    InsuranceInfoEntity(
                        id = 1,
                        provider = "Blue Cross Blue Shield",
                        policyNumber = "XEB-948271049",
                        groupNumber = "GRP-88301",
                        memberId = "MEM-00294184",
                        contactPhone = "+1 (800) 555-0199",
                        planType = "Comprehensive PPO Advantage",
                        notes = "In-network preventive care 100% covered. Copay: $20 Specialist, $10 Generic Rx.",
                        isVerified = true,
                        lastUpdated = System.currentTimeMillis()
                    )
                )

                // 3. Sample Medications
                val med1 = dao.insertMedication(
                    MedicationEntity(
                        name = "Vitamin D3",
                        dosage = "1000 IU",
                        form = "Tablet",
                        frequency = "Once daily",
                        scheduledTimes = "08:00 AM",
                        startDate = "2026-09-01",
                        instructions = "Take in the morning with food",
                        doctorName = "Dr. Robert Vance",
                        pharmacy = "Walgreens Pharmacy",
                        notes = "Essential for immune & bone health",
                        isActive = true
                    )
                )

                val med2 = dao.insertMedication(
                    MedicationEntity(
                        name = "Paracetamol",
                        dosage = "500mg",
                        form = "Tablet",
                        frequency = "As needed",
                        scheduledTimes = "02:00 PM",
                        startDate = "2026-10-01",
                        instructions = "Take with water if headache recurs. Max 3g daily.",
                        doctorName = "Dr. Robert Vance",
                        pharmacy = "CVS Caremark",
                        notes = "Mild analgesic",
                        isActive = true
                    )
                )

                val med3 = dao.insertMedication(
                    MedicationEntity(
                        name = "Omega-3 Fish Oil",
                        dosage = "1200mg",
                        form = "Capsule",
                        frequency = "Once daily",
                        scheduledTimes = "08:00 PM",
                        startDate = "2026-09-01",
                        instructions = "Take after evening dinner",
                        doctorName = "Dr. Robert Vance",
                        pharmacy = "HealthMart",
                        notes = "Cardiovascular wellness",
                        isActive = true
                    )
                )

                // Log that Vitamin D3 was taken today
                dao.insertMedicationLog(
                    MedicationLogEntity(
                        medicationId = med1,
                        medicationName = "Vitamin D3",
                        scheduledTime = "08:00 AM",
                        dateString = todayStr,
                        status = "TAKEN",
                        loggedAt = System.currentTimeMillis() - 3600000 * 3
                    )
                )

                // 4. Sample Medical Appointments
                dao.insertAppointment(
                    AppointmentEntity(
                        doctorName = "Dr. Robert Vance, MD",
                        hospitalClinic = "Metro Health Medical Center",
                        department = "General Medicine",
                        appointmentDate = "2026-10-15",
                        appointmentTime = "10:00 AM",
                        reason = "Annual General Health Check & Blood Pressure Review",
                        notes = "Fast for 8 hours prior to lipid panel blood test",
                        location = "Suite 400, 1200 Health Blvd",
                        contactNumber = "+1 (555) 345-6789",
                        reminderOffset = "1 day before",
                        status = "UPCOMING"
                    )
                )

                dao.insertAppointment(
                    AppointmentEntity(
                        doctorName = "Dr. Elena Rostova",
                        hospitalClinic = "City Dental Care",
                        department = "Dentistry",
                        appointmentDate = "2026-11-04",
                        appointmentTime = "02:30 PM",
                        reason = "Routine 6-Month Dental Cleaning & Exam",
                        notes = "Regular checkup",
                        location = "25 Dental Plaza, Floor 2",
                        contactNumber = "+1 (555) 789-0123",
                        reminderOffset = "2 hours before",
                        status = "UPCOMING"
                    )
                )

                // 5. Today's Wellness Tasks
                dao.insertWellnessTasks(
                    listOf(
                        WellnessTaskEntity(
                            title = "Morning Hydration (500ml water)",
                            category = "Hydration",
                            timeOfDay = "07:00 AM",
                            dateString = todayStr,
                            durationMinutes = 5,
                            points = 10,
                            status = "COMPLETED",
                            isAiGenerated = false
                        ),
                        WellnessTaskEntity(
                            title = "30-Minute Outdoor Walk",
                            category = "Fitness",
                            timeOfDay = "07:30 AM",
                            dateString = todayStr,
                            durationMinutes = 30,
                            points = 20,
                            status = "COMPLETED",
                            isAiGenerated = false
                        ),
                        WellnessTaskEntity(
                            title = "Nutritious Protein Breakfast",
                            category = "Nutrition",
                            timeOfDay = "08:30 AM",
                            dateString = todayStr,
                            durationMinutes = 20,
                            points = 15,
                            status = "COMPLETED",
                            isAiGenerated = false
                        ),
                        WellnessTaskEntity(
                            title = "5-Minute Midday Deep Breathing",
                            category = "Mental Wellness",
                            timeOfDay = "01:00 PM",
                            dateString = todayStr,
                            durationMinutes = 5,
                            points = 10,
                            status = "PENDING",
                            isAiGenerated = false
                        ),
                        WellnessTaskEntity(
                            title = "Evening Stretch & Unwind",
                            category = "Fitness",
                            timeOfDay = "08:30 PM",
                            dateString = todayStr,
                            durationMinutes = 15,
                            points = 15,
                            status = "PENDING",
                            isAiGenerated = true
                        ),
                        WellnessTaskEntity(
                            title = "Digital Sunset 45m Before Sleep",
                            category = "Sleep",
                            timeOfDay = "10:15 PM",
                            dateString = todayStr,
                            durationMinutes = 45,
                            points = 10,
                            status = "PENDING",
                            isAiGenerated = true
                        )
                    )
                )

                // 6. Water Logs (1,500 / 2,000 ml)
                dao.insertWaterLog(
                    WaterLogEntity(amountMl = 500, dateString = todayStr, timeString = "07:15 AM", timestamp = System.currentTimeMillis() - 14000000)
                )
                dao.insertWaterLog(
                    WaterLogEntity(amountMl = 500, dateString = todayStr, timeString = "10:30 AM", timestamp = System.currentTimeMillis() - 7200000)
                )
                dao.insertWaterLog(
                    WaterLogEntity(amountMl = 500, dateString = todayStr, timeString = "01:15 PM", timestamp = System.currentTimeMillis() - 3600000)
                )

                // 7. Sleep Log (7h 30m)
                dao.insertSleepLog(
                    SleepLogEntity(
                        dateString = todayStr,
                        bedtime = "11:00 PM",
                        wakeTime = "06:30 AM",
                        durationHours = 7.5f,
                        qualityRating = 4,
                        notes = "Woke up feeling refreshed, consistent sleep environment."
                    )
                )

                // 8. Exercise Log
                dao.insertExerciseLog(
                    ExerciseLogEntity(
                        activityType = "Walking",
                        durationMinutes = 30,
                        distanceKm = 2.4f,
                        caloriesBurned = 145,
                        dateString = todayStr,
                        notes = "Brisk morning walk in the neighborhood"
                    )
                )

                // 9. Weight Log
                dao.insertWeightLog(
                    WeightLogEntity(
                        weightKg = 74.5f,
                        dateString = todayStr,
                        notes = "Morning weighed before breakfast"
                    )
                )

                // 10. Sample Health Records
                dao.insertHealthRecord(
                    HealthRecordEntity(
                        documentName = "Comprehensive Metabolic Panel (CMP)",
                        category = "Laboratory Result",
                        dateString = "2026-09-12",
                        doctorOrHospital = "Quest Diagnostics",
                        notes = "All liver & kidney markers within optimal reference ranges.",
                        fileType = "PDF",
                        isSecure = true
                    )
                )
                dao.insertHealthRecord(
                    HealthRecordEntity(
                        documentName = "Annual Flu Vaccination Certificate",
                        category = "Vaccination",
                        dateString = "2026-09-28",
                        doctorOrHospital = "Metro Health Pharmacy",
                        notes = "Standard quadrivalent influenza immunization administered.",
                        fileType = "PDF",
                        isSecure = true
                    )
                )
                dao.insertHealthRecord(
                    HealthRecordEntity(
                        documentName = "Prescription - Vitamin D3 & Inhaler Refill",
                        category = "Prescription",
                        dateString = "2026-09-01",
                        doctorOrHospital = "Dr. Robert Vance, MD",
                        notes = "Active prescription valid for 12 months with 3 refills remaining.",
                        fileType = "PDF",
                        isSecure = true
                    )
                )

                // 11. Initial Reminders
                dao.insertReminder(
                    ReminderEntity(
                        title = "Take Vitamin D3",
                        category = "Medication",
                        timeString = "08:00 AM",
                        frequency = "Daily",
                        isEnabled = true
                    )
                )
                dao.insertReminder(
                    ReminderEntity(
                        title = "Drink Water (Midday Refresh)",
                        category = "Water",
                        timeString = "02:00 PM",
                        frequency = "Daily",
                        isEnabled = true
                    )
                )
                dao.insertReminder(
                    ReminderEntity(
                        title = "Take Evening Omega-3",
                        category = "Medication",
                        timeString = "08:00 PM",
                        frequency = "Daily",
                        isEnabled = true
                    )
                )

                // 12. Gamification
                dao.insertOrUpdateGamification(
                    GamificationEntity(
                        id = 1,
                        totalPoints = 380,
                        currentLevel = 4,
                        hydrationStreak = 5,
                        medicationStreak = 12,
                        overallDailyStreak = 6,
                        unlockedBadges = "first_step,hydration_hero,med_streak,wellness_explorer",
                        lastActiveDate = todayStr
                    )
                )

                // 13. Symptom Log with Correlation Sample
                dao.insertSymptom(
                    SymptomLogEntity(
                        symptomName = "Headache",
                        severity = 4,
                        severityLabel = "Moderate",
                        dateString = "2026-10-06",
                        timeString = "04:30 PM",
                        duration = "1 hour",
                        notes = "Mild frontal throbbing after working at computer.",
                        potentialCorrelations = "Potential Correlation: Logged after low water intake (<1200ml) on Oct 6. Increasing hydration may help ease tension."
                    )
                )

                // 14. Initial Welcome Chat Message from Wellness AI
                dao.insertChatMessage(
                    ChatMessageEntity(
                        role = "model",
                        content = "Hello! I'm **Wellness AI**, your personal medical assistance and wellness planning companion.\n\n" +
                                "I can help you build daily healthy routines, understand wellness habits, review symptom correlations, and suggest personalized wellness plans.\n\n" +
                                "*Disclaimer: Wellness AI provides general health and wellness information and is not a substitute for professional medical advice, diagnosis, or treatment. Always consult a qualified physician for medical concerns.*",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
