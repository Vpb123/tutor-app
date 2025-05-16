package com.mytutor.app.ui.screens.tutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mytutor.app.data.remote.models.QuizQuestion
import com.mytutor.app.presentation.quiz.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuizBuilderScreen(
    quizId: String,
    navController: NavHostController,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val quiz by viewModel.quiz.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showEditor by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }

    LaunchedEffect(quizId) {
        viewModel.loadQuizById(quizId)
        viewModel.loadQuizQuestions(quizId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Build Quiz") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            quiz?.let {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.wrapContentHeight()
                        .fillMaxWidth()
                ) {
                    Text(
                        text = quiz?.title ?: "Untitled Quiz",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    if (!quiz?.description.isNullOrBlank()) {
                        Text(
                            text = quiz!!.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Total Marks: ${quiz?.totalMarks}") }
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text("Threshold: ${quiz?.passPercentage}%") }
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text("Questions: ${questions.size}") }
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text("Assigned: ${questions.sumOf { it.marks }}") }
                        )
                    }
                }


                Spacer(Modifier.height(24.dp))

                Text("Questions", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                if (questions.isEmpty()) {
                    Text("No questions added yet.", style = MaterialTheme.typography.bodySmall)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(questions) { question ->
                            QuestionItem(
                                question = question,
                                onDelete = { viewModel.removeQuestion(question.id) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        navController.navigate("questionEditor/$quizId")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Question")
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (viewModel.isTotalMarksValid()) {
                            navController.popBackStack()
                        } else {
                            showValidationError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                ) {
                    Text("Finish Quiz")
                }

                if (showValidationError) {
                    Text(
                        "Assigned marks do not match total quiz marks.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (!error.isNullOrEmpty()) {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } ?: run {
                if (!loading) {
                    Text("Quiz not found.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

    }
}

@Composable
fun QuestionItem(
    question: QuizQuestion,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    question.questionText,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!question.imageUrl.isNullOrBlank()) {
                        Text("📎 Image Attached", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Type: ${question.questionType.name}", style = MaterialTheme.typography.bodySmall)
                    Text("Marks: ${question.marks}", style = MaterialTheme.typography.bodySmall)
                }
            },
            trailingContent = {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Question",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            tonalElevation = 0.dp
        )

        Divider(thickness = 0.6.dp)
    }
}

