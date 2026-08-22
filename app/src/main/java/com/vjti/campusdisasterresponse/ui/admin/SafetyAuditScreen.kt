package com.vjti.campusdisasterresponse.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class SafetyAuditItem(
    val id: String,
    val title: String,
    val category: String,
    val isCompleted: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class BackendSyncPayload(
    val institutionId: String,
    val items: List<SafetyAuditItem>,
    val syncedAt: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyAuditScreen(
    institutionId: String = "CAMPUS-01",
    onSyncToBackend: (BackendSyncPayload) -> Unit = {}
) {
    var checklistItems by remember {
        mutableStateOf(
            listOf(
                SafetyAuditItem(
                    id = "1",
                    title = "Inspect emergency exits & clear blockages",
                    category = "Fire Safety"
                ),
                SafetyAuditItem(
                    id = "2",
                    title = "Test backup generators & emergency lights",
                    category = "Power & Utility"
                ),
                SafetyAuditItem(
                    id = "3",
                    title = "Verify first aid kits fully stocked",
                    category = "Medical"
                ),
                SafetyAuditItem(
                    id = "4",
                    title = "Check structural integrity of assembly points",
                    category = "Infrastructure"
                ),
                SafetyAuditItem(
                    id = "5",
                    title = "Ensure fire extinguishers meet service date",
                    category = "Fire Safety"
                ),
                SafetyAuditItem(
                    id = "6",
                    title = "Test campus PA system & alarms",
                    category = "Communication"
                )
            )
        )
    }

    var syncStatus by remember {
        mutableStateOf("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Pre-Disaster Safety Audit")
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    val completedCount =
                        checklistItems.count {
                            it.isCompleted
                        }

                    Text(
                        text =
                            "$completedCount/${checklistItems.size} Completed",
                        style =
                            MaterialTheme.typography.bodyLarge
                    )

                    Button(
                        onClick = {
                            val payload =
                                BackendSyncPayload(
                                    institutionId =
                                        institutionId,
                                    items =
                                        checklistItems
                                )

                            onSyncToBackend(
                                payload
                            )

                            syncStatus =
                                "Synced ${checklistItems.size} items"
                        }
                    ) {
                        Text(
                            "Sync Backend"
                        )
                    }
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if (
                syncStatus.isNotEmpty()
            ) {
                Text(
                    text =
                        syncStatus,
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary,
                    modifier =
                        Modifier.padding(
                            horizontal =
                                16.dp,
                            vertical =
                                8.dp
                        )
                )
            }

            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        16.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                items(
                    items =
                        checklistItems,
                    key = {
                        it.id
                    }
                ) { item ->

                    AuditItemRow(
                        item = item,
                        onCheckedChange = {
                            checked ->

                            checklistItems =
                                checklistItems.map {

                                    if (
                                        it.id ==
                                        item.id
                                    ) {
                                        it.copy(
                                            isCompleted =
                                                checked,

                                            lastUpdated =
                                                System
                                                    .currentTimeMillis()
                                        )
                                    } else {
                                        it
                                    }
                                }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AuditItemRow(
    item: SafetyAuditItem,
    onCheckedChange:
        (Boolean) -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    2.dp
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(
                    text =
                        item.title,
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )

                Text(
                    text =
                        item.category,
                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .outline
                )
            }

            Checkbox(
                checked =
                    item.isCompleted,
                onCheckedChange =
                    onCheckedChange
            )
        }
    }
}
