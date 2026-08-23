package com.vjti.campusdisasterresponse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vjti.campusdisasterresponse.state.AppViewModel
import com.vjti.campusdisasterresponse.state.AppViewModelFactory
import com.vjti.campusdisasterresponse.state.AppStateRepository
import com.vjti.campusdisasterresponse.sos.ui.SosViewModel
import com.vjti.campusdisasterresponse.sos.ui.SosViewModelFactory
import com.vjti.campusdisasterresponse.worker.SyncScheduler
import com.vjti.campusdisasterresponse.data.local.AppDatabase
import com.vjti.campusdisasterresponse.data.queue.EmergencyQueueRepository
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vjti.campusdisasterresponse.ui.education.EducationDashboardScreen
import com.vjti.campusdisasterresponse.ui.admin.SafetyAuditScreen
import com.vjti.campusdisasterresponse.ui.response.EmergencyResponseScreen

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

    val appStateRepository = remember(context) {
        AppStateRepository(
            AppDatabase
                .getDatabase(context)
                .disasterDao()
        )
    }

    val appViewModel: AppViewModel = viewModel(
        factory = AppViewModelFactory(appStateRepository)
    )

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

    val sosViewModel: SosViewModel = viewModel(
        factory = SosViewModelFactory(queueRepository)
    )
    val items = listOf(
        Screen.Home,
        Screen.Education,
        Screen.Response,
        Screen.Admin
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(appViewModel) }
            composable(Screen.Education.route) { EducationScreen() }
            composable(Screen.Response.route) { ResponseScreen(appViewModel, sosViewModel) }
            composable(Screen.Admin.route) { AdminScreen() }
        }
    }
}

@Composable
fun HomeScreen(
    appViewModel: AppViewModel
) {
    val appState by appViewModel.uiState.collectAsState()

    CenteredText(
        text = "Mode: ${appState.mode.name}\nStatus: ${appState.userStatus.name}"
    )
}

@Composable
fun EducationScreen() {
    EducationDashboardScreen()
}

@Composable
fun ResponseScreen(
    appViewModel: AppViewModel,
    sosViewModel: SosViewModel
) {
    EmergencyResponseScreen(appViewModel, sosViewModel)
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
        Text(text = text, style = MaterialTheme.typography.headlineMedium)
    }
}
