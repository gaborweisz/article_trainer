package com.trainig.articletrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trainig.articletrainer.ui.screens.QuizScreen
import com.trainig.articletrainer.ui.screens.ResultScreen
import com.trainig.articletrainer.ui.screens.StartScreen
import com.trainig.articletrainer.ui.theme.ArticleTrainerTheme
import com.trainig.articletrainer.viewmodel.QuizUiState
import com.trainig.articletrainer.viewmodel.QuizViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArticleTrainerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ArticleTrainerApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ArticleTrainerApp(
    modifier: Modifier = Modifier,
    viewModel: QuizViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is QuizUiState.Loading -> {
            LoadingScreen(modifier = modifier)
        }
        is QuizUiState.Error -> {
            ErrorScreen(message = state.message, modifier = modifier)
        }
        is QuizUiState.Start -> {
            StartScreen(
                maxWords = state.maxWords,
                currentLevel = state.currentLevel,
                onLevelChanged = { level ->
                    viewModel.changeLevel(level)
                },
                onStartQuiz = { wordCount, level ->
                    viewModel.startQuiz(wordCount, level)
                }
            )
        }
        is QuizUiState.Quiz -> {
            QuizScreen(
                currentNoun = state.currentNoun,
                progress = state.progress,
                showHint = state.showHint,
                answerFeedback = state.answerFeedback,
                onToggleHint = { viewModel.toggleHint() },
                onAnswerSelected = { article -> viewModel.submitAnswer(article) },
                onMoveToNext = { viewModel.moveToNext() }
            )
        }
        is QuizUiState.Result -> {
            ResultScreen(
                correctCount = state.correctCount,
                incorrectCount = state.incorrectCount,
                successRate = state.successRate,
                hasFailedWords = state.hasFailedWords,
                isComplete = state.isComplete,
                failedNouns = state.failedNouns,
                originalNouns = state.originalNouns,
                onPracticeFailedWords = { viewModel.practiceFailedWords() },
                onRestartWithNewWords = { viewModel.returnToStart() }
            )
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(message: String, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = "Error: $message",
            color = androidx.compose.material3.MaterialTheme.colorScheme.error
        )
    }
}