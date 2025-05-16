package com.mytutor.app.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mytutor.app.data.remote.FirebaseService.currentUserId
import com.mytutor.app.data.remote.models.Course
import com.mytutor.app.data.remote.models.CourseCompletionStatus
import com.mytutor.app.data.remote.models.Lesson
import com.mytutor.app.data.remote.models.LessonProgress
import com.mytutor.app.data.remote.models.LessonStatus
import com.mytutor.app.domain.usecase.ComputeLessonStatusUseCase
import com.mytutor.app.presentation.course.CourseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseViewScreen(
    courseId: String,
    navController: NavController,
    courseViewModel: CourseViewModel,
    onBack: () -> Unit = { navController.popBackStack() }
) {
    val courseState = remember { mutableStateOf<Course?>(null) }
    val lessonsState = remember { mutableStateOf<List<Lesson>>(emptyList()) }
    val progressState = remember { mutableStateOf<List<LessonProgress>>(emptyList()) }
    val courseStatus = remember { mutableStateOf<CourseCompletionStatus?>(null) }
    val studentId = currentUserId ?: ""
    val computeLessonStatus = remember { ComputeLessonStatusUseCase() }

    LaunchedEffect(courseId, studentId) {
        courseViewModel.getCourseById(courseId) { course ->
            courseState.value = course
        }

        courseViewModel.loadLessonsAndProgress(courseId, studentId) { lessons, progress ->
            lessonsState.value = lessons
            progressState.value = progress
            courseViewModel.getCourseCompletionStatus(
                courseId, lessons, progress, "", studentId
            ) { status ->
                courseStatus.value = status
            }
        }
    }

    val course = courseState.value
    val lessons = lessonsState.value
    val progress = progressState.value
    val lessonStatusMap = computeLessonStatus(lessons, progress)

    course?.let {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Course Details", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()

                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = it.title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "by ${courseViewModel.tutorNames[it.tutorId] ?: "Tutor"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (courseStatus.value == CourseCompletionStatus.COMPLETED) {
                            AssistChip(
                                onClick = { /* navigate to quiz */ },
                                label = { Text("Take Quiz") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = it.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    courseStatus.value?.let { status ->
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = status.name.replace("_", " ").lowercase()
                                        .replaceFirstChar { it.uppercase() }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Lessons", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(8.dp))

                lessons.forEach { lesson ->
                    val status = lessonStatusMap[lesson.id] ?: LessonStatus.LOCKED
                    val isLocked = status == LessonStatus.LOCKED

                    val icon = when (status) {
                        LessonStatus.COMPLETED -> Icons.Default.CheckCircle
                        LessonStatus.AVAILABLE -> Icons.Default.LockOpen
                        LessonStatus.LOCKED -> Icons.Default.Lock
                    }

                    val iconTint = when (status) {
                        LessonStatus.COMPLETED -> Color(0xFF4CAF50) // Green
                        LessonStatus.AVAILABLE -> Color(0xFF2196F3) // Blue
                        LessonStatus.LOCKED -> Color(0xFFBDBDBD)    // Gray
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 20.dp)
                                .clickable(enabled = !isLocked) {
                                    navController.navigate("lessonView/$courseId/${lesson.id}")
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lesson.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${lesson.pages.size} pages",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(22.dp)
                                )
                                if (!isLocked) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "View",
                                        color = iconTint,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    } ?: run {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}



fun Modifier.drawBottomBorder(color: Color = Color.LightGray): Modifier = this.then(
    Modifier.drawBehind {
        val strokeWidth = 1.dp.toPx()
        drawLine(
            color = color,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = strokeWidth
        )
    }
)


