package com.mytutor.app.ui.screens.tutor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.mytutor.app.data.remote.models.ContentBlockData
import com.mytutor.app.data.remote.models.LearningMaterial
import com.mytutor.app.data.remote.models.LessonPage
import com.mytutor.app.presentation.lesson.LessonViewModel
import com.mytutor.app.ui.components.MaterialInputDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonPageBuilderScreen(
    pageIndex: Int,
    existingPage: LessonPage?,
    viewModel: LessonViewModel,
    onSave: (LessonPage, goToNext: Boolean) -> Unit,
    navController: NavHostController,
) {
    val context = LocalContext.current
    var showMaterialDialog by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf(TextFieldValue("")) }
    var showAddedIcon by remember { mutableStateOf(false) }
    var contentBlocks by remember { mutableStateOf<MutableList<ContentBlockData>>(mutableListOf()) }
    val isTextBlockAdded = contentBlocks.any { it.type == "text" }

    var embeddedMaterials by remember {
        mutableStateOf<MutableList<LearningMaterial>>(existingPage?.embeddedMaterials?.toMutableList() ?: mutableListOf())
    }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    LaunchedEffect(existingPage) {
        val existingBlocks = existingPage?.contentBlocks?.toMutableList() ?: mutableListOf()
        contentBlocks = existingBlocks
        textInput = TextFieldValue(existingBlocks.find { it.type == "text" }?.text ?: "")

        imageUris = existingBlocks
            .filter { it.type == "image" && !it.imageUrl.isNullOrBlank() }
            .mapNotNull { it.imageUrl?.toUri() }
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (true) {
            imageUris = uris
            uris.forEach { uri ->
                contentBlocks.add(ContentBlockData(type = "image", imageUrl = uri.toString()))
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editing Page ${pageIndex + 1}", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column(Modifier.navigationBarsPadding()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val page = LessonPage(
                                contentBlocks = contentBlocks.toList(),
                                embeddedMaterials = embeddedMaterials.toList()
                            )
                            onSave(page, false)

                        }
                    ) {
                        Text("Save & Go Back")
                    }
                    Button(
                        onClick = {
                            val page = LessonPage(
                                contentBlocks = contentBlocks.toList(),
                                embeddedMaterials = embeddedMaterials.toList()
                            )
                            onSave(page, true)
                        }
                    ) {
                        Text("Save & Add Another")
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Paragraph Input
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Add Paragraphs", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = {
                            textInput = it
                            if (!isTextBlockAdded) textInput = it
                            showAddedIcon = false
                        },
                        enabled = !isTextBlockAdded,
                        placeholder = { Text("Write multiple paragraphs here...\nUse Enter to separate them.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val newText = textInput.text.trim()
                        if (newText.isNotBlank()) {
                            val updatedBlocks = contentBlocks.toMutableList()
                            val existingIndex = updatedBlocks.indexOfFirst { it.type == "text" }
                            if (existingIndex >= 0) {
                                updatedBlocks[existingIndex] =
                                    ContentBlockData(type = "text", text = newText)
                            } else {
                                updatedBlocks.add(
                                    0,
                                    ContentBlockData(type = "text", text = newText)
                                )
                            }
                            contentBlocks = updatedBlocks
                            showAddedIcon = true
                        }
                    }) {
                        if (showAddedIcon) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Added")
                            Spacer(Modifier.width(4.dp))
                            Text("Paragraphs Added")
                        } else {
                            Text("Add Paragraphs")
                        }
                    }
                }
            }

            // Image Upload Section
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Add Images", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { imagePicker.launch("image/*") }) {
                        Icon(Icons.Default.Image, contentDescription = "Pick Images")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Images")
                    }
                    if (imageUris.isNotEmpty()) {
                        Text("Preview:", modifier = Modifier.padding(top = 8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            imageUris.forEach { uri ->
                                Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Embedded Materials Section
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "Attach Materials (PDF, MP3, MP4)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(onClick = { showMaterialDialog = true }) {
                        Text("Attach Material")
                    }
                    embeddedMaterials.forEach {
                        Text(
                            "📎 ${it.type.name} — ${it.caption ?: "No caption"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

        }

        if (showMaterialDialog) {
            MaterialInputDialog(
                context = context,
                uploadFile = { uri, callback -> viewModel.uploadFile(context, uri, callback) },
                onDismiss = { showMaterialDialog = false },
                onMaterialAdded = { material ->
                    embeddedMaterials.add(material)
                    showMaterialDialog = false
                }
            )
        }
    }
}


