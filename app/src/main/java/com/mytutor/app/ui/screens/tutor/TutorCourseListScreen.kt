package com.mytutor.app.ui.screens.tutor
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mytutor.app.presentation.course.CourseViewModel
import com.mytutor.app.presentation.user.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorCourseListScreen(
    navController: NavHostController,
    viewModel: CourseViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val user by userViewModel.user.collectAsState()
    val courses by viewModel.myCourses.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadCurrentUserProfile()
    }

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Load tutor's courses
    LaunchedEffect(user!!.uid) {
        viewModel.loadCoursesByTutor(user!!.uid)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Courses") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }

            if (courses.isEmpty() && !loading) {
                Text("No courses found.", style = MaterialTheme.typography.bodyMedium)
            }

            courses.forEach { course ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(course.title, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(course.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Students")
                            Text("Students: 0")

                            Icon(Icons.Default.MenuBook, contentDescription = "Lessons")
                            Text("Lessons: ${course.lessonCount}")

                            Icon(Icons.Default.Schedule, contentDescription = "Duration")
                            Text("${course.durationInHours} hrs")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = {
                                navController.navigate("createCourse/${course.id}")
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Edit")
                            }

                            OutlinedButton(
                                onClick = {
                                    user?.uid?.let { tutorId ->
                                        viewModel.deleteCourse(course.id, tutorId)
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Delete")
                            }

                            val isPublished = course.title.contains("Live")
                            Button(
                                onClick = {
                                    val updated = course.copy(
                                        title = if (isPublished) course.title.replace("Live", "") else "${course.title} Live"
                                    )
                                    viewModel.updateCourse(updated) {}
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPublished) Color.Gray else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = if (isPublished) Icons.Default.VisibilityOff else Icons.Default.Upload,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(if (isPublished) "Unpublish" else "Publish")
                            }
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
