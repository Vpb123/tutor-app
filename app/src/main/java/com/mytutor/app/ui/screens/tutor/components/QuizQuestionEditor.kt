package com.mytutor.app.ui.screens.tutor.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mytutor.app.data.remote.models.QuestionType
import com.mytutor.app.data.remote.models.QuizQuestion
import com.mytutor.app.presentation.quiz.QuizViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizQuestionEditorScreen(
    quizId: String,
    navController: NavHostController,
    viewModel: QuizViewModel,
    existingQuestion: QuizQuestion? = null,
) {
    var questionText by remember { mutableStateOf(existingQuestion?.questionText ?: "") }
    var imageUrl by remember { mutableStateOf(existingQuestion?.imageUrl ?: "") }
    var questionType by remember { mutableStateOf(existingQuestion?.questionType ?: QuestionType.MCQ) }
    var options by remember { mutableStateOf(existingQuestion?.options ?: List(4) { "" }) }
    var correctIndex by remember { mutableStateOf(existingQuestion?.correctAnswerIndex ?: 0) }
    var correctIndices by remember { mutableStateOf(existingQuestion?.correctAnswerIndices ?: emptyList()) }
    var correctText by remember { mutableStateOf(existingQuestion?.correctAnswerText ?: "") }
    var marks by remember { mutableStateOf(existingQuestion?.marks?.toString() ?: "1") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Quiz Question") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val id = existingQuestion?.id ?: UUID.randomUUID().toString()
                            val finalQuestion = QuizQuestion(
                                id = id,
                                quizId = quizId,
                                questionText = questionText.trim(),
                                imageUrl = imageUrl.takeIf { it.isNotBlank() },
                                options = options.filter { it.isNotBlank() },
                                questionType = questionType,
                                correctAnswerIndex = if (questionType == QuestionType.MCQ) correctIndex else null,
                                correctAnswerIndices = if (questionType == QuestionType.MSQ) correctIndices else null,
                                correctAnswerText = if (questionType == QuestionType.FILL) correctText.trim() else null,
                                marks = marks.toIntOrNull() ?: 1
                            )
                            viewModel.addQuestionToQuiz(finalQuestion) {
                                navController.popBackStack()
                            }
                        },
                        enabled = questionText.isNotBlank() && (marks.toIntOrNull() ?: 0) > 0
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = questionText,
                onValueChange = { questionText = it },
                label = { Text("Question Text") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            QuestionTypeDropdown(
                selected = questionType.name,
                options = QuestionType.values().map { it.name },
                onOptionSelected = { questionType = QuestionType.valueOf(it) }
            )

            if (questionType == QuestionType.MCQ || questionType == QuestionType.MSQ) {
                Text("Options", style = MaterialTheme.typography.bodyMedium)
                options.forEachIndexed { index, option ->
                    OutlinedTextField(
                        value = option,
                        onValueChange = {
                            options = options.toMutableList().apply { this[index] = it }
                        },
                        label = { Text("Option ${index + 1}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (questionType == QuestionType.MCQ) {
                    QuestionTypeDropdown(
                    label = "Correct Index",
                    selected = correctIndex.toString(),
                    options = options.indices.map { it.toString() },
                    onOptionSelected = { correctIndex = it.toInt() }
                )
                } else {
                    Text("Select Correct Options", style = MaterialTheme.typography.bodySmall)
                    options.forEachIndexed { i, opt ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = i in correctIndices,
                                onCheckedChange = { checked ->
                                    correctIndices = if (checked) {
                                        correctIndices + i
                                    } else {
                                        correctIndices - i
                                    }
                                }
                            )
                            Text(opt)
                        }
                    }
                }
            }

            if (questionType == QuestionType.FILL) {
                OutlinedTextField(
                    value = correctText,
                    onValueChange = { correctText = it },
                    label = { Text("Correct Answer") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = marks,
                onValueChange = { if (it.all { c -> c.isDigit() }) marks = it },
                label = { Text("Marks") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(100.dp)) // Padding at bottom for safe area
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionTypeDropdown(
    selected: String,
    options: List<String>,
    label: String = "Question Type",
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onOptionSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

