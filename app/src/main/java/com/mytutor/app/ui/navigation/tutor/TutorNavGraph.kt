package com.mytutor.app.ui.navigation.tutor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.mytutor.app.presentation.quiz.QuizViewModel
import com.mytutor.app.ui.screens.tutor.CourseProgressScreen
import com.mytutor.app.ui.screens.tutor.CreateOrEditCourseScreen
import com.mytutor.app.ui.screens.tutor.LessonEditorScreen
import com.mytutor.app.ui.screens.tutor.LessonListScreen
import com.mytutor.app.ui.screens.tutor.LessonPageBuilderScreen
import com.mytutor.app.ui.screens.tutor.QuizBuilderScreen
import com.mytutor.app.ui.screens.tutor.QuizCreatorScreen
import com.mytutor.app.ui.screens.tutor.TutorCourseListScreen
import com.mytutor.app.ui.screens.tutor.TutorDashboardScreen
import com.mytutor.app.ui.screens.tutor.TutorProfileScreen
import com.mytutor.app.ui.screens.tutor.quiz.QuizQuestionEditorScreen

@Composable
fun TutorNavGraph(navController: NavHostController, startDestination: String = TutorBottomNavItem.Dashboard.route, onLogout: () -> Unit, paddingValues: PaddingValues) {
    val lessonViewModel: LessonViewModel = hiltViewModel()
    NavHost(navController = navController, startDestination = startDestination) {
        tutorNavGraph(navController, lessonViewModel, onLogout, paddingValues)
    }
}

fun NavGraphBuilder.tutorNavGraph(navController: NavHostController, lessonViewModel: LessonViewModel,  onLogout: () -> Unit, paddingValues: PaddingValues) {
    composable(TutorBottomNavItem.Dashboard.route) {
        TutorDashboardScreen(navController, paddingValues)
    }
    composable(TutorBottomNavItem.Courses.route) {
        TutorCourseListScreen(navController, paddingValues)
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
        route = "lessonEditor/{courseId}?fromCreateCourse={fromCreateCourse}",
        arguments = listOf(
            navArgument("courseId") { type = NavType.StringType },
            navArgument("fromCreateCourse") {
                type = NavType.BoolType
                defaultValue = false
            }
        )
    ) { backStackEntry ->
        val courseId = backStackEntry.arguments?.getString("courseId")!!
        val fromCreateCourse = backStackEntry.arguments?.getBoolean("fromCreateCourse") ?: false
        LessonEditorScreen(courseId = courseId, navController = navController, lessonId = null, viewModel = lessonViewModel, fromCreateCourse = fromCreateCourse)
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

        val selectedLesson = lessonViewModel.selectedLesson.collectAsState().value

        val existingPage = remember(selectedLesson?.id, pageIndex) {
            selectedLesson?.pages?.getOrNull(pageIndex)
        }

        LessonPageBuilderScreen(
            pageIndex = pageIndex,
            existingPage = existingPage,
            navController = navController,
            viewModel = lessonViewModel,
            onSave = { page, goToNext ->
                val lesson = lessonViewModel.selectedLesson.value
                if (lesson != null) {
                    val updatedPages = lesson.pages.toMutableList()
                    if (pageIndex < updatedPages.size) {
                        updatedPages[pageIndex] = page
                    } else {
                        updatedPages.add(page)
                    }

                    val updatedLesson = lesson.copy(pages = updatedPages)
                    lessonViewModel.updateLesson(updatedLesson) {

                        lessonViewModel.selectLesson(updatedLesson.id)

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


    composable(
        route = "quizCreator/{courseId}",
        arguments = listOf(navArgument("courseId") { type = NavType.StringType })
    ) { backStackEntry ->
        val courseId = backStackEntry.arguments?.getString("courseId")!!
        QuizCreatorScreen(courseId = courseId, navController = navController)
    }

    composable(
        route = "quizBuilder/{quizId}",
        arguments = listOf(navArgument("quizId") { type = NavType.StringType })
    ) { backStackEntry ->
        val quizId = backStackEntry.arguments?.getString("quizId")!!
        QuizBuilderScreen(quizId = quizId, navController = navController)
    }

    composable(
        route = "questionEditor/{quizId}",
        arguments = listOf(navArgument("quizId") { type = NavType.StringType })
    ) { backStackEntry ->
        val quizId = backStackEntry.arguments?.getString("quizId")!!
        val viewModel: QuizViewModel = hiltViewModel()
        QuizQuestionEditorScreen(
            quizId = quizId,
            navController = navController,
            viewModel = viewModel
        )
    }

    composable("course-progress/{courseId}") { backStackEntry ->
        val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
        CourseProgressScreen(courseId = courseId, onBack = { navController.popBackStack() })
    }



}
