package com.mytutor.app.domain.usecase

import com.mytutor.app.data.remote.models.Lesson
import com.mytutor.app.data.remote.models.LessonProgress
import com.mytutor.app.data.remote.repository.QuizResultRepository
import com.mytutor.app.data.remote.models.CourseCompletionStatus

class GetCourseCompletionStatusUseCase(
    private val quizResultRepository: QuizResultRepository
) {
    suspend operator fun invoke(
        courseId: String,
        lessons: List<Lesson>,
        progressList: List<LessonProgress>,
        quizId: String,
        studentId: String,
        passThreshold: Int = 50
    ): CourseCompletionStatus {
        val completedLessonIds = progressList.map { it.lessonId }.toSet()

        return when {
            completedLessonIds.isEmpty() -> CourseCompletionStatus.NOT_STARTED

            !lessons.all { completedLessonIds.contains(it.id) } ->
                CourseCompletionStatus.IN_PROGRESS

            else -> {
                val quizResult = quizResultRepository.getQuizResult(quizId, studentId).getOrNull()
                if (quizResult == null || quizResult.score < passThreshold)
                    CourseCompletionStatus.QUIZ_PENDING
                else
                    CourseCompletionStatus.COMPLETED
            }
        }
    }
}
