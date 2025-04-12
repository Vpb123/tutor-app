package com.mytutor.app.domain.usecase

import com.mytutor.app.data.remote.models.Lesson
import com.mytutor.app.data.remote.models.LessonProgress

class GetCourseProgressUseCase {
    operator fun invoke(
        lessons: List<Lesson>,
        progressList: List<LessonProgress>
    ): Float {
        if (lessons.isEmpty()) return 0f

        val completedLessonIds = progressList.map { it.lessonId }.toSet()
        val completedCount = lessons.count { completedLessonIds.contains(it.id) }

        return (completedCount.toFloat() / lessons.size.toFloat()) * 100f
    }
}
