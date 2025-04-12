package com.mytutor.app.data.remote.repository

import com.google.firebase.firestore.Query
import com.mytutor.app.data.remote.models.Lesson
import com.mytutor.app.data.remote.FirebaseService
import kotlinx.coroutines.tasks.await

class LessonRepository {

    private val firestore = FirebaseService.firestore
    private val lessonsCollection = firestore.collection("lessons")

    suspend fun createLesson(lesson: Lesson): Result<Unit> {
        return try {
            val docRef = if (lesson.id.isBlank()) {
                lessonsCollection.document()
            } else {
                lessonsCollection.document(lesson.id)
            }

            val lessonWithId = lesson.copy(id = docRef.id)
            docRef.set(lessonWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLessonsByCourse(courseId: String): Result<List<Lesson>> {
        return try {
            val snapshot = lessonsCollection
                .whereEqualTo("courseId", courseId)
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()

            val lessons = snapshot.toObjects(Lesson::class.java)
            Result.success(lessons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLessonById(lessonId: String): Result<Lesson> {
        return try {
            val snapshot = lessonsCollection.document(lessonId).get().await()
            val lesson = snapshot.toObject(Lesson::class.java)
                ?: throw Exception("Lesson not found")
            Result.success(lesson)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLesson(lesson: Lesson): Result<Unit> {
        return try {
            lessonsCollection.document(lesson.id).set(lesson).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
