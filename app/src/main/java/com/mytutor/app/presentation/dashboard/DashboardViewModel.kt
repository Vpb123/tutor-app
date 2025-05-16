package com.mytutor.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mytutor.app.data.remote.models.CourseAnalytics
import com.mytutor.app.data.remote.repository.CourseRepository
import com.mytutor.app.data.remote.repository.EnrolmentRepository
import com.mytutor.app.data.remote.repository.UserRepository
import com.mytutor.app.domain.usecase.GetTutorDashboardStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getTutorDashboardStatsUseCase: GetTutorDashboardStatsUseCase,
    private val courseRepository: CourseRepository,
    private val enrolmentRepository: EnrolmentRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _dashboardData = MutableStateFlow<List<CourseAnalytics>>(emptyList())
    val dashboardData: StateFlow<List<CourseAnalytics>> = _dashboardData

    private val _courseStats = MutableStateFlow(Pair(0, 0)) // (published, unpublished)
    val courseStats: StateFlow<Pair<Int, Int>> = _courseStats

    private val _pendingRequests = MutableStateFlow<List<EnrolmentRequestUiModel>>(emptyList())
    val pendingRequests: StateFlow<List<EnrolmentRequestUiModel>> = _pendingRequests

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadDashboard(tutorId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val coursesResult = courseRepository.getCoursesByTutor(tutorId)
                val courses = coursesResult.getOrNull().orEmpty()
                val courseIds = courses.map { it.id }

                val published = courses.count { it.isPublished }
                val unpublished = courses.size - published
                _courseStats.value = Pair(published, unpublished)

                val analyticsResult = getTutorDashboardStatsUseCase(tutorId)
                analyticsResult.onSuccess {
                    _dashboardData.value = it
                }.onFailure {
                    _error.value = it.message
                }

                val enrolmentsResult = enrolmentRepository.getPendingEnrolmentsForTutorCourses(courseIds)
                val enrolments = enrolmentsResult.getOrNull().orEmpty()

                val enrolmentUiModels = mutableListOf<EnrolmentRequestUiModel>()

                for (enrolment in enrolments) {
                    val studentResult = userRepository.getUserById(enrolment.studentId)
                    val courseTitle = courses.find { it.id == enrolment.courseId }?.title ?: "Untitled"

                    if (studentResult.isSuccess) {
                        val studentName = studentResult.getOrNull()?.displayName ?: "Unnamed"
                        enrolmentUiModels += EnrolmentRequestUiModel(
                            enrolmentId = enrolment.id,
                            studentName = studentName,
                            courseTitle = courseTitle,
                            requestedAt = enrolment.requestedAt,
                            courseId = enrolment.courseId,
                            studentId = enrolment.studentId
                        )
                    }
                }

                _pendingRequests.value = enrolmentUiModels
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
