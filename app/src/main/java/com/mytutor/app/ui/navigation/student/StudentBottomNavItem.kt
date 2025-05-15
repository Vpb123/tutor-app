package com.mytutor.app.ui.navigation.student

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class StudentBottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object AllCourses : StudentBottomNavItem(
        route = "student/allCourses",
        label = "Courses",
        icon = Icons.Filled.MenuBook
    )

    object MyCourses : StudentBottomNavItem(
        route = "student/myCourses",
        label = "My Courses",
        icon = Icons.Filled.School
    )

    object Profile : StudentBottomNavItem(
        route = "student/profile",
        label = "Profile",
        icon = Icons.Filled.Person
    )

    object Settings : StudentBottomNavItem(
        route = "student/settings",
        label = "Settings",
        icon = Icons.Filled.Settings
    )

    companion object {
        val all = listOf(AllCourses, MyCourses, Profile, Settings)
    }
}
