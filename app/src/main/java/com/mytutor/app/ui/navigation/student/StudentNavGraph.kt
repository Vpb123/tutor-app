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
import com.mytutor.app.data.remote.FirebaseService.currentUserId
import com.mytutor.app.presentation.course.CourseViewModel
import com.mytutor.app.presentation.lesson.LessonViewModel
import com.mytutor.app.ui.screens.student.AllCoursesScreen
import com.mytutor.app.ui.screens.student.CourseViewScreen
import com.mytutor.app.ui.screens.student.LessonPageViewScreen
import com.mytutor.app.ui.screens.student.MyCoursesScreen
import com.mytutor.app.ui.screens.student.StudentQuizScreen
import com.mytutor.app.ui.screens.student.StudentSettingsScreen

import com.mytutor.app.ui.screens.student.StudentProfileScreen
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.Text
import com.mytutor.app.ui.screens.common.SettingsScreen

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun StudentNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit,
    startDestination: String = StudentBottomNavItem.AllCourses.route,
) {
    val courseViewModel: CourseViewModel = hiltViewModel()
    val lessonViewModel: LessonViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = startDestination) {
        studentNavGraph(navController, courseViewModel, lessonViewModel, onLogout)
    }
}

@RequiresApi(Build.VERSION_CODES.P)
fun NavGraphBuilder.studentNavGraph(
    navController: NavHostController,
    courseViewModel: CourseViewModel,
    lessonViewModel: LessonViewModel,
    onLogout: () -> Unit
) {
    composable(StudentBottomNavItem.AllCourses.route) {
        AllCoursesScreen(navController, courseViewModel)
    }
    composable(StudentBottomNavItem.MyCourses.route) {
        MyCoursesScreen(navController, courseViewModel)
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
        StudentSettingsScreen(navController, onLogout)
    }

    composable(
        route = "courseDetail/{courseId}",
        arguments = listOf(navArgument("courseId") { type = NavType.StringType })
    ) { backStackEntry ->
        val courseId = backStackEntry.arguments?.getString("courseId")!!

        CourseViewScreen(
            courseId = courseId,
            navController = navController,
            courseViewModel = courseViewModel
        )
    }
    composable(
        route = "lessonView/{courseId}/{lessonId}",
        arguments = listOf(
            navArgument("courseId") { type = NavType.StringType },
            navArgument("lessonId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val courseId = backStackEntry.arguments?.getString("courseId")!!
        val lessonId = backStackEntry.arguments?.getString("lessonId")!!
        val studentId = currentUserId ?: return@composable

        LessonPageViewScreen(
            lessonId = lessonId,
            courseId = courseId,
            studentId = studentId,
            navController = navController
        )
    }

    composable(
        route = "takeQuiz/{courseId}/{studentId}",
        arguments = listOf(
            navArgument("courseId") { type = NavType.StringType },
            navArgument("studentId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val courseId = backStackEntry.arguments?.getString("courseId")!!
        val studentId = backStackEntry.arguments?.getString("studentId")!!

        StudentQuizScreen(
            courseId = courseId,
            studentId = studentId,
            navController = navController
        )
    }


}
