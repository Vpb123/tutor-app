package com.mytutor.app.ui.screens.tutor

import QuizResultsSection
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mytutor.app.presentation.course.CourseViewModel
import com.mytutor.app.ui.screens.tutor.components.StudentProgressBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseProgressScreen(
    courseId: String,
    courseViewModel: CourseViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val courseTitle = remember { mutableStateOf("") }
    val courseDescription = remember { mutableStateOf("") }
    val lessonCount = remember { mutableStateOf(0) }
    val enrolledCount = remember { mutableStateOf(0) }
    val studentProgressList = remember { mutableStateListOf<Pair<String, Float>>() }
    val isLoading by courseViewModel.loading.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Lesson Progress", "Quiz Results")
    val isLessonProgressLoading = remember { mutableStateOf(true) }

    LaunchedEffect(courseId) {
        isLessonProgressLoading.value = true
        courseViewModel.loadCourseProgress(courseId) { title, description, lessons, students, progressList ->
            courseTitle.value = title
            courseDescription.value = description
            lessonCount.value = lessons
            enrolledCount.value = students

            studentProgressList.clear()
            studentProgressList += progressList.map { it.studentName to it.progressPercent }

            isLessonProgressLoading.value = false
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Course Progress") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = courseTitle.value,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = courseDescription.value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            InfoChip(Icons.AutoMirrored.Filled.MenuBook, "${lessonCount.value} Lessons")
                            InfoChip(Icons.Default.Person, "${enrolledCount.value} Students")
                        }
                    }

                    TabRow(selectedTabIndex = selectedTabIndex) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title) }
                            )
                        }
                    }

                    when (selectedTabIndex) {
                        0 -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                item {
                                    Text(
                                        text = "Student Progress",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }

                                if (isLessonProgressLoading.value) {
                                    item {
                                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                } else if (studentProgressList.isEmpty()) {
                                    item {
                                        Text("No enrolled students found.")
                                    }
                                } else {
                                    itemsIndexed(studentProgressList) { index, (name, percent) ->
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            StudentProgressBar(name = name, percent = percent)
                                            if (index < studentProgressList.lastIndex) {
                                                Divider(
                                                    modifier = Modifier.padding(vertical = 8.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    thickness = 1.dp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            QuizResultsSection(courseId)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun InfoChip(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}