package com.vjti.campusdisasterresponse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vjti.campusdisasterresponse.data.local.AppDatabase
import com.vjti.campusdisasterresponse.data.queue.EmergencyQueueRepository
import com.vjti.campusdisasterresponse.network.AuthSessionStore
import com.vjti.campusdisasterresponse.sos.ui.SosViewModel
import com.vjti.campusdisasterresponse.sos.ui.SosViewModelFactory
import com.vjti.campusdisasterresponse.state.AppStateRepository
import com.vjti.campusdisasterresponse.state.AppViewModel
import com.vjti.campusdisasterresponse.state.AppViewModelFactory
import com.vjti.campusdisasterresponse.ui.auth.BackendLoginCard
import com.vjti.campusdisasterresponse.ui.education.EducationDashboardScreen
import com.vjti.campusdisasterresponse.ui.management.ManagementDashboardScreen
import com.vjti.campusdisasterresponse.ui.management.UserManagementScreen
import com.vjti.campusdisasterresponse.ui.map.CampusEvacuationMapScreen
import com.vjti.campusdisasterresponse.ui.response.EmergencyResponseScreen
import com.vjti.campusdisasterresponse.ui.response.SosOnlyScreen
import com.vjti.campusdisasterresponse.worker.SyncScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { MainApp() } }
    }
}

const val CAMPUS_MAP_ROUTE = "campus_map"
const val START_ROUTE = "start"
const val SOS_ROUTE = "sos"
const val STUDENT_HOME_ROUTE = "student_home"
const val MANAGEMENT_HOME_ROUTE = "management_home"
const val USER_MANAGEMENT_ROUTE = "user_management"

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen(STUDENT_HOME_ROUTE, "Home", Icons.Default.Home)
    object Education : Screen("education", "Education", Icons.Default.List)
    object Response : Screen("response", "Response", Icons.Default.Warning)
    object Management : Screen(MANAGEMENT_HOME_ROUTE, "Dashboard", Icons.Default.Home)
    object Users : Screen(USER_MANAGEMENT_ROUTE, "Users", Icons.Default.Settings)
}

@Composable
fun MainApp() {
    val context = LocalContext.current
    val sessionStore = remember(context) { AuthSessionStore(context) }
    val navController = rememberNavController()
    val queueRepository = remember(context) {
        EmergencyQueueRepository(
            dao = AppDatabase.getDatabase(context).emergencyEventDao(),
            scheduleSync = { SyncScheduler.scheduleSync(context) }
        )
    }
    val appStateRepository = remember(context) {
        AppStateRepository(
            dao = AppDatabase.getDatabase(context).disasterDao(),
            queueRepository = queueRepository
        )
    }
    val appViewModel: AppViewModel = viewModel(factory = AppViewModelFactory(appStateRepository))
    val sosViewModel: SosViewModel = viewModel(factory = SosViewModelFactory(queueRepository))

    val savedRole = sessionStore.getRole()
    val loggedIn = !sessionStore.getToken().isNullOrBlank() &&
        (savedRole == "STUDENT" || savedRole == "STAFF" || savedRole == "ADMIN")
    val initialRoute = when (savedRole) {
        "STAFF", "ADMIN" -> MANAGEMENT_HOME_ROUTE
        "STUDENT" -> STUDENT_HOME_ROUTE
        else -> START_ROUTE
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isManagement = savedRole == "STAFF" || savedRole == "ADMIN"
    val showNavigation = loggedIn && currentRoute != CAMPUS_MAP_ROUTE && currentRoute != SOS_ROUTE

    val logout: () -> Unit = {
        sessionStore.clearSession()
        navController.navigate(START_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
        }
    }

    Scaffold(
        bottomBar = {
            if (showNavigation) {
                val items = if (isManagement) {
                    listOf(Screen.Management, Screen.Response, Screen.Users)
                } else {
                    listOf(Screen.Home, Screen.Education, Screen.Response)
                }
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
        NavHost(navController = navController, startDestination = initialRoute, modifier = Modifier.padding(innerPadding)) {
            composable(START_ROUTE) {
                StartScreen(
                    onSignedIn = {
                        val role = sessionStore.getRole()
                        navController.navigate(if (role == "STAFF" || role == "ADMIN") MANAGEMENT_HOME_ROUTE else STUDENT_HOME_ROUTE) {
                            popUpTo(START_ROUTE) { inclusive = true }
                        }
                    },
                    onEmergencySos = { navController.navigate(SOS_ROUTE) }
                )
            }
            composable(SOS_ROUTE) { SosOnlyScreen(sosViewModel) }
            composable(STUDENT_HOME_ROUTE) { StudentHomeScreen(sessionStore.getUserName() ?: "Student", logout) }
            composable(Screen.Education.route) { EducationScreen() }
            composable(Screen.Response.route) {
                ResponseScreen(appViewModel, sosViewModel) { navController.navigate(CAMPUS_MAP_ROUTE) }
            }
            composable(CAMPUS_MAP_ROUTE) { CampusEvacuationMapScreen { navController.popBackStack() } }
            composable(MANAGEMENT_HOME_ROUTE) {
                ManagementDashboardScreen(
                    name = sessionStore.getUserName() ?: "Staff",
                    role = savedRole ?: "STAFF",
                    onOpenUserManagement = { navController.navigate(USER_MANAGEMENT_ROUTE) },
                    onLogout = logout
                )
            }
            composable(USER_MANAGEMENT_ROUTE) {
                UserManagementScreen(sessionStore = sessionStore, role = savedRole ?: "STAFF")
            }
        }
    }
}

@Composable
fun StartScreen(onSignedIn: () -> Unit, onEmergencySos: () -> Unit) {
    BackendLoginCard("Campus Disaster Response", onSignedIn, onEmergencySos)
}

@Composable
fun StudentHomeScreen(name: String, onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Welcome, $name", style = MaterialTheme.typography.headlineMedium)
        Text("Student dashboard")
        Button(onClick = onLogout) { Text("LOG OUT") }
    }
}

@Composable
fun EducationScreen() { EducationDashboardScreen() }

@Composable
fun ResponseScreen(appViewModel: AppViewModel, sosViewModel: SosViewModel, onOpenCampusMap: () -> Unit) {
    EmergencyResponseScreen(appViewModel, sosViewModel, onOpenCampusMap)
}

@Composable
fun CenteredText(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, style = MaterialTheme.typography.headlineMedium)
    }
}
