package com.example.kodiwellness.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val fullName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val phone: String = "",
    val dateOfBirth: String = "",
    val gender: String = "Prefer not to say",
    val bloodGroup: String = "O+",
    val heightCm: Float = 175f,
    val weightKg: Float = 70.0f,
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val emergencyRelationship: String = "",
    val allergies: String = "None known",
    val medicalConditions: String = "None reported",
    val primaryDoctor: String = "",
    val organDonor: Boolean = false,
    val focusAreas: String = "General Wellness, Medication, Hydration, Sleep",
    val isOnboarded: Boolean = false
)

@Entity(tableName = "insurance_info")
data class InsuranceInfoEntity(
    @PrimaryKey val id: Int = 1,
    val provider: String = "Blue Cross Blue Shield",
    val policyNumber: String = "XEB-948271049",
    val groupNumber: String = "GRP-88301",
    val memberId: String = "MEM-00294184",
    val contactPhone: String = "+1 (800) 555-0199",
    val planType: String = "Comprehensive PPO Advantage",
    val notes: String = "In-network preventive care 100% covered. Copay: $20 Specialist, $10 Generic Rx.",
    val isVerified: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String,
    val intakeFrequency: String = "Once daily", // Name, dosage, and intake frequency as requested
    val frequency: String = intakeFrequency,
    val form: String = "Tablet", // Tablet, Capsule, Syrup, Injection, Inhaler, Drops
    val scheduledTimes: String = "08:00 AM", // "08:00 AM" or "08:00 AM, 08:00 PM"
    val startDate: String = "2026-10-07",
    val endDate: String = "",
    val instructions: String = "Take with a glass of water",
    val doctorName: String = "",
    val pharmacy: String = "",
    val notes: String = "",
    val isActive: Boolean = true
)

@Entity(tableName = "medication_logs")
data class MedicationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val medicationName: String,
    val scheduledTime: String,
    val dateString: String,
    val status: String, // TAKEN, SKIPPED, SNOOZED
    val loggedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val doctorName: String,
    val hospitalClinic: String,
    val department: String,
    val appointmentDate: String, // YYYY-MM-DD
    val appointmentTime: String, // e.g. "10:00 AM"
    val reason: String,
    val notes: String = "",
    val location: String = "",
    val contactNumber: String = "",
    val reminderOffset: String = "2 hours before", // 1 day before, 2 hours before, 30 minutes before
    val status: String = "UPCOMING" // UPCOMING, COMPLETED, CANCELLED
)

@Entity(tableName = "wellness_tasks")
data class WellnessTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // Fitness, Nutrition, Hydration, Sleep, Mental Wellness, Medical
    val timeOfDay: String,
    val dateString: String,
    val durationMinutes: Int = 15,
    val points: Int = 15,
    val status: String = "PENDING", // PENDING, COMPLETED, SKIPPED
    val isAiGenerated: Boolean = false
)

@Entity(tableName = "symptoms")
data class SymptomLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symptomName: String,
    val severity: Int, // 1 to 10
    val severityLabel: String, // Mild, Moderate, Severe
    val dateString: String,
    val timeString: String,
    val duration: String,
    val notes: String,
    val potentialCorrelations: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "health_records")
data class HealthRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentName: String,
    val category: String, // Laboratory Result, Medical Report, Prescription, Vaccination, Imaging, Doctor Note
    val dateString: String,
    val doctorOrHospital: String,
    val notes: String = "",
    val fileType: String = "PDF",
    val isSecure: Boolean = true
)

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val dateString: String,
    val timeString: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sleep_logs")
data class SleepLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String,
    val bedtime: String,
    val wakeTime: String,
    val durationHours: Float,
    val qualityRating: Int, // 1 to 5
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "exercise_logs")
data class ExerciseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityType: String, // Walking, Running, Cycling, Gym, Yoga, Swimming, Other
    val durationMinutes: Int,
    val distanceKm: Float = 0f,
    val caloriesBurned: Int = 0,
    val dateString: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "weight_logs")
data class WeightLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weightKg: Float,
    val dateString: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val timeString: String,
    val frequency: String, // Once, Daily, Weekly, Monthly
    val isEnabled: Boolean = true
)

@Entity(tableName = "gamification")
data class GamificationEntity(
    @PrimaryKey val id: Int = 1,
    val totalPoints: Int = 340,
    val currentLevel: Int = 4,
    val hydrationStreak: Int = 5,
    val medicationStreak: Int = 12,
    val overallDailyStreak: Int = 6,
    val unlockedBadges: String = "first_step,hydration_hero,med_streak,wellness_explorer",
    val lastActiveDate: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "user", "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isEmergencyFlagged: Boolean = false
)
