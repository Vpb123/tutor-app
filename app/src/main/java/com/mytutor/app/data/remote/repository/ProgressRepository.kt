package com.mytutor.app.data.remote.repository

import com.mytutor.app.data.remote.models.Lesson
import com.mytutor.app.data.remote.models.LessonProgress
import com.mytutor.app.data.remote.FirebaseService
import kotlinx.coroutines.tasks.await

class ProgressRepository {

    private val firestore = FirebaseService.firestore
    private val progressCollection = firestore.collection("lessonProgress")

    suspend fun markLessonCompleted(progress: LessonProgress): Result<Unit> {
        return try {
            // Use a composite ID (studentId+lessonId) to ensure uniqueness
            val docId = "${progress.studentId}_${progress.lessonId}"
            progressCollection.document(docId).set(progress).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCompletedLessons(courseId: String, studentId: String): Result<List<LessonProgress>> {
        return try {
            val snapshot = progressCollection
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("studentId", studentId)
                .get()
                .await()

            val progressList = snapshot.toObjects(LessonProgress::class.java)
            Result.success(progressList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCourseProgressPercent(
        lessons: List<Lesson>,
        progressList: List<LessonProgress>
    ): Float {
        if (lessons.isEmpty()) return 0f
        val completedCount = progressList.count { progress -> lessons.any { it.id == progress.lessonId } }
        return (completedCount.toFloat() / lessons.size.toFloat()) * 100
    }
}
