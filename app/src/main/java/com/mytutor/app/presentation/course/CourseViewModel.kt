package com.mytutor.app.presentation.course

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mytutor.app.data.remote.models.Course
import com.mytutor.app.data.remote.models.CourseCompletionStatus
import com.mytutor.app.data.remote.models.Enrolment
import com.mytutor.app.data.remote.models.EnrolmentStatus
import com.mytutor.app.data.remote.models.Lesson
import com.mytutor.app.data.remote.models.LessonProgress
import com.mytutor.app.data.remote.repository.CourseRepository
import com.mytutor.app.data.remote.repository.EnrolmentRepository
import com.mytutor.app.data.remote.repository.LessonRepository
import com.mytutor.app.data.remote.repository.ProgressRepository
import com.mytutor.app.data.remote.repository.QuizResultRepository
import com.mytutor.app.data.remote.repository.UserRepository
import com.mytutor.app.domain.usecase.GetCourseCompletionStatusUseCase
import com.mytutor.app.presentation.dashboard.EnrolmentRequestUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val enrolmentRepository: EnrolmentRepository,
    private val userRepository: UserRepository,
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository,
    private val quizResultRepository: QuizResultRepository
) : ViewModel(){

    private val _allCourses = MutableStateFlow<List<Course>>(emptyList())
    val allCourses: StateFlow<List<Course>> = _allCourses
    val tutorNames = mutableStateMapOf<String, String>()
    val lessonCounts = mutableStateMapOf<String, Int>()
    val pendingEnrolments= MutableStateFlow<List<EnrolmentRequestUiModel>>(emptyList())
    val acceptedEnrolments = MutableStateFlow<List<EnrolmentRequestUiModel>>(emptyList())

    val courseProgress = mutableStateMapOf<String, Float>()
    val currentLesson = mutableStateMapOf<String, Lesson?>()
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
                onSuccess = { courses ->
                    _allCourses.value = courses

                    courses.forEach { course ->

                        if (!tutorNames.containsKey(course.tutorId)) {
                            viewModelScope.launch {
                                userRepository.getUserById(course.tutorId).getOrNull()?.let {
                                    tutorNames[course.tutorId] = it.displayName
                                }
                            }
                        }

                        if (!lessonCounts.containsKey(course.id)) {
                            viewModelScope.launch {
                                lessonRepository.getLessonsByCourse(course.id).getOrNull()?.let {
                                    lessonCounts[course.id] = it.size
                                }
                            }
                        }
                    }
                },
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
            val result = enrolmentRepository.requestEnrolment(courseId, studentId)
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

    fun loadStudentEnrolmentsAndProgress(studentId: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = enrolmentRepository.getEnrolmentsByStudent(studentId)
            result.fold(
                onSuccess = { enrolments ->

                    val acceptedUiList = mutableListOf<EnrolmentRequestUiModel>()
                    val pendingUiList = mutableListOf<EnrolmentRequestUiModel>()

                    enrolments.forEach { enrolment ->
                        val course = courseRepository.getCourseById(enrolment.courseId).getOrNull()
                        if (course != null) {
                            val cachedTutorName = tutorNames[course.tutorId]
                            if (cachedTutorName == null) {
                                launch {
                                    userRepository.getUserById(course.tutorId).getOrNull()?.let {
                                        tutorNames[course.tutorId] = it.displayName
                                    }
                                }
                            }
                            val tutorName = cachedTutorName ?: "Tutor"

                            val uiModel = EnrolmentRequestUiModel(
                                enrolmentId = enrolment.id,
                                studentName = "",
                                courseTitle = course.title,
                                requestedAt = enrolment.requestedAt,
                                courseId = course.id,
                                studentId = enrolment.studentId,
                                tutorName = tutorName,
                                status = enrolment.status,
                                description = course.description,
                                subject = course.subject?.name
                            )

                            if (enrolment.status == EnrolmentStatus.ACCEPTED) {
                                acceptedUiList += uiModel

                                launch {
                                    val lessons = lessonRepository.getLessonsByCourse(course.id).getOrNull().orEmpty()
                                    val progress = progressRepository.getCompletedLessons(course.id, studentId).getOrNull().orEmpty()

                                    lessonCounts[course.id] = lessons.size
                                    courseProgress[course.id] = progressRepository.getCourseProgressPercent(lessons, progress)
                                    currentLesson[course.id] = lessons.firstOrNull { lesson ->
                                        progress.none { it.lessonId == lesson.id }
                                    }
                                }
                            } else if (enrolment.status == EnrolmentStatus.PENDING) {
                                pendingUiList += uiModel
                            }
                        }
                    }

                    acceptedEnrolments.value = acceptedUiList
                    pendingEnrolments.value = pendingUiList
                },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }


    fun loadLessonsAndProgress(
        courseId: String,
        studentId: String,
        onResult: (lessons: List<Lesson>, progress: List<LessonProgress>) -> Unit
    ) {
        viewModelScope.launch {
            val lessons = lessonRepository.getLessonsByCourse(courseId).getOrNull().orEmpty()
            val progress = progressRepository.getCompletedLessons(courseId, studentId).getOrNull().orEmpty()
            onResult(lessons, progress)
        }
    }

    fun getCourseCompletionStatus(
        courseId: String,
        lessons: List<Lesson>,
        progress: List<LessonProgress>,
        quizId: String,
        studentId: String,
        passThreshold: Int = 50,
        onResult: (CourseCompletionStatus) -> Unit
    ) {
        viewModelScope.launch {
            val status = GetCourseCompletionStatusUseCase(quizResultRepository)(
                courseId, lessons, progress, quizId, studentId, passThreshold
            )
            onResult(status)
        }
    }


}
