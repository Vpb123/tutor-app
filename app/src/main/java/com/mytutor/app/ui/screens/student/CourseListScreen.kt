package com.mytutor.app.ui.screens.student

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mytutor.app.presentation.auth.AuthViewModel
import com.mytutor.app.ui.components.PrimaryButton

@Composable
fun CourseListScreen(navController: NavController,  viewModel: AuthViewModel = hiltViewModel()) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "📚 Student Course List Screen",
                style = MaterialTheme.typography.headlineSmall
            )

        }
        PrimaryButton(
            onClick = {
                viewModel.logout();
            },
            text = "logout",

        )
    }
}
