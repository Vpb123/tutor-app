package com.mytutor.app.ui.screens.tutor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.mytutor.app.presentation.auth.AuthViewModel
import com.mytutor.app.presentation.user.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorProfileScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    viewModel: UserViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    val specializations = listOf("Math", "Science", "English", "Computer Science", "History", "Art")
    var specializationExpanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profileImageUri = it
            viewModel.uploadAndSetProfileImage(it, context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCurrentUserProfile()
    }

    LaunchedEffect(user) {
        user?.let {
            name = it.displayName
            email = it.email
            specialization = it.specialization ?: specializations.first()
            experience = it.experienceYears?.toString() ?: ""
            bio = it.bio
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("My Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(90.dp)) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = profileImageUri ?: user?.profileImageUrl
                    ),
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )

                if (isEditing) {
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(1.dp, Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text(email, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Divider()

        EditableRow("Name", name, isEditing) { name = it }
        EditableRow("Email", email, isEditing) { email = it }

        if (isEditing) {
            Text("Specialization", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            ExposedDropdownMenuBox(expanded = specializationExpanded, onExpandedChange = { specializationExpanded = !specializationExpanded }) {
                OutlinedTextField(
                    value = specialization,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(specializationExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(expanded = specializationExpanded, onDismissRequest = { specializationExpanded = false }) {
                    specializations.forEach { spec ->
                        DropdownMenuItem(
                            text = { Text(spec) },
                            onClick = {
                                specialization = spec
                                specializationExpanded = false
                            }
                        )
                    }
                }
            }
        } else {
            EditableRow("Specialization", specialization, false) {}
        }

        EditableRow("Experience (years)", experience, isEditing) { experience = it }
        EditableRow("Bio", bio, isEditing, isMultiline = true) { bio = it }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isEditing) {
                TextButton(onClick = {
                    user?.let {
                        name = it.displayName
                        email = it.email
                        specialization = it.specialization ?: specializations.first()
                        experience = it.experienceYears?.toString() ?: ""
                        bio = it.bio
                    }
                    isEditing = false
                }) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Cancel")
                }
            }

            Button(onClick = {
                if (isEditing) {
                    val updatedUser = user!!.copy(
                        displayName = name,
                        email = email,
                        specialization = specialization,
                        experienceYears = experience.toIntOrNull() ?: 0,
                        bio = bio
                    )
                    viewModel.updateUserProfile(updatedUser)
                }
                isEditing = !isEditing
            }) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = "Toggle Edit"
                )
                Spacer(Modifier.width(4.dp))
                Text(if (isEditing) "Save" else "Edit")
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = {
                    authViewModel.logout()
                    onLogout()
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Logout")
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun EditableRow(
    label: String,
    value: String,
    isEditing: Boolean,
    isMultiline: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = if (isMultiline) 100.dp else 56.dp),
                singleLine = !isMultiline,
                maxLines = if (isMultiline) 4 else 1,
                shape = RoundedCornerShape(6.dp)
            )
        } else {
            Text(text = value.ifBlank { "—" }, fontSize = 14.sp)
        }
        Divider(modifier = Modifier.padding(top = 6.dp))
    }
}
