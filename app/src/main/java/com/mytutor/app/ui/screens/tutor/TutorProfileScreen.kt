package com.mytutor.app.ui.screens.tutor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.mytutor.app.presentation.user.UserViewModel
import com.mytutor.app.ui.theme.Typography
import com.mytutor.app.presentation.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorProfileScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    viewModel: UserViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),

) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCurrentUserProfile()
    }

    val specializations = listOf("Math", "Science", "English", "Computer Science", "History", "Art")

    var isEditing by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember(user) { mutableStateOf(TextFieldValue(user?.displayName ?: "")) }
    var specialization by remember(user) { mutableStateOf(user?.specialization ?: specializations.first()) }
    var experience by remember(user) { mutableStateOf(TextFieldValue(user?.experienceYears?.toString() ?: "")) }
    var bio by remember(user) { mutableStateOf(TextFieldValue(user?.bio ?: "")) }
    var expanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val imagePainter = rememberAsyncImagePainter(
        model = selectedImageUri ?: user?.profileImageUrl
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(contentAlignment = Alignment.TopEnd) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable(enabled = isEditing) {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null || !user?.profileImageUrl.isNullOrBlank()) {
                        Image(
                            painter = imagePainter,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = user?.displayName?.firstOrNull()?.uppercaseChar()?.toString() ?: "T",
                            style = Typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (isEditing) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Image",
                        modifier = Modifier
                            .offset(x = (-8).dp, y = 8.dp)
                            .background(MaterialTheme.colorScheme.background, CircleShape)
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                Text("Edit Profile", style = Typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        readOnly = true,
                        value = specialization,
                        onValueChange = {},
                        label = { Text("Specialization") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .clickable { expanded = true }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        specializations.forEach { spec ->
                            DropdownMenuItem(
                                text = { Text(spec) },
                                onClick = {
                                    specialization = spec
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = experience,
                    onValueChange = { experience = it },
                    label = { Text("Experience (years)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = { isEditing = false }) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        val updatedUser = user?.copy(
                            displayName = name.text,
                            specialization = specialization,
                            experienceYears = experience.text.toIntOrNull() ?: 0,
                            bio = bio.text
                        )

                        updatedUser?.let {
                            viewModel.updateUserProfile(it)
                            selectedImageUri?.let { uri ->
                                viewModel.uploadAndSetProfileImage(uri, context)
                            }
                        }

                        isEditing = false
                    }) {
                        Text("Save")
                    }
                }

            } else {
                Text("Tutor Profile", style = Typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user?.displayName ?: "",
                    style = Typography.headlineMedium
                )
                Text(
                    text = user?.email ?: "",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileDetailRow(label = "Specialization", value = user?.specialization ?: "-")
                        ProfileDetailRow(label = "Experience", value = "${user?.experienceYears ?: 0} years")
                        ProfileDetailRow(label = "Bio", value = user?.bio ?: "-")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = { isEditing = true }) {
                    Text("Edit Profile")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        authViewModel.logout()
                        onLogout()
                              },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Logout", color = MaterialTheme.colorScheme.onError)
                }

            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Column {
        Text(text = label, style = Typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = Typography.bodyLarge)
    }
}

