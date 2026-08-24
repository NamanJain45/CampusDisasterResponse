package com.vjti.campusdisasterresponse

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.vjti.campusdisasterresponse.data.local.AppDatabase
import com.vjti.campusdisasterresponse.data.queue.EmergencyQueueRepository
import com.vjti.campusdisasterresponse.network.AuthSessionStore
import com.vjti.campusdisasterresponse.notifications.NotificationHelper
import com.vjti.campusdisasterresponse.sos.ui.*
import com.vjti.campusdisasterresponse.state.*
import com.vjti.campusdisasterresponse.ui.auth.BackendLoginCard
import com.vjti.campusdisasterresponse.ui.education.EducationDashboardScreen
import com.vjti.campusdisasterresponse.ui.management.*
import com.vjti.campusdisasterresponse.ui.map.CampusEvacuationMapScreen
import com.vjti.campusdisasterresponse.ui.response.EmergencyResponseScreen
import com.vjti.campusdisasterresponse.ui.response.ReportIncidentScreen
import com.vjti.campusdisasterresponse.worker.SyncScheduler
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createChannels(this)
        if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        setContent { MaterialTheme { MainApp() } }
    }
}
const val CAMPUS_MAP_ROUTE="campus_map"; const val START_ROUTE="start"; const val SOS_ROUTE="sos"; const val STUDENT_HOME_ROUTE="student_home"; const val MANAGEMENT_HOME_ROUTE="management_home"; const val USER_MANAGEMENT_ROUTE="user_management"; const val STUDENT_SAFETY_ROUTE="student_safety"; const val REPORT_ROUTE="report"; const val INCIDENT_MANAGEMENT_ROUTE="incident_management"
sealed class Screen(val route:String,val title:String,val icon:androidx.compose.ui.graphics.vector.ImageVector) { object Home:Screen(STUDENT_HOME_ROUTE,"Home",Icons.Default.Home); object Education:Screen("education","Education",Icons.Default.List); object Response:Screen("response","Response",Icons.Default.Warning); object Management:Screen(MANAGEMENT_HOME_ROUTE,"Dashboard",Icons.Default.Home); object Users:Screen(USER_MANAGEMENT_ROUTE,"Users",Icons.Default.Settings) }

