package com.mytutor.app.presentation.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mytutor.app.data.remote.models.Lesson
import com.mytutor.app.data.remote.models.LessonProgress
import com.mytutor.app.data.remote.models.LessonStatus
import com.mytutor.app.data.remote.repository.LessonRepository
import com.mytutor.app.data.remote.repository.ProgressRepository
import com.mytutor.app.domain.usecase.CanStudentAccessLessonUseCase
import com.mytutor.app.domain.usecase.ComputeLessonStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LessonViewModel(
    private val lessonRepository: LessonRepository,
    private val computeLessonStatusesUseCase: ComputeLessonStatusUseCase,
    private val lessonProgressRepository: ProgressRepository,
    private val canStudentAccessLessonUseCase: CanStudentAccessLessonUseCase
) : ViewModel() {

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons

    private val _lessonStatuses = MutableStateFlow<Map<String, LessonStatus>>(emptyMap())
    val lessonStatuses: StateFlow<Map<String, LessonStatus>> = _lessonStatuses

    private val _selectedLesson = MutableStateFlow<Lesson?>(null)
    val selectedLesson: StateFlow<Lesson?> = _selectedLesson

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var lastStudentId: String? = null
    private var lastCourseId: String? = null

    fun loadLessons(courseId: String, studentId: String? = null) {
        lastCourseId = courseId
        lastStudentId = studentId

        viewModelScope.launch {
            val lessonResult = lessonRepository.getLessonsByCourse(courseId)
            lessonResult.fold(
                onSuccess = { lessonList ->
                    _lessons.value = lessonList
                    if (studentId != null) {
                        updateLessonStatuses(courseId, studentId, lessonList)
                    }
                },
                onFailure = { _error.value = it.message }
            )
        }
    }

    private fun updateLessonStatuses(courseId: String, studentId: String, lessons: List<Lesson>) {
        viewModelScope.launch {
            val progressResult = lessonProgressRepository.getCompletedLessons(courseId, studentId)
            progressResult.fold(
                onSuccess = { progressList ->
                    val statuses = computeLessonStatusesUseCase(lessons, progressList)
                    _lessonStatuses.value = statuses
                },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun selectLesson(lessonId: String) {
        viewModelScope.launch {
            val result = lessonRepository.getLessonById(lessonId)
            result.fold(
                onSuccess = { _selectedLesson.value = it },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun markLessonAsComplete(courseId: String, lessonId: String, studentId: String) {
        viewModelScope.launch {
            lessonProgressRepository.markLessonCompleted(
                LessonProgress(
                    courseId = courseId,
                    lessonId = lessonId,
                    studentId = studentId,
                    completedAt = System.currentTimeMillis()
                )
            )

            // Refresh all lessons and progress
            val currentLessons = _lessons.value
            updateLessonStatuses(courseId, studentId, currentLessons)
        }
    }

    fun canAccessLesson(lessonId: String, onResult: (Boolean) -> Unit) {
        val courseId = lastCourseId ?: return
        val studentId = lastStudentId ?: return
        val currentLessons = _lessons.value

        val targetLesson = currentLessons.find { it.id == lessonId } ?: run {
            onResult(false)
            return
        }

        viewModelScope.launch {
            val progressResult = lessonProgressRepository.getCompletedLessons(courseId, studentId)
            progressResult.fold(
                onSuccess = { progressList ->
                    val access = canStudentAccessLessonUseCase(targetLesson, currentLessons, progressList)
                    onResult(access)
                },
                onFailure = {
                    _error.value = it.message
                    onResult(false)
                }
            )
        }
    }

    fun updateLesson(lesson: Lesson, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = lessonRepository.updateLesson(lesson)
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun createLesson(lesson: Lesson, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = lessonRepository.createLesson(lesson)
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { _error.value = it.message }
            )
        }
    }
}
