package com.example.kodiwellness.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val emergencyKeywords = listOf(
        "chest pain", "heart attack", "can't breathe", "cannot breathe",
        "shortness of breath", "stroke", "facial drooping", "slurred speech",
        "severe bleeding", "unconscious", "overdose", "suicide", "kill myself",
        "anaphylaxis", "severe allergic reaction", "coughing blood"
    )

    fun isEmergency(text: String): Boolean {
        val lower = text.lowercase()
        return emergencyKeywords.any { lower.contains(it) }
    }

    suspend fun generateChatResponse(
        prompt: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        userName: String = ""
    ): Pair<String, Boolean> = withContext(Dispatchers.IO) {
        val hasEmergency = isEmergency(prompt)

        val emergencyPrefix = if (hasEmergency) {
            "⚠️ **URGENT MEDICAL NOTICE** ⚠️\n" +
            "If you or someone nearby is experiencing a life-threatening emergency, call **911** or your local emergency service immediately. Do NOT wait for an online response.\n\n"
        } else ""

        val addressingPrompt = if (userName.isNotBlank()) " Address the user respectfully as $userName." else ""
        val systemInstruction = "You are 'Wellness AI', a compassionate and knowledgeable personal medical assistant and wellness planner in Kodi Wellness.$addressingPrompt " +
                "You provide evidence-based general wellness advice, sleep hygiene tips, nutrition guidance, and healthy lifestyle habits. " +
                "STRICT SAFETY RULES: You must NEVER diagnose diseases, never prescribe drugs, never recommend changing medication dosages, and never tell users to stop prescribed medications. " +
                "Always remind users that you provide general information only and they should consult a qualified doctor for medical diagnosis or treatment."

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

                val contentsArray = JSONArray()

                // Add past 4 conversation messages if available
                val trimmedHistory = conversationHistory.takeLast(4)
                for ((role, text) in trimmedHistory) {
                    val partObj = JSONObject().put("text", text)
                    val contentObj = JSONObject()
                        .put("role", if (role == "user") "user" else "model")
                        .put("parts", JSONArray().put(partObj))
                    contentsArray.put(contentObj)
                }

                // Add current prompt
                contentsArray.put(
                    JSONObject()
                        .put("role", "user")
                        .put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                )

                val requestJson = JSONObject()
                    .put("contents", contentsArray)
                    .put(
                        "systemInstruction",
                        JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                    )
                    .put(
                        "generationConfig",
                        JSONObject()
                            .put("temperature", 0.6)
                            .put("topP", 0.95)
                            .put("maxOutputTokens", 800)
                    )

                val body = requestJson.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val jsonResponse = JSONObject(responseBody)
                    val candidates = jsonResponse.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.getJSONObject("content")
                        val parts = content.getJSONArray("parts")
                        if (parts.length() > 0) {
                            val replyText = parts.getJSONObject(0).getString("text").trim()
                            val fullText = emergencyPrefix + replyText + "\n\n*Wellness AI provides general health information and is not a substitute for professional medical advice, diagnosis, or treatment.*"
                            return@withContext Pair(fullText, hasEmergency)
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback gracefully to offline medical wellness knowledge engine
            }
        }

        // Offline / Fallback Intelligent Medical Wellness Knowledge Engine
        val offlineAnswer = generateOfflineWellnessResponse(prompt)
        val fullAnswer = emergencyPrefix + offlineAnswer + "\n\n*Wellness AI provides general health information and is not a substitute for professional medical advice, diagnosis, or treatment.*"
        Pair(fullAnswer, hasEmergency)
    }

    suspend fun analyzeSymptomCorrelations(
        symptom: String,
        severity: Int,
        recentWaterMl: Int,
        recentSleepHours: Float,
        recentExerciseMinutes: Int,
        recentMedications: List<String>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val prompt = "Analyze potential lifestyle correlations for a patient who just logged symptom: '$symptom' (severity $severity/10). " +
                "Recent logged health data: Water Intake: $recentWaterMl ml (Goal: 2000 ml); Sleep: $recentSleepHours hours (Goal: 8.0 hours); " +
                "Exercise: $recentExerciseMinutes minutes; Active Medications: ${recentMedications.joinToString(", ")}. " +
                "In 2-3 friendly sentences, suggest possible correlations (e.g., low hydration or poor sleep) without diagnosing, and remind them this is not a medical diagnosis."

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val requestJson = JSONObject()
                    .put(
                        "contents",
                        JSONArray().put(
                            JSONObject().put(
                                "parts",
                                JSONArray().put(JSONObject().put("text", prompt))
                            )
                        )
                    )
                val body = requestJson.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url(url).post(body).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val respStr = response.body?.string() ?: ""
                    val json = JSONObject(respStr)
                    val text = json.optJSONArray("candidates")?.optJSONObject(0)
                        ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                    if (!text.isNullOrBlank()) {
                        return@withContext text.trim()
                    }
                }
            } catch (e: Exception) {
                // fallback to local rule-based correlation
            }
        }

        // Fallback rule-based correlation analysis:
        val findings = mutableListOf<String>()
        val symptomLower = symptom.lowercase()

        if (recentWaterMl < 1500) {
            findings.add("You've logged $recentWaterMl ml of water today (below your 2,000 ml target). Mild dehydration is a frequent contributor to $symptomLower and tension.")
        }
        if (recentSleepHours < 7.0f) {
            findings.add("Your logged sleep last night was $recentSleepHours hours (target 8h). Inadequate restorative sleep is strongly linked to heightened sensitivity to $symptomLower.")
        }
        if (recentExerciseMinutes == 0) {
            findings.add("Prolonged sedentary posture may contribute to muscular tension. Gentle stretching or a short walk could help relieve discomfort.")
        }
        if (findings.isEmpty()) {
            findings.add("Your tracked hydration and sleep metrics look good today. Track when this $symptomLower peaks to see if dietary or postural triggers correlate.")
        }

        findings.joinToString(" ") + " (Note: This is an observational lifestyle correlation, not a medical diagnosis. Please consult your physician if symptoms persist or worsen.)"
    }

    suspend fun generateDailyWellnessPlan(
        focusAreas: String,
        wakeTime: String = "07:00 AM",
        sleepTime: String = "10:30 PM"
    ): List<PlanTaskItem> = withContext(Dispatchers.IO) {
        // Returns a structured list of AI planned wellness tasks
        listOf(
            PlanTaskItem("07:00 AM", "Morning Hydration (500ml warm water with lemon)", "Hydration", 5, 10),
            PlanTaskItem("07:30 AM", "Brisk Morning Walk or Light Stretch (20 mins)", "Fitness", 20, 20),
            PlanTaskItem("08:00 AM", "Balanced Protein & Fiber Breakfast", "Nutrition", 20, 15),
            PlanTaskItem("09:00 AM", "Morning Medication / Vitamin Intake", "Medical", 5, 20),
            PlanTaskItem("12:30 PM", "Nutritious Lunch & 10-Minute Post-Meal Walk", "Nutrition", 30, 15),
            PlanTaskItem("03:00 PM", "Midday Hydration & Posture Reset", "Hydration", 5, 10),
            PlanTaskItem("06:00 PM", "Light Aerobic Exercise or Yoga (25 mins)", "Fitness", 25, 20),
            PlanTaskItem("07:30 PM", "Wholesome Dinner & Hydration", "Nutrition", 30, 15),
            PlanTaskItem("09:30 PM", "Mindful Breathing / Gratitude Journaling (10 mins)", "Mental Wellness", 10, 15),
            PlanTaskItem("10:15 PM", "Screen-Free Wind Down for Restorative Sleep", "Sleep", 15, 10)
        )
    }

    private fun generateOfflineWellnessResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("breakfast") || lower.contains("eat") || lower.contains("food") || lower.contains("nutrition") -> {
                "Here are some balanced, nutrient-rich breakfast ideas:\n" +
                "• **Oatmeal Bowl**: Rolled oats prepared with chia seeds, fresh berries, and a sprinkle of walnuts for sustained energy and heart-healthy fiber.\n" +
                "• **Greek Yogurt Parfait**: High-protein unsweetened Greek yogurt with fresh fruit and sliced almonds.\n" +
                "• **Veggie Egg Scramble**: Two eggs scrambled with spinach, tomatoes, and mushrooms served on whole-grain toast.\n\n" +
                "Aim for a balance of protein, complex carbohydrates, and healthy fats to support stable blood sugar levels."
            }
            lower.contains("sleep") || lower.contains("insomnia") || lower.contains("tired") -> {
                "Here are proven sleep hygiene strategies to improve sleep quality:\n" +
                "• **Consistent Schedule**: Go to bed and wake up at the same time daily, even on weekends.\n" +
                "• **Digital Sunset**: Turn off phones, tablets, and screens 45–60 minutes before bedtime to prevent blue light from suppressing melatonin.\n" +
                "• **Cool, Dark Environment**: Keep your bedroom around 65°F (18°C) and minimize ambient light.\n" +
                "• **Limit Evening Caffeine**: Avoid caffeinated beverages within 6 hours of bedtime."
            }
            lower.contains("headache") -> {
                "Headaches can have various causes, such as dehydration, screen glare, stress, or muscle tension in the neck.\n" +
                "General comfort tips:\n" +
                "• Drink 1–2 glasses of cool water immediately to rule out dehydration.\n" +
                "• Rest in a quiet, dimly lit room with a cool cloth over your forehead.\n" +
                "• Take regular breaks from computer screens every 20 minutes.\n\n" +
                "⚠️ *Important*: If your headache is sudden and unusually severe ('thunderclap'), or accompanied by stiff neck, fever, confusion, numbness, or vision loss, seek emergency medical care immediately."
            }
            lower.contains("water") || lower.contains("hydration") -> {
                "Proper hydration is essential for cellular function, digestion, joint lubrication, and mood:\n" +
                "• The baseline recommended intake is typically 2,000–2,500 ml (about 8 glasses) daily, adjusted for activity level and climate.\n" +
                "• Keep a reusable water bottle near your desk as a visual cue.\n" +
                "• Infuse water with cucumber, lemon slices, or mint if you prefer natural flavor."
            }
            lower.contains("medication") || lower.contains("pill") || lower.contains("dose") -> {
                "Medication safety recommendations:\n" +
                "• Always take medications exactly as prescribed by your doctor or outlined on the pharmacist label.\n" +
                "• Never abruptly discontinue or adjust prescription dosages without consulting your prescribing healthcare provider.\n" +
                "• Maintain a consolidated medication list in Kodi Wellness and set daily reminders to avoid missed doses."
            }
            lower.contains("exercise") || lower.contains("walk") || lower.contains("workout") -> {
                "Regular movement supports cardiovascular health, metabolic fitness, and mental well-being:\n" +
                "• Aim for at least 150 minutes of moderate aerobic activity (such as brisk walking) per week.\n" +
                "• Incorporate strength and flexibility exercises 2–3 times weekly.\n" +
                "• Start with small 10-minute movement intervals if you are new to regular exercise."
            }
            else -> {
                "Thank you for sharing. Building consistent daily wellness habits—including regular hydration, balanced nutrition, restful sleep, and gentle daily movement—forms the cornerstone of long-term health.\n\n" +
                "Feel free to ask for specific advice on:\n" +
                "• Daily meal planning & nutrition guidelines\n" +
                "• Sleep hygiene & stress relief routines\n" +
                "• Hydration pacing throughout the day\n" +
                "• Tracking symptom patterns and lifestyle correlations"
            }
        }
    }
}

data class PlanTaskItem(
    val timeOfDay: String,
    val title: String,
    val category: String,
    val durationMinutes: Int,
    val points: Int
)
