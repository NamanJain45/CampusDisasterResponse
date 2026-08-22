package com.vjti.campusdisasterresponse.quiz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class QuizViewModel : ViewModel() {
    val questions = QuizRepository.questions

    var currentQuestionIndex by mutableStateOf(0)
        private set

    var selectedOptionIndex by mutableStateOf<Int?>(null)
        private set

    var isSubmitted by mutableStateOf(false)
        private set

    var score by mutableStateOf(0)
        private set

    var isQuizFinished by mutableStateOf(false)
        private set

    val currentQuestion: Question?
        get() = questions.getOrNull(currentQuestionIndex)

    fun selectOption(index: Int) {
        if (!isSubmitted) {
            selectedOptionIndex = index
        }
    }

    fun submitAnswer() {
        val q = currentQuestion ?: return

        if (selectedOptionIndex != null && !isSubmitted) {
            isSubmitted = true

            if (selectedOptionIndex == q.correctAnswerIndex) {
                score++
            }
        }
    }

    fun nextQuestion() {
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            selectedOptionIndex = null
            isSubmitted = false
        } else {
            isQuizFinished = true
        }
    }

    fun restartQuiz() {
        currentQuestionIndex = 0
        selectedOptionIndex = null
        isSubmitted = false
        score = 0
        isQuizFinished = false
    }
}
