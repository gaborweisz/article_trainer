package com.trainig.articletrainer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trainig.articletrainer.data.CsvParser
import com.trainig.articletrainer.data.NounEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel managing the German Articles Trainer quiz state.
 *
 * Handles:
 * - Loading nouns from CSV based on selected level
 * - Quiz state management
 * - Tracking correct/incorrect answers
 * - Managing failed words for practice rounds
 * - Preserving state across configuration changes
 *
 * Handles:
 * - Loading nouns from CSV based on selected level
 * - Quiz state management
 * - Tracking correct/incorrect answers
 * - Managing failed words for practice rounds
 * - Preserving state across configuration changes
 */
    // Current language level

    private val csvParser = CsvParser()

    // All nouns loaded from CSV
    private var allNouns: List<NounEntry> = emptyList()

    // Current language level
    private var currentLevel: String = "A1"

    // UI State
            try {
                currentLevel = level
                android.util.Log.d("QuizViewModel", "Loading nouns from: $fileName")
                allNouns = csvParser.parseFromAssets(getApplication(), fileName)
                android.util.Log.d("QuizViewModel", "Loaded ${allNouns.size} nouns from $fileName")
                _uiState.value = QuizUiState.Start(maxWords = allNouns.size, currentLevel = currentLevel)
                currentLevel = level
                val fileName = "german_nouns_${level.lowercase()}.csv"
                android.util.Log.e("QuizViewModel", "Error loading nouns", e)
                allNouns = csvParser.parseFromAssets(getApplication(), fileName)
    }

    /**
     * Change language level and reload nouns.
     */
    fun changeLevel(level: String) {
        if (level != currentLevel) {
            _uiState.value = QuizUiState.Loading
            loadNouns(level)
        }
    }

    /**
     * Start a new quiz with N random words.
     */
    fun startQuiz(wordCount: Int, level: String) {
        android.util.Log.d("QuizViewModel", "startQuiz called with wordCount=$wordCount, level=$level, currentLevel=$currentLevel")
        // If level changed, reload nouns first
        if (level != currentLevel) {
            viewModelScope.launch {
                try {
                    currentLevel = level
                    val fileName = "german_nouns_${level.lowercase()}.csv"
                    android.util.Log.d("QuizViewModel", "Level changed, loading from: $fileName")
        // If level changed, reload nouns first
        if (level != currentLevel) {
                    val selectedNouns = allNouns.shuffled().take(wordCount)
                    startQuizWithNouns(selectedNouns, isRetryRound = false)
            }
                    val fileName = "german_nouns_${level.lowercase()}.csv"
                    android.util.Log.d("QuizViewModel", "Level changed, loading from: $fileName")
                    allNouns = csvParser.parseFromAssets(getApplication(), fileName)
        } else {
            android.util.Log.d("QuizViewModel", "Using cached nouns: ${allNouns.size}")
            val selectedNouns = allNouns.shuffled().take(wordCount)
            startQuizWithNouns(selectedNouns, isRetryRound = false)
        }
    }

    /**
     * Start quiz with specific nouns (used for failed words practice).
     */
    private fun startQuizWithNouns(nouns: List<NounEntry>, isRetryRound: Boolean) {
        if (nouns.isEmpty()) {
            _uiState.value = QuizUiState.Start(maxWords = allNouns.size, currentLevel = currentLevel)
            return
        }

        _uiState.value = QuizUiState.Quiz(
            currentIndex = 0,
            nouns = nouns,
            correctCount = 0,
            incorrectCount = 0,
            failedNouns = emptySet(),
            showHint = false,
            answerFeedback = null,
            originalNouns = if (isRetryRound) {
                ((_uiState.value as? QuizUiState.Result)?.originalNouns ?: nouns)
            } else {
                nouns
            }
        )
    }

    /**
     * Toggle hint visibility for current word.
     */
    fun toggleHint() {
        val currentState = _uiState.value as? QuizUiState.Quiz ?: return
        _uiState.value = currentState.copy(showHint = !currentState.showHint)
    }

    /**
     * Submit an answer for the current word.
     */
    fun submitAnswer(selectedArticle: String) {
        val currentState = _uiState.value as? QuizUiState.Quiz ?: return

        // Prevent double submission
        if (currentState.answerFeedback != null) return

        val currentNoun = currentState.nouns[currentState.currentIndex]
        val isCorrect = selectedArticle == currentNoun.article

        val newCorrectCount = if (isCorrect) currentState.correctCount + 1 else currentState.correctCount
        val newIncorrectCount = if (isCorrect) currentState.incorrectCount else currentState.incorrectCount + 1
        val newFailedNouns = if (isCorrect) {
            currentState.failedNouns
        } else {
            currentState.failedNouns + currentNoun
        }

        // Update state with feedback
        _uiState.value = currentState.copy(
            correctCount = newCorrectCount,
            incorrectCount = newIncorrectCount,
            failedNouns = newFailedNouns,
            answerFeedback = AnswerFeedback(isCorrect, currentNoun.article)
        )
    }

    /**
     * Move to next word (called after delay).
     */
    fun moveToNext() {
        val currentState = _uiState.value as? QuizUiState.Quiz ?: return

        val nextIndex = currentState.currentIndex + 1

        if (nextIndex >= currentState.nouns.size) {
            // Quiz finished, show results
            _uiState.value = QuizUiState.Result(
                correctCount = currentState.correctCount,
                incorrectCount = currentState.incorrectCount,
                failedNouns = currentState.failedNouns.toList(),
                originalNouns = currentState.originalNouns
            )
        } else {
            // Move to next word
            _uiState.value = currentState.copy(
                currentIndex = nextIndex,
                showHint = false,
                answerFeedback = null
            )
        }
    }

    /**
     * Practice failed words from the last round.
     */
    fun practiceFailedWords() {
        val resultState = _uiState.value as? QuizUiState.Result ?: return
        startQuizWithNouns(resultState.failedNouns, isRetryRound = true)
    }

    /**
     * Return to start screen.
     */
    fun returnToStart() {
        _uiState.value = QuizUiState.Start(maxWords = allNouns.size, currentLevel = currentLevel)
    }
}

/**
 * Sealed class representing different UI states.
 */
sealed class QuizUiState {
    object Loading : QuizUiState()
    data class Error(val message: String) : QuizUiState()
    data class Start(val maxWords: Int, val currentLevel: String) : QuizUiState()

    data class Quiz(
        val currentIndex: Int,
        val nouns: List<NounEntry>,
        val correctCount: Int,
        val incorrectCount: Int,
        val failedNouns: Set<NounEntry>,
        val showHint: Boolean,
        val answerFeedback: AnswerFeedback?,
        val originalNouns: List<NounEntry> // Track original selection for final screen
    ) : QuizUiState() {
        val currentNoun: NounEntry
            get() = nouns[currentIndex]

        val totalWords: Int
            get() = nouns.size

        val progress: String
            get() = "Word ${currentIndex + 1} of $totalWords"
    }

    data class Result(
        val correctCount: Int,
        val incorrectCount: Int,
        val failedNouns: List<NounEntry>,
        val originalNouns: List<NounEntry>
    ) : QuizUiState() {
        val totalAnswers: Int
            get() = correctCount + incorrectCount

        val successRate: Int
            get() = if (totalAnswers > 0) {
                ((correctCount.toFloat() / totalAnswers) * 100).toInt()
            } else 0

        val hasFailedWords: Boolean
            get() = failedNouns.isNotEmpty()

        val isComplete: Boolean
            get() = failedNouns.isEmpty()
    }
}

/**
 * Feedback shown after answering.
 */
data class AnswerFeedback(
    val isCorrect: Boolean,
    val correctArticle: String
)
