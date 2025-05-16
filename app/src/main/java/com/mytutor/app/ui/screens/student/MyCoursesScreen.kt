package com.mytutor.app.ui.screens.student


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mytutor.app.data.remote.FirebaseService.currentUserId
import com.mytutor.app.presentation.course.CourseViewModel

@Composable
fun MyCoursesScreen(
    navController: NavController,
    viewModel: CourseViewModel
) {

    val studentId = currentUserId
    val pendingEnrolments = viewModel.pendingEnrolments.collectAsState().value
    val acceptedEnrolments = viewModel.acceptedEnrolments.collectAsState().value
    val lessonCounts = viewModel.lessonCounts
    val courseProgress = viewModel.courseProgress
    val currentLesson = viewModel.currentLesson

    LaunchedEffect(Unit) {
        studentId?.let { viewModel.loadStudentEnrolmentsAndProgress(it) }
        println("Pending: ${viewModel.pendingEnrolments.value.size}")
        println("Accepted: ${viewModel.acceptedEnrolments.value.size}")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (pendingEnrolments.isEmpty() && acceptedEnrolments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("You haven't enrolled in any courses yet.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        if (pendingEnrolments.isNotEmpty()) {
            item {
                Text("Pending Courses", style = MaterialTheme.typography.titleLarge)
            }
            items(pendingEnrolments) { enrolment ->
                PendingCourseCard(
                    courseTitle = enrolment?.courseTitle ?: "Course",
                    description = enrolment?.description ?: "",
                    tutorName = enrolment?.tutorName?:"Tutor",
                    subject = enrolment?.subject ?: "Subject"
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
                    courseTitle = enrolment?.courseTitle ?: "Course",
                    description = enrolment?.description ?: "",
                    tutorName = enrolment?.tutorName?:"Tutor",
                    lessonCount = lessonCounts[courseId] ?: 0,
                    progress = courseProgress[courseId] ?: 0f,
                    onContinue = {
                        navController.navigate("courseDetail/$courseId")
                    } ,
                    showTakeQuiz = (courseProgress[courseId] ?: 0f) >= 100f,
                    onTakeQuiz = {
                            navController.navigate("takeQuiz/$courseId/$currentUserId")
                    }
                )
            }
        }
    }
}

@Composable
fun PendingCourseCard(
    courseTitle: String,
    tutorName: String,
    description: String,
    subject: String
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(courseTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("👤 $tutorName", style = MaterialTheme.typography.bodySmall)
                Text("📚 $subject", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                AssistChip(onClick = {}, label = { Text("Pending Approval") })
            }
        }
    }
}


@Composable
fun EnrolledCourseCard(
    courseTitle: String,
    description: String,
    tutorName: String,
    lessonCount: Int,
    progress: Float,
    onContinue: () -> Unit,
    showTakeQuiz: Boolean = false,
    onTakeQuiz: () -> Unit = {}
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(courseTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("👤 $tutorName", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar + lesson status
            LinearProgressIndicator(
                progress = progress / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Progress: ${"%.1f".format(progress)}%  |  ${((progress / 100f) * lessonCount).toInt()} / $lessonCount lessons",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
            ) {
                if (progress >= 100f) {
                    Button(
                        onClick = onContinue,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Completed", color = MaterialTheme.colorScheme.onTertiary)
                    }
                } else {
                    Button(
                        onClick = onContinue,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Continue"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (progress == 0f) "Start Course" else "Continue")
                    }
                }

                if (showTakeQuiz) {
                    Button(
                        onClick = onTakeQuiz,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Take Quiz")
                    }
                }
            }

        }
    }
}