@Composable fun MainApp() {
    val context=LocalContext.current; val sessionStore=remember(context){AuthSessionStore(context)}; val navController=rememberNavController(); val queueRepository=remember(context){EmergencyQueueRepository(AppDatabase.getDatabase(context).emergencyEventDao()){SyncScheduler.scheduleSync(context)}}; val appStateRepository=remember(context){AppStateRepository(AppDatabase.getDatabase(context).disasterDao(),queueRepository)}; val appViewModel:AppViewModel=viewModel(factory=AppViewModelFactory(appStateRepository)); val sosViewModel:SosViewModel=viewModel(factory=SosViewModelFactory(queueRepository)); val savedRole=sessionStore.getRole(); val loggedIn=!sessionStore.getToken().isNullOrBlank()&&(savedRole=="STUDENT"||savedRole=="STAFF"||savedRole=="ADMIN"); val initialRoute=when(savedRole){"STAFF","ADMIN"->MANAGEMENT_HOME_ROUTE;"STUDENT"->STUDENT_HOME_ROUTE;else->START_ROUTE}; val navBackStackEntry by navController.currentBackStackEntryAsState(); val currentRoute=navBackStackEntry?.destination?.route; val isManagement=savedRole=="STAFF"||savedRole=="ADMIN"; val showNavigation=loggedIn&&currentRoute!=CAMPUS_MAP_ROUTE&&currentRoute!=SOS_ROUTE&&currentRoute!=REPORT_ROUTE&&currentRoute!=STUDENT_SAFETY_ROUTE&&currentRoute!=INCIDENT_MANAGEMENT_ROUTE; val logout:()->Unit={sessionStore.clearSession();navController.navigate(START_ROUTE){popUpTo(navController.graph.findStartDestination().id){inclusive=true}}}
    Scaffold(bottomBar={if(showNavigation){val items=if(isManagement)listOf(Screen.Management,Screen.Response,Screen.Users)else listOf(Screen.Home,Screen.Education,Screen.Response);NavigationBar{items.forEach{screen->NavigationBarItem(icon={Icon(screen.icon,screen.title)},label={Text(screen.title)},selected=currentRoute==screen.route,onClick={navController.navigate(screen.route){popUpTo(navController.graph.findStartDestination().id){saveState=true};launchSingleTop=true;restoreState=true}})}}}}){innerPadding->NavHost(navController,startDestination=initialRoute,modifier=Modifier.padding(innerPadding)){composable(START_ROUTE){StartScreen({val role=sessionStore.getRole();navController.navigate(if(role=="STAFF"||role=="ADMIN")MANAGEMENT_HOME_ROUTE else STUDENT_HOME_ROUTE){popUpTo(START_ROUTE){inclusive=true}}},{navController.navigate(SOS_ROUTE)})};composable(SOS_ROUTE){SosScreen(sosViewModel)};composable(STUDENT_HOME_ROUTE){StudentHomeScreen(sessionStore.getUserName()?:"Student",logout,{navController.navigate(SOS_ROUTE)},{navController.navigate(CAMPUS_MAP_ROUTE)},{navController.navigate("education")},{navController.navigate(STUDENT_SAFETY_ROUTE)},{navController.navigate(REPORT_ROUTE)})};composable(Screen.Education.route){EducationScreen()};composable(Screen.Response.route){ResponseScreen(appViewModel,sosViewModel){navController.navigate(CAMPUS_MAP_ROUTE)}};composable(CAMPUS_MAP_ROUTE){CampusEvacuationMapScreen{navController.popBackStack()}};composable(REPORT_ROUTE){ReportIncidentScreen(sessionStore.getToken()?:""){navController.popBackStack()}};composable(INCIDENT_MANAGEMENT_ROUTE){IncidentManagementScreen(sessionStore.getToken()?:""){navController.popBackStack()}};composable(MANAGEMENT_HOME_ROUTE){ManagementDashboardScreen(sessionStore.getUserName()?:"Staff",savedRole?:"STAFF",{navController.navigate(USER_MANAGEMENT_ROUTE)},{navController.navigate(STUDENT_SAFETY_ROUTE)},onOpenIncidentReports={navController.navigate(INCIDENT_MANAGEMENT_ROUTE)},onReportIncident={navController.navigate(REPORT_ROUTE)},onOpenCampusMap={navController.navigate(CAMPUS_MAP_ROUTE)},onBroadcastEmergency={navController.navigate(Screen.Response.route)},onLogout=logout)};composable(STUDENT_SAFETY_ROUTE){StudentSafetyScreen(sessionStore.getToken()?:""){navController.popBackStack()}};composable(USER_MANAGEMENT_ROUTE){UserManagementScreen(sessionStore,savedRole?:"STAFF")}}}
}
private val safetyTips=listOf("EARTHQUAKE • Drop, Cover, and Hold On. Stay away from windows.","FIRE • Use stairs instead of elevators during a fire evacuation.","FIRE • If you see smoke, stay low and move toward the nearest safe exit.","EARTHQUAKE • After shaking stops, watch for falling objects and damaged structures.","FLOOD • Never walk through moving floodwater.","MEDICAL • Tell responders your exact location when someone needs help.","EVACUATION • Follow official instructions and don't return until told it is safe.","GENERAL • Keep your phone charged whenever possible during an emergency.","GENERAL • Know at least two ways out of every building you regularly use.","FIRE • If a door feels hot, don't open it. Find another exit.","EARTHQUAKE • If outside, move away from buildings, poles, and power lines.","EMERGENCY • If you are safe, update your safety status so responders know.","EMERGENCY • If you need rescue, give your most accurate location.","EVACUATION • Don't push during evacuation; move steadily toward safe exits.","GENERAL • Keep emergency contacts and important information accessible offline.")
@Composable fun StartScreen(onSignedIn:()->Unit,onEmergencySos:()->Unit)=BackendLoginCard("Campus Disaster Response",onSignedIn,onEmergencySos)
@Composable fun StudentHomeScreen(name:String,onLogout:()->Unit,onEmergency:()->Unit,onMap:()->Unit,onEducation:()->Unit,onSafety:()->Unit,onReportIncident:()->Unit){val tip=remember(name){safetyTips[Random.nextInt(safetyTips.size)]};Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column{Text("Welcome, $name 👋",style=MaterialTheme.typography.headlineMedium);Text("Campus Safety",style=MaterialTheme.typography.bodyMedium)};TextButton(onClick=onLogout){Text("LOG OUT")}};Card(Modifier.fillMaxWidth()){Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("🛡️ TODAY'S SAFETY TIP",style=MaterialTheme.typography.titleLarge);Text(tip,style=MaterialTheme.typography.bodyLarge)}};Text("QUICK ACCESS",style=MaterialTheme.typography.titleMedium);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){Button(onClick=onEmergency,Modifier.weight(1f).height(70.dp)){Text("🚨\nSOS")};Button(onClick=onMap,Modifier.weight(1f).height(70.dp)){Text("🗺️\nMAP")}};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){Button(onClick=onEducation,Modifier.weight(1f).height(70.dp)){Text("📚\nEDUCATION")};Button(onClick=onSafety,Modifier.weight(1f).height(70.dp)){Text("👤\nMY SAFETY")}};OutlinedButton(onClick=onReportIncident,modifier=Modifier.fillMaxWidth()){Text("📋 REPORT AN INCIDENT")}}}
@Composable fun EducationScreen()=EducationDashboardScreen();@Composable fun ResponseScreen(appViewModel:AppViewModel,sosViewModel:SosViewModel,onOpenCampusMap:()->Unit)=EmergencyResponseScreen(appViewModel,sosViewModel,onOpenCampusMap);@Composable fun CenteredText(text:String){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text(text,style=MaterialTheme.typography.headlineMedium)}}
