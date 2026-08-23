package com.vjti.campusdisasterresponse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vjti.campusdisasterresponse.data.local.AppDatabase
import com.vjti.campusdisasterresponse.data.queue.EmergencyQueueRepository
import com.vjti.campusdisasterresponse.sos.ui.SosViewModel
import com.vjti.campusdisasterresponse.sos.ui.SosViewModelFactory
import com.vjti.campusdisasterresponse.state.AppStateRepository
import com.vjti.campusdisasterresponse.state.AppViewModel
import com.vjti.campusdisasterresponse.state.AppViewModelFactory
import com.vjti.campusdisasterresponse.ui.admin.SafetyAuditScreen
import com.vjti.campusdisasterresponse.ui.auth.BackendLoginCard
import com.vjti.campusdisasterresponse.ui.education.EducationDashboardScreen
import com.vjti.campusdisasterresponse.ui.map.CampusEvacuationMapScreen
import com.vjti.campusdisasterresponse.ui.response.EmergencyResponseScreen
import com.vjti.campusdisasterresponse.worker.SyncScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainApp()
            }
        }
    }
}

const val CAMPUS_MAP_ROUTE = "campus_map"
const val START_ROUTE = "start"

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Education : Screen("education", "Education", Icons.Default.List)
    object Response : Screen("response", "Response", Icons.Default.Warning)
    object Admin : Screen("admin", "Admin", Icons.Default.Settings)
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val queueRepository = remember(context) {
        EmergencyQueueRepository(
            dao = AppDatabase
                .getDatabase(context)
                .emergencyEventDao(),
            scheduleSync = {
                SyncScheduler.scheduleSync(context)
            }
        )
    }

    val appStateRepository = remember(context) {
        AppStateRepository(
            dao = AppDatabase
                .getDatabase(context)
                .disasterDao(),
            queueRepository = queueRepository
        )
    }

    val appViewModel: AppViewModel = viewModel(
        factory = AppViewModelFactory(appStateRepository)
    )

    val sosViewModel: SosViewModel = viewModel(
        factory = SosViewModelFactory(queueRepository)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showAuthenticatedNavigation = currentRoute != START_ROUTE && currentRoute != CAMPUS_MAP_ROUTE

    Scaffold(
        bottomBar = {
            if (showAuthenticatedNavigation) {
                val items = listOf(
                    Screen.Home,
                    Screen.Education,
                    Screen.Response,
                    Screen.Admin
                )

                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = START_ROUTE,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(START_ROUTE) {
                StartScreen(
                    onSignedIn = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(START_ROUTE) {
                                inclusive = true
                            }
                        }
                    },
                    onEmergencySos = {
                        navController.navigate(Screen.Response.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Home.route) { HomeScreen(appViewModel) }
            composable(Screen.Education.route) { EducationScreen() }
            composable(Screen.Response.route) {
                ResponseScreen(
                    appViewModel = appViewModel,
                    sosViewModel = sosViewModel,
                    onOpenCampusMap = {
                        navController.navigate(CAMPUS_MAP_ROUTE)
                    }
                )
            }
            composable(CAMPUS_MAP_ROUTE) {
                CampusEvacuationMapScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Admin.route) { AdminScreen() }
        }
    }
}

@Composable
fun StartScreen(
    onSignedIn: () -> Unit,
    onEmergencySos: () -> Unit
) {
    BackendLoginCard(
        statusText = "Campus Disaster Response",
        onSignedIn = onSignedIn,
        onEmergencySos = onEmergencySos
    )
}

@Composable
fun HomeScreen(appViewModel: AppViewModel) {
    val appState by appViewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Mode: ${appState.mode.name}\nStatus: ${appState.userStatus.name}",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
fun EducationScreen() {
    EducationDashboardScreen()
}

@Composable
fun ResponseScreen(
    appViewModel: AppViewModel,
    sosViewModel: SosViewModel,
    onOpenCampusMap: () -> Unit
) {
    EmergencyResponseScreen(
        appViewModel = appViewModel,
        sosViewModel = sosViewModel,
        onOpenCampusMap = onOpenCampusMap
    )
}

@Composable
fun AdminScreen() {
    SafetyAuditScreen()
}

@Composable
fun CenteredText(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
