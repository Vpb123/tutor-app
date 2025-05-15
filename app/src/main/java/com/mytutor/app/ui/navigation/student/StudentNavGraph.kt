package com.mytutor.app.ui.navigation.student

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mytutor.app.presentation.course.CourseViewModel
import com.mytutor.app.presentation.lesson.LessonViewModel
import com.mytutor.app.ui.screens.student.AllCoursesScreen


@Composable
fun StudentNavGraph(
    navController: NavHostController,
    startDestination: String = StudentBottomNavItem.AllCourses.route
) {
    val courseViewModel: CourseViewModel = hiltViewModel()
    val lessonViewModel: LessonViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = startDestination) {
        studentNavGraph(navController, courseViewModel, lessonViewModel)
    }
}

fun NavGraphBuilder.studentNavGraph(
    navController: NavHostController,
    courseViewModel: CourseViewModel,
    lessonViewModel: LessonViewModel
) {
    // Bottom Nav Screens
    composable(StudentBottomNavItem.AllCourses.route) {
        AllCoursesScreen(navController, courseViewModel)
    }
    composable(StudentBottomNavItem.MyCourses.route) {
//        MyCoursesScreen(navController, courseViewModel)
    }
    composable(StudentBottomNavItem.Profile.route) {
//        StudentProfileScreen(navController)
    }
    composable(StudentBottomNavItem.Settings.route) {
//        StudentSettingsScreen(navController)
    }

//    composable(
//        route = "courseDetail/{courseId}",
//        arguments = listOf(navArgument("courseId") { type = NavType.StringType })
//    ) { backStackEntry ->
//        val courseId = backStackEntry.arguments?.getString("courseId")!!
//        StudentCourseDetailScreen(navController, courseId, courseViewModel)
//    }
//
//    composable(
//        route = "lessonList/{courseId}",
//        arguments = listOf(navArgument("courseId") { type = NavType.StringType })
//    ) { backStackEntry ->
//        val courseId = backStackEntry.arguments?.getString("courseId")!!
//        StudentLessonListScreen(navController, courseId, lessonViewModel)
//    }
//
//    composable(
//        route = "lessonView/{courseId}/{lessonId}",
//        arguments = listOf(
//            navArgument("courseId") { type = NavType.StringType },
//            navArgument("lessonId") { type = NavType.StringType }
//        )
//    ) { backStackEntry ->
//        val courseId = backStackEntry.arguments?.getString("courseId")!!
//        val lessonId = backStackEntry.arguments?.getString("lessonId")!!
//        StudentLessonViewScreen(navController, courseId, lessonId, lessonViewModel)
//    }
}
