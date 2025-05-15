package com.mytutor.app.ui.navigation.tutor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class TutorBottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Dashboard : TutorBottomNavItem("tutorDashboard", Icons.Default.Home, "Dashboard")
    object Courses : TutorBottomNavItem("tutorCourses", Icons.Default.List, "Courses")
    object Create : TutorBottomNavItem("tutorCreate", Icons.Default.Add, "Create")
    object Profile : TutorBottomNavItem("tutorProfile", Icons.Default.Person, "Profile")

    companion object {
        val all = listOf(Dashboard, Courses, Create, Profile)
    }
}
