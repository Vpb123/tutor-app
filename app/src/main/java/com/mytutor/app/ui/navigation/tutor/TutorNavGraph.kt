package com.mytutor.app.ui.navigation.tutor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mytutor.app.data.remote.models.Course
import com.mytutor.app.presentation.course.CourseViewModel
import com.mytutor.app.presentation.lesson.LessonViewModel
import com.mytutor.app.ui.screens.tutor.CreateOrEditCourseScreen
import com.mytutor.app.ui.screens.tutor.LessonEditorScreen
import com.mytutor.app.ui.screens.tutor.LessonListScreen
import com.mytutor.app.ui.screens.tutor.LessonPageBuilderScreen
import com.mytutor.app.ui.screens.tutor.TutorCourseListScreen
import com.mytutor.app.ui.screens.tutor.TutorDashboardScreen
import com.mytutor.app.ui.screens.tutor.TutorProfileScreen

@Composable
fun TutorNavGraph(navController: NavHostController, startDestination: String = TutorBottomNavItem.Dashboard.route, onLogout: () -> Unit) {
    val lessonViewModel: LessonViewModel = hiltViewModel()
    NavHost(navController = navController, startDestination = startDestination) {
        tutorNavGraph(navController, lessonViewModel, onLogout)
    }
}

fun NavGraphBuilder.tutorNavGraph(navController: NavHostController, lessonViewModel: LessonViewModel,  onLogout: () -> Unit) {
    composable(TutorBottomNavItem.Dashboard.route) {
        TutorDashboardScreen(navController)
    }
    composable(TutorBottomNavItem.Courses.route) {
        TutorCourseListScreen(navController)
    }

    composable(TutorBottomNavItem.Create.route) {
            CreateOrEditCourseScreen(
                navController = navController,
                existingCourse = null
            )
    }

    composable(TutorBottomNavItem.Profile.route) {
        TutorProfileScreen(navController, onLogout)
    }

    composable(
        route = "lessonEditor/{courseId}",
        arguments = listOf(
            navArgument("courseId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val courseId = backStackEntry.arguments?.getString("courseId")!!
        LessonEditorScreen(courseId = courseId, navController = navController, lessonId = null, viewModel = lessonViewModel)
    }

    composable(
        route = "lessonEditor/{courseId}/{lessonId}",
        arguments = listOf(
            navArgument("courseId") { type = NavType.StringType },
            navArgument("lessonId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val courseId = backStackEntry.arguments?.getString("courseId")!!
        val lessonId = backStackEntry.arguments?.getString("lessonId")
        LessonEditorScreen(courseId = courseId, navController = navController, lessonId = lessonId, viewModel = lessonViewModel)
    }

    composable("lessonList/{courseId}") { entry ->
        val courseId = entry.arguments?.getString("courseId")!!
        LessonListScreen(courseId = courseId, navController = navController)
    }

    composable("createCourse/{courseId}") { entry ->
        val courseId = entry.arguments?.getString("courseId")!!
        val courseViewModel: CourseViewModel = hiltViewModel()

        var course by remember { mutableStateOf<Course?>(null) }

        LaunchedEffect(courseId) {
            courseViewModel.getCourseById(courseId) {
                course = it
            }
        }

        if (course != null) {
            CreateOrEditCourseScreen(
                navController = navController,
                existingCourse = course
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    composable(
        route = "editPageBuilder/{pageIndex}",
        arguments = listOf(
            navArgument("pageIndex") { type = NavType.IntType }
        )
    ) { backStackEntry ->
        val pageIndex = backStackEntry.arguments?.getInt("pageIndex") ?: 0
        val existingPage = remember(lessonViewModel.selectedLesson, pageIndex) {
            lessonViewModel.selectedLesson.value?.pages?.getOrNull(pageIndex)
        }

        LessonPageBuilderScreen(
            pageIndex = pageIndex,
            existingPage = existingPage,
            navController = navController,
            viewModel = lessonViewModel,
            onSave = { page, goToNext ->
                val lesson = lessonViewModel.selectedLesson.value
                println("selectedLesson: $lesson")
                if (lesson != null) {
                    val updatedPages = lesson.pages.toMutableList()
                    if (pageIndex < updatedPages.size) {
                        updatedPages[pageIndex] = page
                    } else {
                        updatedPages.add(page)
                    }

                    lessonViewModel.updateLesson(lesson.copy(pages = updatedPages)) {
                        if (goToNext) {
                            navController.navigate("editPageBuilder/${updatedPages.size}")
                        } else {
                            navController.popBackStack()
                        }
                    }

                }
            }
        )
    }



}
