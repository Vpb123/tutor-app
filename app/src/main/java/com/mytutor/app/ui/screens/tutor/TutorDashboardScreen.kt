package com.mytutor.app.ui.screens.tutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mytutor.app.presentation.course.EnrolmentViewModel
import com.mytutor.app.presentation.dashboard.DashboardViewModel
import com.mytutor.app.presentation.user.UserViewModel
import com.mytutor.app.ui.screens.tutor.components.CourseSummaryCard
import com.mytutor.app.ui.screens.tutor.components.DashboardChart
import com.mytutor.app.ui.screens.tutor.components.DashboardDonutChart
import com.mytutor.app.ui.screens.tutor.components.PendingRequestsSection


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorDashboardScreen(
    navController: NavController,
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    enrolmentViewModel: EnrolmentViewModel = hiltViewModel()
) {
    val user by userViewModel.user.collectAsState()
    val loading by dashboardViewModel.loading.collectAsState()
    val error by dashboardViewModel.error.collectAsState()
    val courseStats by dashboardViewModel.courseStats.collectAsState()
    val analytics by dashboardViewModel.dashboardData.collectAsState()
    val pendingRequests by dashboardViewModel.pendingRequests.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadCurrentUserProfile()
    }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { dashboardViewModel.loadDashboard(it) }
        println("DashboardScreen: $analytics");
    }
    LaunchedEffect(analytics) {
        println("📊 Analytics Updated:")
        analytics.forEach {
            println("Course: ${it.courseTitle}, Enrolled: ${it.enrolledCount}, Progress: ${it.averageProgress}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Dashboard") }
            )
        }
    ) {innerPadding ->
        when {
            loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.heightIn(700.dp)
                        .padding(16.dp).padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item { CourseSummaryCard( publishedCount = courseStats.first,
                        unpublishedCount = courseStats.second) }
                    item {
                        PendingRequestsSection(
                            requests = pendingRequests,
                            onAccept = { request ->
                                enrolmentViewModel.acceptEnrolment(
                                    courseId = request.courseId,
                                    studentId = request.studentId
                                )
                            },
                            onReject = { request ->
                                enrolmentViewModel.rejectEnrolment(
                                    courseId = request.courseId,
                                    studentId = request.studentId
                                )
                            }
                        )
                    }
                    item {
                        DashboardChart(
                            analytics = analytics
                        )
                    }
                    item {
                        DashboardDonutChart(
                            analytics = analytics

                            )
                    }
                }
            }
        }
    }
}
