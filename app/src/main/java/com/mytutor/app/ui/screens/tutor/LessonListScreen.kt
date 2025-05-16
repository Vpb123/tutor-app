package com.mytutor.app.ui.screens.tutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mytutor.app.data.remote.models.Course
import com.mytutor.app.presentation.course.CourseViewModel
import com.mytutor.app.presentation.lesson.LessonViewModel
import com.mytutor.app.presentation.quiz.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonListScreen(
    courseId: String,
    navController: NavHostController,
    viewModel: LessonViewModel = hiltViewModel(),
    courseViewModel: CourseViewModel = hiltViewModel(),
    quizViewModel: QuizViewModel = hiltViewModel()
) {
    val lessons by viewModel.lessons.collectAsState()
    val error by viewModel.error.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var course by remember { mutableStateOf<Course?>(null) }

    LaunchedEffect(courseId) {
        courseViewModel.getCourseById(courseId) {
            course = it
        }
        viewModel.loadLessons(courseId)
    }
    val quiz by quizViewModel.quiz.collectAsState()

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            quizViewModel.loadQuiz(courseId)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { course?.let { Text(it.title) } },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(course?.description ?: "", style = MaterialTheme.typography.bodyMedium)

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Lessons", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Quiz", modifier = Modifier.padding(16.dp))
                }
            }

            when (selectedTab) {
                0 -> {
                    if (lessons.isEmpty()) {
                        Text("No lessons added yet.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            lessons.forEach { lesson ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(lesson.title, style = MaterialTheme.typography.titleMedium)
                                            Text("${lesson.pages.size} page(s)", style = MaterialTheme.typography.bodySmall)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            IconButton(onClick = {
                                                navController.navigate("lessonEditor/$courseId/${lesson.id}")
                                            }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Lesson")
                                            }

                                            IconButton(onClick = {
                                                viewModel.deleteLesson(lesson.id, courseId)
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Lesson")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { navController.navigate("lessonEditor/$courseId") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Lesson")
                        Spacer(Modifier.width(8.dp))
                        Text("Add Lesson")
                    }
                }

                1 -> {
                if (quiz == null) {
                    Text("No quiz added for this course.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            navController.navigate("quizCreator/$courseId")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Quiz")
                        Spacer(Modifier.width(8.dp))
                        Text("Add Quiz")
                    }
                } else {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.elevatedCardColors()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                ListItem(
                                    headlineContent  = { Text(quiz?.title ?: "Untitled Quiz", style = MaterialTheme.typography.titleLarge) },
                                    supportingContent = { Text("Quiz already exists for this course.", style = MaterialTheme.typography.bodyMedium) }
                                )

                                Spacer(Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    ElevatedAssistChip(
                                        onClick = {},
                                        label = { Text("Total Marks: ${quiz?.totalMarks}") }
                                    )
                                    ElevatedAssistChip(
                                        onClick = {},
                                        label = { Text("Threshold: ${quiz?.passPercentage}%") }
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                OutlinedButton(
                                    onClick = {
                                        navController.navigate("quizBuilder/${quiz?.id}")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("View or Edit Quiz")
                                }
                            }

                            IconButton(
                                onClick = {
                                    quiz?.id?.let { id ->
                                        quizViewModel.deleteQuiz(id) {
                                            quizViewModel.loadQuiz(courseId)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Quiz",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                }

            }

            if (error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}


