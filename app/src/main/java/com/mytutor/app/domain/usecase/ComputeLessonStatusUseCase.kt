package com.mytutor.app.domain.usecase

import com.mytutor.app.data.remote.models.Lesson
import com.mytutor.app.data.remote.models.LessonProgress
import com.mytutor.app.data.remote.models.LessonStatus

class ComputeLessonStatusUseCase {
    operator fun invoke(
        lessons: List<Lesson>,
        progressList: List<LessonProgress>
    ): Map<String, LessonStatus> {
        val completedIds = progressList.map { it.lessonId }.toSet()
        val statusMap = mutableMapOf<String, LessonStatus>()

        lessons.sortedBy { it.order }.forEachIndexed { index, lesson ->
            statusMap[lesson.id] = when {
                completedIds.contains(lesson.id) -> LessonStatus.COMPLETED
                index == 0 || statusMap[lessons[index - 1].id] == LessonStatus.COMPLETED -> LessonStatus.AVAILABLE
                else -> LessonStatus.LOCKED
            }
        }

        return statusMap
    }
}