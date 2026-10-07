package com.example

import com.example.kodiwellness.data.remote.GeminiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class WellnessLogicTest {

    private val geminiService = GeminiService()

    @Test
    fun testEmergencyKeywordDetection() {
        assertTrue(geminiService.isEmergency("Patient reports acute chest pain and numbness"))
        assertTrue(geminiService.isEmergency("I have shortness of breath and cannot breathe"))
        assertFalse(geminiService.isEmergency("Healthy breakfast tips with oats"))
    }

    @Test
    fun testOfflineSymptomCorrelationAnalysis() = runBlocking {
        val correlation = geminiService.analyzeSymptomCorrelations(
            symptom = "Headache",
            severity = 5,
            recentWaterMl = 800,
            recentSleepHours = 5.5f,
            recentExerciseMinutes = 0,
            recentMedications = listOf("Vitamin D3")
        )

        assertNotNull(correlation)
        assertTrue(correlation.contains("dehydration", ignoreCase = true) || correlation.contains("water", ignoreCase = true))
        assertTrue(correlation.contains("medical diagnosis", ignoreCase = true) || correlation.contains("physician", ignoreCase = true))
    }

    @Test
    fun testDailyPlanGeneration() = runBlocking {
        val plan = geminiService.generateDailyWellnessPlan("Hydration, Sleep, Fitness")
        assertTrue(plan.isNotEmpty())
        assertEquals("07:00 AM", plan.first().timeOfDay)
        assertEquals("Hydration", plan.first().category)
    }

    @Test
    fun testSymptomSeverityClassification() {
        fun classifySeverity(sev: Int): String = when {
            sev <= 3 -> "Mild"
            sev <= 6 -> "Moderate"
            else -> "Severe"
        }

        assertEquals("Mild", classifySeverity(2))
        assertEquals("Moderate", classifySeverity(5))
        assertEquals("Severe", classifySeverity(8))
        assertEquals("Severe", classifySeverity(10))
    }
}
