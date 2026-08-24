package com.vjti.campusdisasterresponse.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vjti.campusdisasterresponse.R
import com.vjti.campusdisasterresponse.network.AuthSessionStore
import com.vjti.campusdisasterresponse.network.MapIncident
import com.vjti.campusdisasterresponse.network.MapIncidentClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusEvacuationMapScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val token = remember { AuthSessionStore(context).getToken().orEmpty() }
    val scope = rememberCoroutineScope()
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var incidents by remember { mutableStateOf<List<MapIncident>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var kindFilter by remember { mutableStateOf("ALL") }
    var statusFilter by remember { mutableStateOf("ALL") }
    var loadMessage by remember { mutableStateOf("Loading incident locations…") }

    suspend fun loadIncidents() {
        if (token.isBlank()) return
        val result = withContext(Dispatchers.IO) { MapIncidentClient().list(token) }
        result.onSuccess { incidents = it; loadMessage = if (it.isEmpty()) "No incident/SOS locations recorded" else "${it.size} locations" }
            .onFailure { loadMessage = "Incident locations unavailable" }
    }

    LaunchedEffect(token) { loadIncidents() }

    val visible = incidents.filter { item ->
        val kindOk = kindFilter == "ALL" || item.kind == kindFilter
        val statusOk = statusFilter == "ALL" || item.status == statusFilter
        val queryOk = query.isBlank() || listOf(item.type, item.location, item.status, item.kind).any { it.contains(query, ignoreCase = true) }
        kindOk && statusOk && queryOk
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("VJTI Campus Safety Map", color = Color.White) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
        )
    }) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            Box(Modifier.fillMaxWidth().weight(1f).background(Color(0xFF101010))) {
                Image(
                    painter = painterResource(R.drawable.vjti_campus_map),
                    contentDescription = "VJTI campus layout reference",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale; translationX = offset.x; translationY = offset.y }
                        .pointerInput(Unit) { detectTransformGestures { _, pan, zoom, _ -> val newScale = (scale * zoom).coerceIn(1f, 5f); scale = newScale; offset = if (newScale <= 1.01f) Offset.Zero else offset + pan } }
                )
                Column(Modifier.align(Alignment.BottomCenter).background(Color(0xDD000000)).padding(10.dp)) {
                    Text("Reference campus layout • Pinch to zoom • Drag to pan", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Incident and SOS records below use reporter GPS when available.", color = Color.LightGray, fontSize = 10.sp)
                }
            }
            Column(Modifier.fillMaxWidth().heightIn(max = 360.dp).padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("INCIDENT & SOS LOCATIONS", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { scope.launch { loadIncidents() } }) { Text("REFRESH") }
                }
                Text(loadMessage, style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Search type/location/status") })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("ALL", "INCIDENT", "SOS").forEach { value ->
                        TextButton(onClick = { kindFilter = value }) { Text(if (kindFilter == value) "[$value]" else value) }
                    }
                    listOf("ALL", "ACTIVE", "RESOLVED").forEach { value ->
                        TextButton(onClick = { statusFilter = value }) { Text(if (statusFilter == value) "[$value]" else value) }
                    }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(visible, key = { "${it.kind}:${it.id}" }) { incident ->
                        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(10.dp)) {
                            Text("${incident.kind} • ${incident.type} • ${incident.status}", style = MaterialTheme.typography.titleSmall)
                            Text(incident.location)
                            if (incident.latitude != null && incident.longitude != null) Text("GPS: ${incident.latitude}, ${incident.longitude}", style = MaterialTheme.typography.bodySmall)
                            Text(incident.createdAt, style = MaterialTheme.typography.bodySmall)
                        } }
                    }
                }
            }
        }
    }
}
