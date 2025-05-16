package com.mytutor.app.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mytutor.app.data.remote.models.LearningMaterial
import com.mytutor.app.data.remote.models.MaterialType
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun MaterialInputDialog(
    context: Context,
    uploadFile: (Uri, (Result<String>) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onMaterialAdded: (LearningMaterial) -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedType by remember { mutableStateOf(MaterialType.TEXT) }
    var content by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var uploading by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploading = true
            uploadFile(it) { result ->
                result.fold(
                    onSuccess = { content = it },
                    onFailure = { /* show error */ }
                )
                uploading = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Add Material", style = MaterialTheme.typography.headlineSmall)

            // Material Type Dropdown
            ExposedDropdownMenuBox(
                expanded = typeMenuExpanded,
                onExpandedChange = { typeMenuExpanded = !typeMenuExpanded }
            ) {
                OutlinedTextField(
                    value = selectedType.name.replace("_", " ").capitalize(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Material Type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .clickable { typeMenuExpanded = true }
                )
                ExposedDropdownMenu(
                    expanded = typeMenuExpanded,
                    onDismissRequest = { typeMenuExpanded = false }
                ) {
                    MaterialType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name.replace("_", " ").capitalize()) },
                            onClick = {
                                selectedType = type
                                content = ""
                                typeMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Input or File Selector
            if (selectedType == MaterialType.TEXT) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Text Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                )
            } else {
                Button(
                    onClick = { launcher.launch(getMimeTypeFilter(selectedType)) },
                    enabled = !uploading
                ) {
                    Text(if (content.isNotBlank()) "Change File" else "Select File")
                }

                if (uploading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                if (content.isNotBlank()) {
                    Text("✅ File uploaded", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Caption Field
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("Caption (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val material = LearningMaterial(
                            id = UUID.randomUUID().toString(),
                            type = selectedType,
                            content = content,
                            caption = caption.ifBlank { null }
                        )
                        onMaterialAdded(material)
                        onDismiss()
                    },
                    enabled = content.isNotBlank() && !uploading
                ) {
                    Text("Add")
                }
            }
        }
    }
}


fun getMimeTypeFilter(type: MaterialType): String {
    return when (type) {
        MaterialType.IMAGE -> "image/*"
        MaterialType.VIDEO -> "video/*"
        MaterialType.AUDIO -> "audio/*"
        MaterialType.PDF -> "application/pdf"
        else -> "*/*"
    }
}