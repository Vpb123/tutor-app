package com.mytutor.app.ui.screens.auth.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.mytutor.app.data.remote.models.UserRole
import com.mytutor.app.presentation.auth.AuthState
import com.mytutor.app.presentation.auth.AuthViewModel
import com.mytutor.app.ui.components.AuthTextField
import com.mytutor.app.ui.components.PrimaryButton
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun LoginForm(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val firestore = FirebaseFirestore.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Welcome back!",
            style = MaterialTheme.typography.headlineSmall
        )

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email"
        )

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true
        )

        errorText?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        val isLoading = authState is AuthState.Loading

        PrimaryButton(
            text = if (isLoading) "Logging in..." else "Login",
            onClick = {
                errorText = null
                viewModel.login(email, password)
            },
            enabled = !isLoading
        )
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                val userId = (authState as AuthState.Success).userId
                firestore.collection("users").document(userId).get()
                    .addOnSuccessListener { doc ->
                        val roleStr = doc.getString("role")
                        val role = UserRole.valueOf(roleStr ?: "STUDENT")
                        when (role) {
                            UserRole.TUTOR -> navController.navigate("tutorDashboard") {
                                popUpTo("auth") { inclusive = true }
                            }
                            UserRole.STUDENT -> navController.navigate("studentHome") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    }
                    .addOnFailureListener {
                        errorText = "Failed to fetch user role."
                    }
            }

            is AuthState.Error -> {
                errorText = (authState as AuthState.Error).message
            }

            else -> Unit
        }
    }
}
