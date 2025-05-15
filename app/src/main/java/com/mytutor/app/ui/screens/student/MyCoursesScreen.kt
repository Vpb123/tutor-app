package com.mytutor.app.ui.screens.student


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mytutor.app.presentation.course.CourseViewModel
import com.mytutor.app.presentation.user.UserViewModel
import androidx.compose.runtime.*

@Composable
fun MyCoursesScreen(
    navController: NavController,
    viewModel: CourseViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),

) {
    val user by userViewModel.user.collectAsState()
    val studentId = user?.uid ?: return
    val pendingEnrolments = viewModel.pendingEnrolments.collectAsState().value
    val acceptedEnrolments = viewModel.acceptedEnrolments.collectAsState().value
    val tutorNames = viewModel.tutorNames
    val lessonCounts = viewModel.lessonCounts
    val courseProgress = viewModel.courseProgress
    val currentLesson = viewModel.currentLesson

    LaunchedEffect(Unit) {
        viewModel.loadStudentEnrolmentsAndProgress(studentId)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (pendingEnrolments.isNotEmpty()) {
            item {
                Text("Pending Courses", style = MaterialTheme.typography.titleLarge)
            }
            items(pendingEnrolments) { enrolment ->
                PendingCourseCard(
                    courseId = enrolment.courseId,
                    tutorName = tutorNames[enrolment.courseId] ?: "Tutor",
                    subject = "Subject Info" // replace if available
                )
            }
        }

        if (acceptedEnrolments.isNotEmpty()) {
            item {
                Text("My Courses", style = MaterialTheme.typography.titleLarge)
            }
            items(acceptedEnrolments) { enrolment ->
                val courseId = enrolment.courseId
                EnrolledCourseCard(
                    courseId = courseId,
                    tutorName = tutorNames[courseId] ?: "Tutor",
                    lessonCount = lessonCounts[courseId] ?: 0,
                    progress = courseProgress[courseId] ?: 0f,
                    onContinue = {
                        val lesson = currentLesson[courseId]
                        lesson?.let {
                            navController.navigate("lessonPlayer/${courseId}/${lesson.id}")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PendingCourseCard(courseId: String, tutorName: String, subject: String) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$courseId", style = MaterialTheme.typography.titleMedium)
            Text("👤 $tutorName", style = MaterialTheme.typography.bodySmall)
            Text("📚 $subject", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            AssistChip(onClick = {}, label = { Text("Pending Approval") })
        }
    }
}

@Composable
fun EnrolledCourseCard(
    courseId: String,
    tutorName: String,
    lessonCount: Int,
    progress: Float,
    onContinue: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(courseId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("👤 $tutorName", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Progress: ${"%.1f".format(progress)}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (progress == 0f) "Start Course" else "Continue")
            }
        }
    }
}
