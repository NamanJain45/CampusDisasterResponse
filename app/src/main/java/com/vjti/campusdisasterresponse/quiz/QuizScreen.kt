package com.vjti.campusdisasterresponse.quiz

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun QuizScreen(viewModel: QuizViewModel = viewModel()) {
    if (viewModel.isQuizFinished) {
        QuizResultView(
            score = viewModel.score,
            total = viewModel.questions.size,
            onRestart = { viewModel.restartQuiz() }
        )
    } else {
        val question = viewModel.currentQuestion ?: return

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Question ${viewModel.currentQuestionIndex + 1} of ${viewModel.questions.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = question.text,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                question.options.forEachIndexed { index, optionText ->
                    val isSelected = viewModel.selectedOptionIndex == index
                    val isCorrect = index == question.correctAnswerIndex

                    val backgroundColor = when {
                        viewModel.isSubmitted && isCorrect ->
                            Color(0xFFD4EDDA)

                        viewModel.isSubmitted && isSelected && !isCorrect ->
                            Color(0xFFF8D7DA)

                        isSelected ->
                            MaterialTheme.colorScheme.primaryContainer

                        else ->
                            MaterialTheme.colorScheme.surfaceVariant
                    }

                    val borderColor = when {
                        viewModel.isSubmitted && isCorrect ->
                            Color(0xFF28A745)

                        viewModel.isSubmitted && isSelected && !isCorrect ->
                            Color(0xFFDC3545)

                        isSelected ->
                            MaterialTheme.colorScheme.primary

                        else ->
                            Color.Transparent
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .border(
                                2.dp,
                                borderColor,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = !viewModel.isSubmitted) {
                                viewModel.selectOption(index)
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = backgroundColor
                        )
                    ) {
                        Text(
                            text = optionText,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (viewModel.isSubmitted) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor =
                                if (
                                    viewModel.selectedOptionIndex ==
                                    question.correctAnswerIndex
                                ) {
                                    Color(0xFFE8F5E9)
                                } else {
                                    Color(0xFFFFEBEE)
                                }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text =
                                    if (
                                        viewModel.selectedOptionIndex ==
                                        question.correctAnswerIndex
                                    ) {
                                        "Correct"
                                    } else {
                                        "Incorrect"
                                    },
                                fontWeight = FontWeight.Bold,
                                color =
                                    if (
                                        viewModel.selectedOptionIndex ==
                                        question.correctAnswerIndex
                                    ) {
                                        Color(0xFF2E7D32)
                                    } else {
                                        Color(0xFFC62828)
                                    }
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = question.explanation,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (viewModel.isSubmitted) {
                        viewModel.nextQuestion()
                    } else {
                        viewModel.submitAnswer()
                    }
                },
                enabled = viewModel.selectedOptionIndex != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = when {
                        !viewModel.isSubmitted ->
                            "Submit Answer"

                        viewModel.currentQuestionIndex ==
                            viewModel.questions.size - 1 ->
                            "View Results"

                        else ->
                            "Next Question"
                    }
                )
            }
        }
    }
}

@Composable
fun QuizResultView(
    score: Int,
    total: Int,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quiz Completed",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Score: $score / $total",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Retake Quiz")
        }
    }
}
