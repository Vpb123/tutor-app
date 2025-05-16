package com.mytutor.app.ui.screens.tutor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mytutor.app.data.remote.models.Quiz
import com.mytutor.app.presentation.quiz.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizCreatorScreen(
    courseId: String,
    navController: NavHostController,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    val focusManager = LocalFocusManager.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var totalMarks by remember { mutableStateOf("") }
    var threshold by remember { mutableStateOf("50") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Quiz") },
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
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Quiz Info", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Quiz Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = totalMarks,
                onValueChange = { if (it.all { c -> c.isDigit() }) totalMarks = it },
                label = { Text("Total Marks") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = threshold,
                onValueChange = { if (it.all { c -> c.isDigit() }) threshold = it },
                label = { Text("Pass Threshold (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (title.isNotBlank() && totalMarks.isNotBlank()) {
                        viewModel.createQuiz(
                            Quiz(
                                courseId = courseId,
                                title = title.trim(),
                                description = description.trim(),
                                totalMarks = totalMarks.toInt(),
                                passPercentage = threshold.toInt()
                            )
                        ) { quizId ->
                            navController.navigate("quizBuilder/$quizId")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create Quiz")
                }
            }

            if (!error.isNullOrEmpty()) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
