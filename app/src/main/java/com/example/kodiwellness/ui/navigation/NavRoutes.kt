package com.example.kodiwellness.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Splash : Screen("splash", "Welcome to Kodi Wellness")
    object Onboarding : Screen("onboarding", "Welcome")
    object Home : Screen("home", "Dashboard")
    object Planner : Screen("planner", "Planner")
    object Health : Screen("health", "Health")
    object AiAssistant : Screen("ai_assistant", "Wellness AI")
    object Profile : Screen("profile", "Profile")
    object Emergency : Screen("emergency", "Emergency Profile")
    object Calendar : Screen("calendar", "Calendar")
    object Gamification : Screen("gamification", "Achievements")
    object Education : Screen("education", "Health Education")
    object Appointments : Screen("appointments", "Appointments")
}
