package com.mytutor.app.domain.usecase

import com.mytutor.app.data.remote.models.EnrolmentStatus
import com.mytutor.app.data.remote.repository.CourseRepository
import com.mytutor.app.data.remote.repository.EnrolmentRepository
import com.mytutor.app.data.remote.repository.LessonRepository
import com.mytutor.app.data.remote.repository.ProgressRepository
import com.mytutor.app.data.remote.repository.QuizResultRepository
import com.mytutor.app.data.remote.models.CourseAnalytics
import com.mytutor.app.data.remote.models.CourseCompletionStatus
import com.mytutor.app.data.remote.repository.QuizRepository

class GetTutorDashboardStatsUseCase(
    private val courseRepository: CourseRepository,
    private val enrolmentRepository: EnrolmentRepository,
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository,
    private val quizResultRepository: QuizResultRepository,
    private val quizRepository: QuizRepository,
    private val getCourseProgressUseCase: GetCourseProgressUseCase,
    private val getCourseCompletionStatusUseCase: GetCourseCompletionStatusUseCase
) {
    suspend operator fun invoke(tutorId: String): Result<List<CourseAnalytics>> {
        return try {
            val coursesResult = courseRepository.getCoursesByTutor(tutorId)
            if (coursesResult.isFailure) return Result.failure(coursesResult.exceptionOrNull()!!)

            val resultList = mutableListOf<CourseAnalytics>()
            val courses = coursesResult.getOrNull().orEmpty()
            println("courses in use case $courses")
            for (course in courses) {
                val quiz = quizRepository.getQuizByCourseId(course.id).getOrNull()
                val quizId = quiz?.id
                val enrolments = enrolmentRepository
                    .getEnrolmentsByCourse(course.id)
                    .getOrNull()
                    ?.filter { it.status == EnrolmentStatus.ACCEPTED }
                    ?: emptyList()
                println("enrolments in use case $enrolments")
                val lessons = lessonRepository.getLessonsByCourse(course.id).getOrNull().orEmpty()

                var totalProgress = 0f
                var completedCount = 0
                var passedQuizCount = 0

                for (enrolment in enrolments) {
                    val progressList = progressRepository
                        .getCompletedLessons(course.id, enrolment.studentId)
                        .getOrNull()
                        .orEmpty()

                    val progressPercent = getCourseProgressUseCase(lessons, progressList)
                    totalProgress += progressPercent


                    val status = if (quizId != null) {
                        getCourseCompletionStatusUseCase(
                            courseId = course.id,
                            lessons = lessons,
                            progressList = progressList,
                            quizId = quizId,
                            studentId = enrolment.studentId
                        )
                    } else null

                    if (status == CourseCompletionStatus.COMPLETED) completedCount++

                    if (quizId != null) {
                        val result = quizResultRepository.getQuizResult(quizId, enrolment.studentId).getOrNull()
                        if ((result?.score ?: 0) >= 50) passedQuizCount++
                    }

                }

                val avgProgress = if (enrolments.isNotEmpty()) {
                    totalProgress / enrolments.size
                } else 0f

                resultList.add(
                    CourseAnalytics(
                        courseId = course.id,
                        courseTitle = course.title,
                        enrolledCount = enrolments.size,
                        averageProgress = avgProgress,
                        completedCount = completedCount,
                        passedQuizCount = passedQuizCount
                    )
                )
            }

            Result.success(resultList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
