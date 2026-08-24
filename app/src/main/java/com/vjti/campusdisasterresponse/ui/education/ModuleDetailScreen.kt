package com.vjti.campusdisasterresponse.ui.education

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ModuleDetailScreen(module: DisasterModule, onBack: () -> Unit) {
    val context = LocalContext.current
    val answers = remember(module.id) { mutableStateMapOf<Int, Int>() }
    var submitted by remember(module.id) { mutableStateOf(false) }
    val quiz = module.sections.filterIsInstance<ModuleSection.Quiz>().firstOrNull()

    val score = quiz?.questions?.count { question ->
        val questionIndex = quiz.questions.indexOf(question)
        answers[questionIndex] == question.correctIndex
    } ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = module.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onBack) {
                Text("BACK")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            itemsIndexed(module.sections) { _, section ->
                when (section) {
                    is ModuleSection.Instruction -> {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(section.title, style = MaterialTheme.typography.titleLarge)
                                Text(section.description)
                                section.keyTakeaways.forEach { takeaway -> Text("• $takeaway") }
                            }
                        }
                    }

                    is ModuleSection.Quiz -> {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Quick MCQ", style = MaterialTheme.typography.titleLarge)
                                section.questions.forEachIndexed { index, question ->
                                    Text("${index + 1}. ${question.question}", style = MaterialTheme.typography.titleMedium)
                                    question.options.forEachIndexed { optionIndex, option ->
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            RadioButton(
                                                selected = answers[index] == optionIndex,
                                                onClick = { if (!submitted) answers[index] = optionIndex },
                                                enabled = !submitted
                                            )
                                            Text(option, modifier = Modifier.padding(top = 12.dp))
                                        }
                                    }
                                }
                                Button(
                                    onClick = { submitted = true },
                                    enabled = !submitted && answers.size == section.questions.size
                                ) {
                                    Text("CHECK ANSWERS")
                                }
                                if (submitted) {
                                    Text("Score: $score / ${section.questions.size}", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }

                    is ModuleSection.VideoLearning -> {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(section.title, style = MaterialTheme.typography.titleLarge)
                                if (section.videoUrl.isBlank()) {
                                    Text("Video link will be added when the YouTube link is provided.")
                                } else {
                                    Button(onClick = {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(section.videoUrl)))
                                    }) {
                                        Text("WATCH VIDEO")
                                    }
                                }
                                Text(section.duration, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    is ModuleSection.VisualGuide -> {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                section.steps.forEach { step ->
                                    Text("${step.stepNumber}. ${step.title}", style = MaterialTheme.typography.titleMedium)
                                    Text(step.description)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
