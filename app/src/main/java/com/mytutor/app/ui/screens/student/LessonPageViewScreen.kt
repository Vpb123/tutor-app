package com.mytutor.app.ui.screens.student

import android.content.Intent
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mytutor.app.data.remote.models.Lesson
import com.mytutor.app.data.remote.models.LessonProgress
import com.mytutor.app.data.remote.repository.LessonRepository
import com.mytutor.app.data.remote.repository.ProgressRepository
import com.mytutor.app.ui.components.VideoPlayer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonPageViewScreen(
    lessonId: String,
    courseId: String,
    studentId: String,
    navController: NavController,
    lessonRepository: LessonRepository = LessonRepository(),
    progressRepository: ProgressRepository = ProgressRepository()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var lesson by remember { mutableStateOf<Lesson?>(null) }
    var currentPageIndex by remember { mutableStateOf(0) }
    val isCompleted = remember { mutableStateOf(false) }

    LaunchedEffect(lessonId, studentId) {
        val progressResult = progressRepository.getCompletedLessons(courseId, studentId)
        progressResult.onSuccess { completedList ->
            isCompleted.value = completedList.any { it.lessonId == lessonId }
        }
    }

    LaunchedEffect(lessonId) {
        val result = lessonRepository.getLessonById(lessonId)
        result.onSuccess {
            lesson = it
            println("Lesson Data: $it")
        }.onFailure {
            println("Failed to load lesson: ${it.message}")
        }
    }

    lesson?.let { lsn ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(lsn.title) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            bottomBar = {
                BottomAppBar(modifier = Modifier.padding(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentPageIndex > 0) {
                            Button(onClick = { currentPageIndex-- }) {
                                Text("Previous")
                            }
                        } else {
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text("Page ${currentPageIndex + 1} of ${lsn.pages.size}")

                        if (currentPageIndex < lsn.pages.lastIndex) {
                            Button(onClick = { currentPageIndex++ }) {
                                Text("Next")
                            }
                        } else {
                            if (!isCompleted.value) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val progress = LessonProgress(
                                                courseId = courseId,
                                                lessonId = lessonId,
                                                studentId = studentId,
                                                completedAt = System.currentTimeMillis()
                                            )
                                            progressRepository.markLessonCompleted(progress)
                                            isCompleted.value = true
                                            navController.popBackStack()
                                        }
                                    }
                                ) {
                                    Text("Mark as Completed")
                                }
                            } else {
                                Text(
                                    text = "Lesson Already Completed",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            val page = lsn.pages.getOrNull(currentPageIndex)
            val textBlock = page?.contentBlocks?.find {
                !it.text.isNullOrBlank()
            }

            val imageBlocks = page?.contentBlocks?.filter {
                !it.imageUrl.isNullOrBlank()
            } ?: emptyList()

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                val hasText = textBlock != null
                val hasImages = imageBlocks.isNotEmpty()
                val hasMaterials = page?.embeddedMaterials?.isNotEmpty() == true

                if (!hasText && !hasImages && !hasMaterials) {
                    Text("No content available for this page.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    if (imageBlocks.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = "Supporting Images",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Supporting Images",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }


                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            userScrollEnabled = false
                        ) {
                            items(imageBlocks) { block ->
                                block.imageUrl?.let { imageUrl ->
                                    Image(
                                        painter = rememberAsyncImagePainter(imageUrl),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                    )
                                }
                            }
                        }
                    }
                    if (hasText) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Article,
                                contentDescription = "Content",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Content",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = textBlock.text ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }


                    }
                }
                if (hasMaterials) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Extra Materials",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Extra Materials",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    page.embeddedMaterials.forEach { material ->
                        Spacer(modifier = Modifier.height(16.dp))
                        val materialType = material.type.toString().uppercase()
                        when (materialType) {
                            "IMAGE" -> Image(
                                painter = rememberAsyncImagePainter(material.content),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )

                            "PDF" -> Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent =
                                            Intent(Intent.ACTION_VIEW, material.content.toUri())
                                        context.startActivity(intent)
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "PDF Material",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Tap to open",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            "VIDEO" -> VideoPlayer(
                                videoUrl = material.content
                            )
                        }
                    }
                }
            }
        }
    }
}
