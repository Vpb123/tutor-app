package com.mytutor.app.ui.screens.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mytutor.app.data.remote.models.QuestionType
import com.mytutor.app.presentation.quiz.QuizViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentQuizScreen(
    courseId: String,
    studentId: String,
    navController: NavHostController,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val quiz by viewModel.quiz.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val result by viewModel.quizResult.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val answers = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(courseId) {
        viewModel.loadQuiz(courseId)
    }
    LaunchedEffect(viewModel.quiz.collectAsState().value?.id) {
        viewModel.quiz.value?.id?.let {
            viewModel.loadQuizQuestions(it)
        }
    }

    if (result != null) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Quiz Submitted",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text("Quiz Submitted!", style = MaterialTheme.typography.headlineSmall)
            }

            Spacer(Modifier.height(16.dp))
            Text("Your Score: ${result!!.score}/${quiz?.totalMarks}", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(32.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Back to Dashboard")
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(quiz?.title ?: "Quiz") })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                quiz?.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                }

                if (quiz != null && questions.isNotEmpty()) {
                    questions.forEachIndexed { index, question ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            // Question number and text
                            Text(
                                text = "Q${index + 1}. ${question.questionText}",
                                style = MaterialTheme.typography.titleMedium
                            )

                            // Optional image attachment row
                            if (!question.imageUrl.isNullOrEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Image attached", style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Answer options
                            when (question.questionType) {
                                QuestionType.MCQ -> {
                                    question.options.forEachIndexed { i, option ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { answers[question.id] = i.toString() }
                                                .padding(vertical = 4.dp),
                                            color = if (answers[question.id] == i.toString())
                                                MaterialTheme.colorScheme.primaryContainer
                                            else
                                                MaterialTheme.colorScheme.surfaceVariant,
                                            shape = MaterialTheme.shapes.medium
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = answers[question.id] == i.toString(),
                                                    onClick = null
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(option, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }
                                }

                                QuestionType.MSQ -> {
                                    val selectedIndices = answers[question.id]
                                        ?.split(",")
                                        ?.mapNotNull { it.toIntOrNull() }
                                        ?: emptyList()

                                    question.options.forEachIndexed { i, option ->
                                        val selected = selectedIndices.contains(i)
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    val updated = selectedIndices.toMutableSet()
                                                    if (selected) updated.remove(i) else updated.add(i)
                                                    answers[question.id] = updated.sorted().joinToString(",")
                                                }
                                                .padding(vertical = 4.dp),
                                            color = if (selected)
                                                MaterialTheme.colorScheme.primaryContainer
                                            else
                                                MaterialTheme.colorScheme.surfaceVariant,
                                            shape = MaterialTheme.shapes.medium
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = selected,
                                                    onCheckedChange = null
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(option, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }
                                }

                                QuestionType.FILL -> {
                                    OutlinedTextField(
                                        value = answers[question.id] ?: "",
                                        onValueChange = { answers[question.id] = it },
                                        label = { Text("Your Answer") },
                                        placeholder = { Text("Type your response...") },
                                        shape = MaterialTheme.shapes.medium,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                                        )
                                    )
                                }
                            }
                        }

                        Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                    }


                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.submitAnswers(quiz!!.id, studentId, answers)
                        },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Quiz")
                    }
                } else {
                    Text("Loading quiz...", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

