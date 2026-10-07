package com.example.kodiwellness.data.local

import androidx.room.*
import com.example.kodiwellness.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WellnessDao {
    // User & Profile
    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    // Health Insurance
    @Query("SELECT * FROM insurance_info WHERE id = 1 LIMIT 1")
    fun getInsuranceInfo(): Flow<InsuranceInfoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateInsurance(info: InsuranceInfoEntity)

    // Medications
    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY id DESC")
    fun getActiveMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications ORDER BY id DESC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(med: MedicationEntity): Long

    @Update
    suspend fun updateMedication(med: MedicationEntity)

    @Delete
    suspend fun deleteMedication(med: MedicationEntity)

    // Medication Logs
    @Query("SELECT * FROM medication_logs WHERE dateString = :date ORDER BY loggedAt DESC")
    fun getMedicationLogsForDate(date: String): Flow<List<MedicationLogEntity>>

    @Query("SELECT * FROM medication_logs ORDER BY loggedAt DESC LIMIT 100")
    fun getRecentMedicationLogs(): Flow<List<MedicationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationLog(log: MedicationLogEntity): Long

    // Appointments
    @Query("SELECT * FROM appointments ORDER BY appointmentDate ASC, appointmentTime ASC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE status = 'UPCOMING' ORDER BY appointmentDate ASC, appointmentTime ASC")
    fun getUpcomingAppointments(): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appt: AppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appt: AppointmentEntity)

    @Delete
    suspend fun deleteAppointment(appt: AppointmentEntity)

    // Wellness Tasks
    @Query("SELECT * FROM wellness_tasks WHERE dateString = :date ORDER BY id ASC")
    fun getWellnessTasksForDate(date: String): Flow<List<WellnessTaskEntity>>

    @Query("SELECT * FROM wellness_tasks ORDER BY dateString DESC, id ASC")
    fun getAllWellnessTasks(): Flow<List<WellnessTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWellnessTask(task: WellnessTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWellnessTasks(tasks: List<WellnessTaskEntity>)

    @Update
    suspend fun updateWellnessTask(task: WellnessTaskEntity)

    @Delete
    suspend fun deleteWellnessTask(task: WellnessTaskEntity)

    // Symptoms
    @Query("SELECT * FROM symptoms ORDER BY timestamp DESC")
    fun getAllSymptoms(): Flow<List<SymptomLogEntity>>

    @Query("SELECT * FROM symptoms WHERE dateString = :date ORDER BY timestamp DESC")
    fun getSymptomsForDate(date: String): Flow<List<SymptomLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: SymptomLogEntity): Long

    @Update
    suspend fun updateSymptom(symptom: SymptomLogEntity)

    @Delete
    suspend fun deleteSymptom(symptom: SymptomLogEntity)

    // Health Records
    @Query("SELECT * FROM health_records ORDER BY dateString DESC, id DESC")
    fun getAllHealthRecords(): Flow<List<HealthRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecord(record: HealthRecordEntity): Long

    @Delete
    suspend fun deleteHealthRecord(record: HealthRecordEntity)

    // Water Logs
    @Query("SELECT * FROM water_logs WHERE dateString = :date ORDER BY timestamp DESC")
    fun getWaterLogsForDate(date: String): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentWaterLogs(): Flow<List<WaterLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLogEntity): Long

    @Query("DELETE FROM water_logs WHERE dateString = :date")
    suspend fun resetWaterLogsForDate(date: String)

    // Sleep Logs
    @Query("SELECT * FROM sleep_logs ORDER BY timestamp DESC")
    fun getAllSleepLogs(): Flow<List<SleepLogEntity>>

    @Query("SELECT * FROM sleep_logs WHERE dateString = :date LIMIT 1")
    fun getSleepLogForDate(date: String): Flow<SleepLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(log: SleepLogEntity): Long

    // Exercise Logs
    @Query("SELECT * FROM exercise_logs ORDER BY timestamp DESC")
    fun getAllExerciseLogs(): Flow<List<ExerciseLogEntity>>

    @Query("SELECT * FROM exercise_logs WHERE dateString = :date ORDER BY timestamp DESC")
    fun getExerciseLogsForDate(date: String): Flow<List<ExerciseLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseLog(log: ExerciseLogEntity): Long

    // Weight Logs
    @Query("SELECT * FROM weight_logs ORDER BY timestamp DESC")
    fun getAllWeightLogs(): Flow<List<WeightLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(log: WeightLogEntity): Long

    // Reminders
    @Query("SELECT * FROM reminders ORDER BY id DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    // Gamification
    @Query("SELECT * FROM gamification WHERE id = 1 LIMIT 1")
    fun getGamification(): Flow<GamificationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGamification(game: GamificationEntity)

    // Chat
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(msg: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()
}
