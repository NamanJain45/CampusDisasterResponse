package com.vjti.campusdisasterresponse.ui.education

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationDashboardScreen(
    modules: List<DisasterModule> = sampleModules,
    onModuleClick: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Education Mode") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Preparedness Modules",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Complete interactive drills and quizzes to stay emergency-ready.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(modules, key = { it.id }) { module ->
                    ModuleCard(
                        module = module,
                        onClick = { onModuleClick(module.id) }
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Phone Layout",
    widthDp = 360,
    heightDp = 640,
    showBackground = true
)
@Preview(
    name = "Tablet Layout",
    widthDp = 800,
    heightDp = 1280,
    showBackground = true
)
@Composable
fun EducationDashboardScreenPreview() {
    MaterialTheme {
        EducationDashboardScreen()
    }
}
