package com.mytutor.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mytutor.app.presentation.root.StudentRootScreen
import com.mytutor.app.presentation.root.TutorRootScreen
import com.mytutor.app.ui.screens.SplashScreen
import com.mytutor.app.ui.screens.auth.AuthScreen
import com.mytutor.app.ui.theme.TutorAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TutorAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController = navController)
        }
        composable("auth") {
            AuthScreen(navController = navController)
        }
        composable("studentHome") {
            StudentRootScreen(  onLogout = {
                navController.navigate("auth") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }
        composable("tutorDashboard") {
           TutorRootScreen(
               onLogout = {
                   navController.navigate("auth") {
                       popUpTo(0) { inclusive = true }
                   }
               }
           )
        }
    }
}
