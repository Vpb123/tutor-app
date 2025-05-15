package com.mytutor.app.presentation.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mytutor.app.data.remote.models.Course
import com.mytutor.app.data.remote.repository.CourseRepository
import com.mytutor.app.data.remote.repository.EnrolmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val enrolmentRepository: EnrolmentRepository
) : ViewModel(){

    private val _allCourses = MutableStateFlow<List<Course>>(emptyList())
    val allCourses: StateFlow<List<Course>> = _allCourses

    private val _myCourses = MutableStateFlow<List<Course>>(emptyList())
    val myCourses: StateFlow<List<Course>> = _myCourses

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadAllCourses() {
        _loading.value = true
        viewModelScope.launch {
            val result = courseRepository.getAllCourses()
            result.fold(
                onSuccess = { _allCourses.value = it },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun loadMyCourses(studentId: String) {
        _loading.value = true
        viewModelScope.launch {
            val courseIds = enrolmentRepository.getAcceptedCoursesForStudent(studentId).getOrNull().orEmpty()
            val result = courseRepository.getCoursesByIds(courseIds)
            result.fold(
                onSuccess = { _myCourses.value = it },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }


    fun loadCoursesByTutor(tutorId: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = courseRepository.getCoursesByTutor(tutorId)
            result.fold(
                onSuccess = { _myCourses.value = it },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun requestEnrolment(studentId: String, courseId: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = enrolmentRepository.requestEnrolment(studentId, courseId)
            result.fold(
                onSuccess = { loadMyCourses(studentId) },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun createCourse(course: Course, onSuccess: (String) -> Unit) {
        _loading.value = true
        viewModelScope.launch {
            val result = courseRepository.createCourse(course)
            result.fold(
                onSuccess = { id ->
                    loadCoursesByTutor(course.tutorId)
                    onSuccess(id)
                },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun updateCourse(course: Course, onSuccess: () -> Unit) {
        _loading.value = true
        viewModelScope.launch {
            val result = courseRepository.updateCourse(course)
            result.fold(
                onSuccess = {
                    loadCoursesByTutor(course.tutorId)
                    onSuccess()
                },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun deleteCourse(courseId: String, tutorId: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = courseRepository.deleteCourse(courseId)
            result.fold(
                onSuccess = { loadCoursesByTutor(tutorId) },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun getCourseById(courseId: String, onResult: (Course?) -> Unit) {
        viewModelScope.launch {
            val result = courseRepository.getCourseById(courseId)
            result.fold(
                onSuccess = { onResult(it) },
                onFailure = {
                    _error.value = it.message
                    onResult(null)
                }
            )
        }
    }
}
