package com.mytutor.app.ui.screens.tutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mytutor.app.data.remote.models.Course
import com.mytutor.app.data.remote.models.CourseSubject
import com.mytutor.app.presentation.course.CourseViewModel
import com.mytutor.app.presentation.user.UserViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrEditCourseScreen(
    navController: NavHostController,
    existingCourse: Course? = null,
    viewModel: CourseViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val user by userViewModel.user.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadCurrentUserProfile()
    }

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val tutorId = user!!.uid
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var title by remember(existingCourse) { mutableStateOf(TextFieldValue(existingCourse?.title ?: "")) }
    var description by remember(existingCourse) { mutableStateOf(TextFieldValue(existingCourse?.description ?: "")) }
    var duration by remember(existingCourse) { mutableStateOf(TextFieldValue(existingCourse?.durationInHours?.toString() ?: "")) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var selectedSubject by remember(existingCourse) { mutableStateOf(existingCourse?.subject ?: CourseSubject.COMPUTER_SCIENCE) }

    val subjectOptions = CourseSubject.entries.toTypedArray()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingCourse == null) "Create Course" else "Edit Course") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(24.dp)
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Course Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Course Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                )

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (in hours)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )

                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = !subjectExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedSubject.name.replace("_", " ").capitalize(Locale.ROOT),
                        onValueChange = {},
                        label = { Text("Subject") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        subjectOptions.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject.name.replace("_", " ").capitalize()) },
                                onClick = {
                                    selectedSubject = subject
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }

                if (error != null) {
                    Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = {
                        val valid = title.text.isNotBlank() && description.text.isNotBlank()
                        val durationInt = duration.text.toIntOrNull()

                        if (!valid || durationInt == null) return@Button

                        val course = Course(
                            id = existingCourse?.id ?: "",
                            title = title.text.trim(),
                            description = description.text.trim(),
                            subject = selectedSubject,
                            tutorId = tutorId,
                            durationInHours = durationInt
                        )

                        if (existingCourse == null) {
                            viewModel.createCourse(course) { courseId ->
                                println("Available destinations: ${navController.graph}")
                                navController.navigate("lessonEditor/$courseId/null")
                            }
                        } else {
                            viewModel.updateCourse(course) {
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                ) {
                    Text(if (existingCourse == null) "Create Course" else "Update Course")
                }
                if (existingCourse != null) {
                    Button(
                        onClick = {
                            navController.navigate("lessonList/${existingCourse.id}")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit Content")
                    }
                }

                if (loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

            }
        }
    }
}
