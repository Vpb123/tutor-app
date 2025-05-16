package com.mytutor.app.ui.screens.student

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.mytutor.app.presentation.student.StudentProfileViewModel
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.Color


@Composable
fun StudentProfileScreen(
    viewModel: StudentProfileViewModel = hiltViewModel()
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val user by viewModel.user.collectAsState()
    val error by viewModel.error.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    var bio by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    LaunchedEffect(uid) {
        uid?.let { viewModel.loadUser(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${error ?: "Unknown error"}", color = MaterialTheme.colorScheme.error)
                }
            }

            user != null -> {
                val u = user!!

                LaunchedEffect(u) {
                    bio = u.bio
                    phone = u.phoneNumber ?: ""
                    address = u.address ?: ""
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text("My Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Profile Image + Details Header
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
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = u.profileImageUrl ?: "https://via.placeholder.com/150"
                                ),
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = u.displayName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
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
                            EditableRow("Phone", phone, isEditing) { phone = it }
                            EditableRow("Address", address, isEditing) { address = it }
                            EditableRow("Bio", bio, isEditing) { bio = it }
                            ReadOnlyRow("Email", u.email ?: "—")
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    if (isEditing) {
                        // Cancel button
                        FloatingActionButton(
                            onClick = {
                                user?.let {
                                    bio = it.bio
                                    phone = it.phoneNumber ?: ""
                                    address = it.address ?: ""
                                }
                                isEditing = false
                            },
                            containerColor = Color.Gray,
                            contentColor = Color.White,
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Edit"
                            )
                        }
                    }

                    // Edit / Save button
                    FloatingActionButton(
                        onClick = {
                            if (isEditing) {
                                uid?.let { viewModel.updateEditableFields(it, bio, phone, address) }
                            }
                            isEditing = !isEditing
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Save" else "Edit"
                        )
                    }
                }
            }

            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun ReadOnlyRow(label: String, value: String?) {
    Log.d("LABEL AND VALUE", "${label}, ${value}")

    if (!value.isNullOrBlank()) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(text = value, fontSize = 14.sp)
            Divider(modifier = Modifier.padding(top = 6.dp))
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
            Text(text = value, fontSize = 14.sp)
        }
        Divider(modifier = Modifier.padding(top = 6.dp))
    }
}
