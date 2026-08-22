package com.vjti.campusdisasterresponse.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class StatusCategory(
    val label: String,
    val color: Color
) {
    SAFE(
        "Safe",
        Color(0xFF4CAF50)
    ),

    TRAPPED(
        "Trapped",
        Color(0xFFF44336)
    ),

    FIRST_AID(
        "Need First Aid",
        Color(0xFFFF9800)
    ),

    ASSISTANCE(
        "Need Assistance",
        Color(0xFFFFC107)
    )
}

data class UserReport(
    val id: String,
    val name: String,
    val status: StatusCategory,
    val lastKnownLocation: String,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserStatusMonitorScreen(
    reports: List<UserReport>
) {
    val groupedReports =
        reports.groupBy {
            it.status
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Institutional Status Monitor"
                    )
                },
                colors =
                    TopAppBarDefaults
                        .topAppBarColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .primaryContainer
                        )
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(
                    paddingValues
                )
                .fillMaxSize()
                .padding(
                    horizontal =
                        16.dp
                ),
            contentPadding =
                PaddingValues(
                    vertical =
                        16.dp
                ),
            verticalArrangement =
                Arrangement
                    .spacedBy(
                        16.dp
                    )
        ) {

            StatusCategory
                .values()
                .forEach { category ->

                    val categoryReports =
                        groupedReports[
                            category
                        ] ?: emptyList()

                    if (
                        categoryReports
                            .isNotEmpty()
                    ) {

                        item {

                            Text(
                                text =
                                    "${category.label} (${categoryReports.size})",

                                style =
                                    MaterialTheme
                                        .typography
                                        .titleMedium,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    category.color,

                                modifier =
                                    Modifier
                                        .padding(
                                            bottom =
                                                8.dp,
                                            top =
                                                8.dp
                                        )
                            )
                        }

                        items(
                            items =
                                categoryReports,
                            key = {
                                it.id
                            }
                        ) { report ->

                            UserReportCard(
                                report
                            )
                        }
                    }
                }
        }
    }
}

@Composable
fun UserReportCard(
    report: UserReport
) {
    val timeFormatted =
        SimpleDateFormat(
            "HH:mm:ss",
            Locale.getDefault()
        ).format(
            Date(
                report.timestamp
            )
        )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom =
                    8.dp
            ),
        elevation =
            CardDefaults
                .cardElevation(
                    defaultElevation =
                        2.dp
                ),
        colors =
            CardDefaults
                .cardColors(
                    containerColor =
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                horizontalArrangement =
                    Arrangement
                        .SpaceBetween
            ) {

                Text(
                    text =
                        report.name,
                    fontWeight =
                        FontWeight.Bold,
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge
                )

                Text(
                    text =
                        timeFormatted,
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

            Text(
                text =
                    "Location: ${report.lastKnownLocation}",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true
)
@Composable
fun PreviewUserStatusMonitorScreen() {

    val mockData =
        listOf(

            UserReport(
                id = "1",
                name = "Yash",
                status =
                    StatusCategory.SAFE,
                lastKnownLocation =
                    "Library, Floor 2",
                timestamp =
                    System.currentTimeMillis() -
                    120000
            ),

            UserReport(
                id = "2",
                name = "Sanya",
                status =
                    StatusCategory.TRAPPED,
                lastKnownLocation =
                    "Lab 3, North Wing",
                timestamp =
                    System.currentTimeMillis() -
                    60000
            ),

            UserReport(
                id = "3",
                name = "Naman",
                status =
                    StatusCategory.FIRST_AID,
                lastKnownLocation =
                    "Cafeteria Exit",
                timestamp =
                    System.currentTimeMillis() -
                    300000
            ),

            UserReport(
                id = "4",
                name = "Vedant",
                status =
                    StatusCategory.SAFE,
                lastKnownLocation =
                    "Main Gate",
                timestamp =
                    System.currentTimeMillis() -
                    45000
            ),

            UserReport(
                id = "5",
                name = "Varun",
                status =
                    StatusCategory.ASSISTANCE,
                lastKnownLocation =
                    "Stairwell B",
                timestamp =
                    System.currentTimeMillis() -
                    10000
            )
        )

    MaterialTheme {
        UserStatusMonitorScreen(
            reports =
                mockData
        )
    }
}
