package com.vjti.campusdisasterresponse.ui.education

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ModuleDetailScreen(module: DisasterModule, onBack: () -> Unit) {
    val context = LocalContext.current
    val answers = remember(module.id) { mutableStateMapOf<Int, Int>() }
    var submitted by remember(module.id) { mutableStateOf(false) }
    val quiz = module.sections.filterIsInstance<ModuleSection.Quiz>().firstOrNull()
    val score = quiz?.questions?.countIndexed { index, question -> answers[index] == question.correctIndex } ?: 0

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(module.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onBack) { Text("BACK") }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            itemsIndexed(module.sections) { _, section ->
                when (section) {
                    is ModuleSection.Instruction -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(section.title, style = MaterialTheme.typography.titleLarge); Text(section.description); section.keyTakeaways.forEach { Text("• $it") } } }
                    is ModuleSection.Quiz -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Quick MCQ", style = MaterialTheme.typography.titleLarge); section.questions.forEachIndexed { index, question -> Text(question.question, style = MaterialTheme.typography.titleMedium); question.options.forEachIndexed { optionIndex, option -> Row { RadioButton(selected = answers[index] == optionIndex, onClick = { if (!submitted) answers[index] = optionIndex }); Text(option, modifier = Modifier.padding(top = 12.dp)) } } } Button(onClick = { submitted = true }, enabled = answers.size == section.questions.size) { Text("CHECK ANSWERS") }; if (submitted) Text("Score: $score / ${section.questions.size}") } }
                    is ModuleSection.VideoLearning -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(section.title, style = MaterialTheme.typography.titleLarge); if (section.videoUrl.isBlank()) Text("Video link will be added when the YouTube link is provided.") else Button(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(section.videoUrl))) }) { Text("WATCH VIDEO") }; Text(section.duration, style = MaterialTheme.typography.bodySmall) } }
                    is ModuleSection.VisualGuide -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { section.steps.forEach { Text("${it.stepNumber}. ${it.title}", style = MaterialTheme.typography.titleMedium); Text(it.description) } } }
                }
            }
        }
    }
}

private inline fun <T> Iterable<T>.countIndexed(predicate: (Int, T) -> Boolean): Int { var count = 0; for ((index, item) in withIndex()) if (predicate(index, item)) count++; return count }
