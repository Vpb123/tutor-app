package com.mytutor.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mytutor.app.ui.screens.auth.components.LoginForm
import com.mytutor.app.ui.screens.auth.components.RegisterForm

@Composable
fun AuthScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Login", "Register")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = selectedTab == index,
                    onClick = { selectedTab = index }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (selectedTab) {
            0 -> LoginForm(navController = navController)
            1 -> RegisterForm(navController = navController)
        }
    }
}
