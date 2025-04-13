package com.mytutor.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mytutor.app.data.remote.models.CourseAnalytics
import com.mytutor.app.domain.usecase.GetTutorDashboardStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getTutorDashboardStatsUseCase: GetTutorDashboardStatsUseCase
) : ViewModel() {

    private val _dashboardData = MutableStateFlow<List<CourseAnalytics>>(emptyList())
    val dashboardData: StateFlow<List<CourseAnalytics>> = _dashboardData

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadDashboard(tutorId: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = getTutorDashboardStatsUseCase(tutorId)
            result.fold(
                onSuccess = { _dashboardData.value = it },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }
}
