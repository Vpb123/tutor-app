package com.mytutor.app.domain.usecase

import com.mytutor.app.data.remote.models.Lesson
import com.mytutor.app.data.remote.models.LessonProgress

class CanStudentAccessLessonUseCase {
    operator fun invoke(
        targetLesson: Lesson,
        allLessons: List<Lesson>,
        progressList: List<LessonProgress>
    ): Boolean {
        val completedLessonIds = progressList.map { it.lessonId }.toSet()
        val sortedLessons = allLessons.sortedBy { it.order }

        val targetIndex = sortedLessons.indexOfFirst { it.id == targetLesson.id }

        if (targetIndex == -1) return false
        if (targetIndex == 0) return true

        val previousLesson = sortedLessons.getOrNull(targetIndex - 1) ?: return false
        return completedLessonIds.contains(previousLesson.id)
    }
}
