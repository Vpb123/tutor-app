package com.mytutor.app.ui.navigation.student

import android.os.Build
import androidx.annotation.RequiresApi
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
import com.mytutor.app.ui.screens.student.MyCoursesScreen
import com.mytutor.app.ui.screens.student.StudentProfileScreen
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.Text
import com.mytutor.app.ui.screens.common.SettingsScreen

@RequiresApi(Build.VERSION_CODES.P)
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

@RequiresApi(Build.VERSION_CODES.P)
fun NavGraphBuilder.studentNavGraph(
    navController: NavHostController,
    courseViewModel: CourseViewModel,
    lessonViewModel: LessonViewModel
) {
    composable(StudentBottomNavItem.AllCourses.route) {
        AllCoursesScreen(navController, courseViewModel)
    }
    composable(StudentBottomNavItem.MyCourses.route) {
        MyCoursesScreen(navController)
    }
    composable(StudentBottomNavItem.Profile.route) {
//        StudentProfileScreen(navController)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            StudentProfileScreen()
        } else {
            // Handle unauthenticated state
            Text("Not logged in")
        }
    }
    composable(StudentBottomNavItem.Settings.route) {
        SettingsScreen(navController = navController)
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
