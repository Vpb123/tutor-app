package com.mytutor.app.ui.screens.auth.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mytutor.app.data.remote.models.UserRole
import com.mytutor.app.presentation.auth.AuthState
import com.mytutor.app.presentation.auth.AuthViewModel
import com.mytutor.app.ui.components.AuthTextField
import com.mytutor.app.ui.components.PrimaryButton

@Composable
fun RegisterForm(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val isLoading = authState is AuthState.Loading

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Create an account",
            style = MaterialTheme.typography.headlineSmall
        )

        AuthTextField(
            value = name,
            onValueChange = { name = it },
            label = "Display Name"
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

        RoleSelector(
            selectedRole = selectedRole,
            onRoleSelected = { role -> selectedRole = role }
        )

        errorText?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        PrimaryButton(
            text = if (isLoading) "Registering..." else "Register",
            onClick = {
                errorText = null
                viewModel.register(email, password, name, selectedRole)
            },
            enabled = !isLoading
        )
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                when (selectedRole) {
                    UserRole.TUTOR -> navController.navigate("tutorDashboard") {
                        popUpTo("auth") { inclusive = true }
                    }
                    UserRole.STUDENT -> navController.navigate("studentHome") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            }

            is AuthState.Error -> {
                errorText = (authState as AuthState.Error).message
            }

            else -> Unit
        }
    }
}
