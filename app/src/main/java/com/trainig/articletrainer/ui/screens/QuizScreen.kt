package com.trainig.articletrainer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trainig.articletrainer.data.NounEntry
import com.trainig.articletrainer.viewmodel.AnswerFeedback
import kotlinx.coroutines.delay

/**
 * Quiz screen showing the current noun and article buttons.
 */
@Composable
fun QuizScreen(
    currentNoun: NounEntry,
    progress: String,
    showHint: Boolean,
    answerFeedback: AnswerFeedback?,
    onToggleHint: () -> Unit,
    onAnswerSelected: (String) -> Unit,
    onMoveToNext: () -> Unit,
    onBackToStart: () -> Unit
) {
    var showBackDialog by remember { mutableStateOf(false) }

    // Confirmation dialog
    if (showBackDialog) {
        AlertDialog(
            onDismissRequest = { showBackDialog = false },
            title = { Text("Back to main page?") },
            text = { Text("Your current progress will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    showBackDialog = false
                    onBackToStart()
                }) {
                    Text("Yes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    // Auto-advance to next word after feedback is shown
    LaunchedEffect(answerFeedback) {
        if (answerFeedback != null) {
            delay(3000)
            onMoveToNext()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar: back arrow (left) + progress (center)
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { showBackDialog = true },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to main page",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = progress,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // The noun (without article)
        Text(
            text = currentNoun.noun,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hint button
        OutlinedButton(
            onClick = onToggleHint,
            enabled = answerFeedback == null
        ) {
            Text(text = if (showHint) "Hide Hint" else "Show Hint")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hint section
        AnimatedVisibility(visible = showHint) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "German Example:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = currentNoun.germanExample,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "English Translation:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = currentNoun.english,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "English Example:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = currentNoun.englishExample,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Feedback section
        answerFeedback?.let { feedback ->
            FeedbackSection(feedback)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Article buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ArticleButton(
                article = "der",
                onClick = { onAnswerSelected("der") },
                enabled = answerFeedback == null
            )
            ArticleButton(
                article = "die",
                onClick = { onAnswerSelected("die") },
                enabled = answerFeedback == null
            )
            ArticleButton(
                article = "das",
                onClick = { onAnswerSelected("das") },
                enabled = answerFeedback == null
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ArticleButton(
    article: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .width(100.dp)
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = article,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FeedbackSection(feedback: AnswerFeedback) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (feedback.isCorrect) {
                Color(0xFF4CAF50).copy(alpha = 0.2f)
            } else {
                Color(0xFFF44336).copy(alpha = 0.2f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (feedback.isCorrect) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (feedback.isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (feedback.isCorrect) {
                    "Correct!"
                } else {
                    "Incorrect. The correct article is: ${feedback.correctArticle}"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (feedback.isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}
