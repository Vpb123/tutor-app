package com.mytutor.app.presentation.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mytutor.app.data.remote.models.Enrolment
import com.mytutor.app.data.remote.models.EnrolmentStatus
import com.mytutor.app.data.remote.repository.EnrolmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnrolmentViewModel @Inject constructor(
    private val enrolmentRepository: EnrolmentRepository
) : ViewModel() {

    private val _pendingEnrolments = MutableStateFlow<List<Enrolment>>(emptyList())
    val pendingEnrolments: StateFlow<List<Enrolment>> = _pendingEnrolments

    private val _acceptedEnrolments = MutableStateFlow<List<Enrolment>>(emptyList())
    val acceptedEnrolments: StateFlow<List<Enrolment>> = _acceptedEnrolments

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadPendingEnrolments(courseIds: List<String>) {
        _loading.value = true
        viewModelScope.launch {
            val result = enrolmentRepository.getPendingEnrolmentsForTutorCourses(courseIds)
            result.fold(
                onSuccess = { _pendingEnrolments.value = it },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun acceptEnrolment(courseId: String, studentId: String, onSuccess: () -> Unit = {}) {
        _loading.value = true
        viewModelScope.launch {
            val result = enrolmentRepository.updateEnrolmentStatus(courseId, studentId, EnrolmentStatus.ACCEPTED)
            result.fold(
                onSuccess = {
                    _pendingEnrolments.value = _pendingEnrolments.value.filterNot {
                        it.courseId == courseId && it.studentId == studentId
                    }
                    onSuccess()
                },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun rejectEnrolment(courseId: String, studentId: String, onSuccess: () -> Unit = {}) {
        _loading.value = true
        viewModelScope.launch {
            val result = enrolmentRepository.updateEnrolmentStatus(courseId, studentId, EnrolmentStatus.REJECTED)
            result.fold(
                onSuccess = {
                    _pendingEnrolments.value = _pendingEnrolments.value.filterNot {
                        it.courseId == courseId && it.studentId == studentId
                    }
                    onSuccess()
                },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun loadAcceptedEnrolments(courseId: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = enrolmentRepository.getEnrolmentsByCourse(courseId)
            result.fold(
                onSuccess = { enrolments ->
                    _acceptedEnrolments.value = enrolments.filter { it.status == EnrolmentStatus.ACCEPTED }
                },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }
}
