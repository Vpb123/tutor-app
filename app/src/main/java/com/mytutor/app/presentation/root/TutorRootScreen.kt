package com.mytutor.app.presentation.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mytutor.app.ui.navigation.tutor.TutorBottomNavBar
import com.mytutor.app.ui.navigation.tutor.TutorBottomNavItem
import com.mytutor.app.ui.navigation.tutor.TutorNavGraph

@Composable
fun TutorRootScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = TutorBottomNavItem.all.any { currentRoute?.startsWith(it.route) == true }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                TutorBottomNavBar(navController)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TutorNavGraph(navController = navController, onLogout = onLogout, paddingValues=innerPadding)
        }
    }
}
