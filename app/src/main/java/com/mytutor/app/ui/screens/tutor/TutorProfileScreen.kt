package com.mytutor.app.ui.screens.tutor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.mytutor.app.presentation.auth.AuthViewModel
import com.mytutor.app.presentation.user.UserViewModel
import androidx.compose.material.icons.filled.Close

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

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profileImageUri = it
            viewModel.uploadProfileImage(it)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCurrentUserProfile()
    }

    LaunchedEffect(user) {
        user?.let {
            name = it.displayName
            email = it.email ?: ""
            specialization = it.specialization ?: ""
            experience = it.experienceYears?.toString() ?: ""
            bio = it.bio ?: ""
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (user != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text("My Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = profileImageUri ?: user!!.profileImageUrl ?: "https://via.placeholder.com/150"
                                ),
                                contentDescription = "Profile Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .matchParentSize()
                                    .border(1.5.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
                            )

                            if (isEditing) {
                                IconButton(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(28.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        .border(1.dp, Color.White, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Change Profile Picture",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = user!!.displayName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = user!!.email ?: "—",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (isEditing) {
                            EditableRow("Name", name, isEditing) { name = it }
                            EditableRow("Email", email, isEditing) { email = it }
                        }
                        EditableRow("Specialization", specialization, isEditing) { specialization = it }
                        EditableRow("Experience (years)", experience, isEditing) { experience = it }
                        EditableRow("Bio", bio, isEditing, isMultiline = true) { bio = it }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (isEditing) {
                    // Cancel Button
                    FloatingActionButton(
                        onClick = {
                            // Revert fields to original user data
                            user?.let {
                                name = it.displayName
                                email = it.email ?: ""
                                specialization = it.specialization ?: ""
                                experience = it.experienceYears?.toString() ?: ""
                                bio = it.bio ?: ""
                            }
                            isEditing = false
                        },
                        containerColor = Color.Gray,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close, // Or use a cancel icon if available
                            contentDescription = "Cancel Edit"
                        )
                    }
                }

                // Save/Edit Button
                FloatingActionButton(
                    onClick = {
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
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }

                // Logout Button
                FloatingActionButton(
                    onClick = {
                        authViewModel.logout()
                        onLogout()
                    },
                    containerColor = Color.Red,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout"
                    )
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
                    .defaultMinSize(minHeight = if (isMultiline) 100.dp else 56.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    ),
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