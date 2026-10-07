# Kodi Wellness (WellnessCare)
*Your Health. Your Plan. Your Wellness.*

Kodi Wellness is a modern, professional, native Android mobile application designed to empower individuals with comprehensive personal medical assistance, medication tracking, appointments, lifestyle vital tracking, symptom correlation insights, and AI-driven daily wellness planning.

---

## 🌟 Key Features

### 1. Main Dashboard
- Dynamic greeting and daily wellness score (calculated based on task completion, hydration, sleep, and medication adherence).
- High-visibility SOS Emergency action button accessible immediately from the top bar.
- Next dose medication status card with quick **Take Dose**, **Snooze**, and **Skip** actions.
- Upcoming medical appointment reminders and details.
- Hydration progress with one-tap quick-add buttons (+250ml, +500ml, +750ml).
- Sleep and exercise progress cards.
- Today's interactive wellness task list.

### 2. Medication Management
- Prescriptions and supplements tracking: Name, dosage, form (Tablet, Capsule, Syrup, Injection, Inhaler, Drops), frequency, scheduled times, instructions, doctor, and pharmacy.
- Daily medication logs: Taken, Skipped, Snoozed with adherence percentage metrics.
- High-priority Android notification channel (`kodi_medications`).

### 3. Medical Appointments Planner
- Track upcoming and past doctor visits, clinic location, department, appointment date/time, and notes.
- Configurable reminder offsets (1 day before, 2 hours before, 30 minutes before).
- Status tracking: Upcoming, Completed, Cancelled.

### 4. Wellness Planner & Daily Plan
- Categorized wellness goals: **Fitness**, **Nutrition**, **Hydration**, **Sleep**, **Mental Wellness**, and **Medical**.
- **AI Daily Plan Generator**: Generates personalized timed daily wellness activities (e.g. morning lemon water, 20m walk, protein breakfast, posture reset, digital sunset) directly applicable to the daily plan.
- Real-time completion percentage tracking.

### 5. Daily Symptom Tracker & Form (Local Room Database)
- **Simple Daily Logging Form**: Select from common symptoms (*Headache, Fatigue, Back Pain, Fever, Cough, Nausea, Dizziness, Joint Pain, Other*), set live **Date Stamp** (`YYYY-MM-DD`) and **Time Stamp**, adjust the **1–10 Severity Scale** slider with dynamic Mild/Moderate/Severe color bubbles, set duration, and write notes.
- Saves directly to the local Room database (`symptoms` table) with immediate confirmation and history timeline.
- **Post-Log AI Correlation Prompt**: Evaluates recent health logs (water intake, sleep, activity, medications) to suggest lifestyle correlations (e.g. *"You've logged headaches after periods of low water intake. Consider increasing your hydration."*).
- **Medical Safety Guardrails**: High severity (>7) alerts, emergency disclaimers, and clear notifications that observations are not diagnostic replacements for physician care.

### 6. App Brand Logo & Splash Screens for App Understanding
- **Kodi Wellness Logo**: Features the human figure in sky blue, lush green leaves, and stethoscope symbol with the official slogan: *"Health Today. A Brighter Tomorrow."* Replaces launcher icon and featured in headers and splash.
- **Interactive Splash & Tour**: Showcases 5 key pillars (*Daily Symptom Journal, Medication Management, Wellness Planner & AI, Appointments & SOS, Private Local Storage*) with feature indicators and seamless entry into the Dashboard. Accessible anytime from Profile > App Overview.

### 6. Health & Vital Metrics Hub
- **Hydration Tracker**: Target 2,000 ml with quick increments and daily completion history.
- **Sleep Tracker**: Bedtime, wake-up time, duration, and 1–5 star quality rating.
- **Exercise Tracker**: Movement duration (mins), distance (km), estimated calories burned (kcal), and activity type.
- **Weight Tracker**: Weight log history, target weight tracker, and observational BMI calculation.
- **Health Records**: Encrypted storage for Lab Results, Prescriptions, Vaccination certificates, Imaging, and Doctor Notes.

### 7. Wellness AI Assistant (Gemini Chatbot)
- Conversational wellness companion powered by Google Gemini (with an intelligent offline healthcare knowledge engine fallback).
- Quick suggestion chips for sleep hygiene, balanced nutrition, hydration pacing, and tension relief.
- Urgent medical emergency detection (flags emergencies like chest pain, difficulty breathing, or stroke symptoms with immediate SOS directions).

### 8. Health Insurance Information in Profile (Enhanced)
- Secure profile section for insurance provider, policy number, group number, member/subscriber ID, plan type, and coverage notes.
- Direct-dial support for member services.
- Protected local storage.

### 9. Gamification & Streaks System (Enhanced)
- Health points (+25 for meds, +10 for hydration, +25 for exercise, +15 for sleep).
- Level progression and rank tracking.
- Badges: *First Step*, *7-Day Hydration Streak*, *Medication Champion*, *First Month of Adherence*, *Wellness Pioneer*, *Century Master*.
- Optional social leaderboard for friendly habit motivation.

### 10. Emergency Health Profile & Safety
- Instant access to Blood Group, Allergies, Medical Conditions, Primary Physician, Active Medications, and Emergency Contact with one-tap dialing.
- Emergency dispatcher (911) dialer with confirmation dialog to avoid accidental calls.

---

## 🛠️ Architecture & Tech Stack

- **Platform**: Native Android (API 24+)
- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Local Persistence**: Room Database (`AppDatabase`, `WellnessDao`) with reactive `Flow` queries and automated initial sample data pre-population.
- **AI Integration**: Gemini 3.5 Flash REST API via OkHttp / Kotlin Coroutines with emergency guardrails and offline fallback.
- **Notifications**: Android `NotificationChannel` with high-importance channels for medications, doctor appointments, and hydration reminders.
- **Theming**: Custom healthcare palette (Teal `#0D9488`, Cyan `#0284C7`, Emerald `#10B981`) with full Light/Dark mode support.

---

## ⚙️ Configuration & API Setup

### Gemini API Key
To connect the live Gemini 3.5 Flash model:
1. Open the AI Studio Secrets panel.
2. Ensure `GEMINI_API_KEY` is provided.
3. The app automatically reads `BuildConfig.GEMINI_API_KEY` via the Secrets Gradle Plugin.
4. When offline or without an API key, the app seamlessly uses its built-in medical wellness knowledge engine.

### Firebase Setup (Optional / Ready)
The app is built with an abstracted repository layer. To connect Firebase Firestore or Firebase Auth in the future, add your `google-services.json` to the `/app/` directory and uncomment the Firestore and Auth dependencies in `/app/build.gradle.kts`.

---

## 📜 Medical Safety Notice
*Kodi Wellness is a wellness and health management tool, NOT a medical diagnosis system. Wellness AI provides general health information and is not a substitute for professional medical advice, diagnosis, or treatment. Users should always consult a qualified healthcare professional for medical concerns.*
