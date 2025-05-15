package com.mytutor.app.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mytutor.app.data.remote.models.Course
import com.mytutor.app.data.remote.models.CourseSubject
import com.mytutor.app.presentation.course.CourseViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllCoursesScreen(
    navController: NavHostController,
    courseViewModel: CourseViewModel = hiltViewModel()
) {
    val allCourses by courseViewModel.allCourses.collectAsState()
    val loading by courseViewModel.loading.collectAsState()
    val error by courseViewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf<CourseSubject?>(null) }
    val subjectOptions = CourseSubject.values().toList()

    LaunchedEffect(Unit) {
        courseViewModel.loadAllCourses()
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp)
        ) {
            Column {
                Text("Explore Courses", style =  MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search by title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🎯 Subject Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedSubject == null,
                    onClick = { selectedSubject = null },
                    label = { Text("All") }
                )
            }
            items(subjectOptions.size) { index ->
                val subject = subjectOptions[index]
                FilterChip(
                    selected = selectedSubject == subject,
                    onClick = { selectedSubject = subject },
                    label = { Text(subject.name.replace("_", " ")) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val filteredCourses = allCourses.filter {
                (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true)) &&
                        (selectedSubject == null || it.subject == selectedSubject)
            }

            if (filteredCourses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No courses found", style =  MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(filteredCourses) { course ->
                        ModernCourseCard(
                            course = course,
                            onView = { navController.navigate("courseDetail/${course.id}") },
                            onEnroll = {
                                // TODO: Use actual studentId
                                courseViewModel.requestEnrolment("studentId", course.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun ColumnScope.item(function: () -> Unit) {}

@Composable
fun ModernCourseCard(
    course: Course,
    onView: () -> Unit,
    onEnroll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = course.title,
                style =  MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("👤 Tutor: ${course.tutorId.take(8)}...", style =  MaterialTheme.typography.bodySmall) // Replace with actual name if mapped
                Text("📚 Subject: ${course.subject.name.replace("_", " ")}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📘 Lessons: ${course.lessonCount}", style =  MaterialTheme.typography.bodySmall)
                Text("⏱ Duration: ${course.durationInHours}h", style =  MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onView) {
                    Text("View Details")
                }
                Button(onClick = onEnroll) {
                    Text("Enroll")
                }
            }
        }
    }
}

