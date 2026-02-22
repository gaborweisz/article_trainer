package com.trainig.articletrainer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Start screen where user selects language level and how many words to practice.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    maxWords: Int,
    currentLevel: String,
    onLevelChanged: (String) -> Unit,
    onStartQuiz: (Int, String) -> Unit
) {
    var wordCount by remember { mutableStateOf("20") }
    var expanded by remember { mutableStateOf(false) }

    val levelOptions = listOf(
        "A1 basic nouns (360)" to "A1",
        "A2 basic nouns (560)" to "A2",
        "Animals only (90)"    to "animals_a2"
    )

    var selectedDisplay by remember(currentLevel) {
        mutableStateOf(levelOptions.find { it.second == currentLevel }?.first ?: "A1 basic nouns (360)")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "German Articles Trainer",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Language Level Selector
        Text(
            text = "Select Dictionary",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.width(260.dp)
        ) {
            OutlinedTextField(
                value = selectedDisplay,
                onValueChange = {},
                readOnly = true,
                label = { Text("Dictionary") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                levelOptions.forEach { (display, key) ->
                    DropdownMenuItem(
                        text = { Text(display) },
                        onClick = {
                            selectedDisplay = display
                            expanded = false
                            onLevelChanged(key)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "How many words do you want to guess?",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = wordCount,
            onValueChange = { newValue ->
                // Only allow digits
                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                    wordCount = newValue
                }
            },
            label = { Text("Number of words") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(260.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Maximum: $maxWords words available",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val count = wordCount.toIntOrNull() ?: 20
                val validCount = count.coerceIn(1, maxWords)
                val selectedKey = levelOptions.find { it.first == selectedDisplay }?.second ?: "A1"
                onStartQuiz(validCount, selectedKey)
            },
            modifier = Modifier
                .width(200.dp)
                .height(56.dp)
        ) {
            Text(
                text = "Start",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
