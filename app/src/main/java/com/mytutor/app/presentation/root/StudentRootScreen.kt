package com.mytutor.app.presentation.root

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mytutor.app.ui.navigation.student.StudentBottomNavBar
import com.mytutor.app.ui.navigation.student.StudentBottomNavItem
import com.mytutor.app.ui.navigation.student.StudentNavGraph

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun StudentRootScreen(onLogout: () -> Unit,    isDarkTheme: Boolean,
                      onToggleTheme: (Boolean) -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = StudentBottomNavItem.all.any { currentRoute?.startsWith(it.route) == true }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                StudentBottomNavBar(navController)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            StudentNavGraph(navController = navController, onLogout = onLogout, isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme)
        }
    }
}
