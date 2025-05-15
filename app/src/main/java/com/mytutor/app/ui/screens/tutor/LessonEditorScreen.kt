package com.mytutor.app.ui.screens.tutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mytutor.app.presentation.lesson.LessonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonEditorScreen(
    courseId: String,
    navController: NavHostController,
    lessonId: String? = null,
    viewModel: LessonViewModel
) {
    val context = LocalContext.current
    val existingLesson by viewModel.selectedLesson.collectAsState()
    var title by remember { mutableStateOf(TextFieldValue("")) }
    val pages = existingLesson?.pages ?: emptyList()
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(lessonId) {
        if (lessonId != null) viewModel.selectLesson(lessonId)
    }

    LaunchedEffect(existingLesson?.id) {
        existingLesson?.let {
            title = TextFieldValue(it.title)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (lessonId == null) "Create Lesson" else "Edit Lesson") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        val newLesson = viewModel.selectedLesson.value?.copy(
                            title = title.text.trim(),
                            courseId = courseId
                        ) ?: return@Button

                        viewModel.updateLesson(newLesson) {
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text("Save & Go Back")
                }

                Button(
                    onClick = {
                        val newLesson = viewModel.selectedLesson.value?.copy(
                            title = title.text.trim(),
                            courseId = courseId
                        ) ?: return@Button

                        viewModel.updateLesson(newLesson) {
                            navController.navigate("lessonEditor/$courseId")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text("Save & Add Another")
                }
            }
        }

    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Lesson Title") },
                modifier = Modifier.fillMaxWidth()
            )

            pages.forEachIndexed { index, page ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Page ${index + 1} - ${page.contentBlocks.size} blocks, ${page.embeddedMaterials.size} attachments", style = MaterialTheme.typography.titleSmall)

                        Button(onClick = {
                            navController.navigate("editPageBuilder/$index")
                        }) {
                            Text("Edit Page")
                        }
                    }
                }
            }

            Button(onClick = {
                if (title.text.isBlank()) {
                    error = "Enter lesson title first"
                    return@Button
                }

                val existing = viewModel.selectedLesson.value
                if (existing == null) {
                    viewModel.createEmptyLesson(title.text, courseId) { created ->
                        navController.navigate("editPageBuilder/0")
                    }
                } else {
                    val nextIndex = existing.pages.size
                    navController.navigate("editPageBuilder/$nextIndex")
                }
            }) {
                Text("Add New Page")
            }
            if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }
}

