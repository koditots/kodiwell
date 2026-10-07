package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kodiwellness.ui.WellnessViewModel
import com.example.kodiwellness.ui.navigation.Screen
import com.example.kodiwellness.ui.screens.*
import com.example.ui.theme.KodiWellnessTheme

class MainActivity : ComponentActivity() {

    private val viewModel: WellnessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KodiWellnessTheme {
                MainAppContent(viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: WellnessViewModel) {
    val user by viewModel.user.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()

    var currentScreen by remember { mutableStateOf<String>(Screen.Splash.route) }

    // Check onboarding status
    LaunchedEffect(user) {
        if (user != null && !user!!.isOnboarded && currentScreen != Screen.Splash.route) {
            currentScreen = Screen.Onboarding.route
        }
    }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Handle back button when in a subscreen
    if (currentScreen != Screen.Home.route && currentScreen != Screen.Onboarding.route && currentScreen != Screen.Splash.route) {
        BackHandler {
            currentScreen = Screen.Home.route
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(Screen.Planner.route, "Planner", Icons.Filled.EventNote, Icons.Outlined.EventNote),
        BottomNavItem(Screen.Health.route, "Health", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
        BottomNavItem(Screen.AiAssistant.route, "Wellness AI", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
        BottomNavItem(Screen.Profile.route, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    val showBottomBar = currentScreen in listOf(
        Screen.Home.route,
        Screen.Planner.route,
        Screen.Health.route,
        Screen.AiAssistant.route,
        Screen.Profile.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    tonalElevation = 6.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentScreen == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = item.route },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("nav_item_${item.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    Screen.Splash.route -> {
                        SplashScreen(
                            onContinue = {
                                currentScreen = if (user != null && !user!!.isOnboarded) {
                                    Screen.Onboarding.route
                                } else {
                                    Screen.Home.route
                                }
                            }
                        )
                    }
                    Screen.Onboarding.route -> {
                        OnboardingScreen(
                            viewModel = viewModel,
                            onComplete = { currentScreen = Screen.Home.route }
                        )
                    }
                    Screen.Home.route -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToMedications = { currentScreen = "medications" },
                            onNavigateToAppointments = { currentScreen = Screen.Appointments.route },
                            onNavigateToSymptoms = { currentScreen = "symptoms" },
                            onNavigateToPlanner = { currentScreen = Screen.Planner.route },
                            onNavigateToEmergency = { currentScreen = Screen.Emergency.route },
                            onNavigateToGamification = { currentScreen = Screen.Gamification.route },
                            onNavigateToCalendar = { currentScreen = Screen.Calendar.route },
                            onNavigateToHealthHub = { currentScreen = Screen.Health.route }
                        )
                    }
                    Screen.Planner.route -> {
                        PlannerScreen(viewModel = viewModel)
                    }
                    Screen.Health.route -> {
                        HealthHubScreen(
                            viewModel = viewModel,
                            onNavigateToMedications = { currentScreen = "medications" },
                            onNavigateToSymptoms = { currentScreen = "symptoms" }
                        )
                    }
                    Screen.AiAssistant.route -> {
                        AiAssistantScreen(
                            viewModel = viewModel,
                            onNavigateToEmergency = { currentScreen = Screen.Emergency.route }
                        )
                    }
                    Screen.Profile.route -> {
                        ProfileScreen(
                            viewModel = viewModel,
                            onNavigateToEducation = { currentScreen = Screen.Education.route },
                            onNavigateToGamification = { currentScreen = Screen.Gamification.route },
                            onNavigateToEmergency = { currentScreen = Screen.Emergency.route },
                            onNavigateToSplash = { currentScreen = Screen.Splash.route }
                        )
                    }
                    "medications" -> {
                        MedicationsScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.Home.route }
                        )
                    }
                    Screen.Appointments.route -> {
                        AppointmentsScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.Home.route }
                        )
                    }
                    "symptoms" -> {
                        SymptomTrackerScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.Home.route }
                        )
                    }
                    Screen.Calendar.route -> {
                        CalendarScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.Home.route }
                        )
                    }
                    Screen.Gamification.route -> {
                        GamificationScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.Home.route }
                        )
                    }
                    Screen.Emergency.route -> {
                        EmergencyProfileScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.Home.route }
                        )
                    }
                    Screen.Education.route -> {
                        HealthEducationScreen(
                            onBack = { currentScreen = Screen.Profile.route }
                        )
                    }
                    else -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToMedications = { currentScreen = "medications" },
                            onNavigateToAppointments = { currentScreen = Screen.Appointments.route },
                            onNavigateToSymptoms = { currentScreen = "symptoms" },
                            onNavigateToPlanner = { currentScreen = Screen.Planner.route },
                            onNavigateToEmergency = { currentScreen = Screen.Emergency.route },
                            onNavigateToGamification = { currentScreen = Screen.Gamification.route },
                            onNavigateToCalendar = { currentScreen = Screen.Calendar.route },
                            onNavigateToHealthHub = { currentScreen = Screen.Health.route }
                        )
                    }
                }
            }
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
