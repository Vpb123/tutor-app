package com.mytutor.app.data.remote.repository

import com.google.firebase.firestore.Query
import com.mytutor.app.data.remote.models.Course
import com.mytutor.app.data.remote.models.CourseSubject
import com.mytutor.app.data.remote.FirebaseService
import kotlinx.coroutines.tasks.await

class CourseRepository {

    private val firestore = FirebaseService.firestore
    private val coursesCollection = firestore.collection("courses")

    suspend fun createCourse(course: Course): Result<String> {
        return try {
            val docRef = if (course.id.isBlank()) {
                coursesCollection.document() // Auto-generate ID
            } else {
                coursesCollection.document(course.id)
            }
            val courseWithId = course.copy(id = docRef.id)
            docRef.set(courseWithId).await()
            Result.success(courseWithId.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllCourses(): Result<List<Course>> {
        return try {
            val snapshot = coursesCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val courses = snapshot.toObjects(Course::class.java)
            Result.success(courses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCoursesByTutor(tutorId: String): Result<List<Course>> {
        return try {
            val snapshot = coursesCollection
                .whereEqualTo("tutorId", tutorId)
                .get()
                .await()
            val courses = snapshot.toObjects(Course::class.java)
            Result.success(courses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getCourseById(courseId: String): Result<Course> {
        return try {
            val snapshot = coursesCollection.document(courseId).get().await()
            val course = snapshot.toObject(Course::class.java)
                ?: throw Exception("Course not found")
            Result.success(course)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCoursesByIds(courseIds: List<String>): Result<List<Course>> {
        return try {
            if (courseIds.isEmpty()) return Result.success(emptyList())

            val chunks = courseIds.chunked(10)
            val allCourses = mutableListOf<Course>()

            for (chunk in chunks) {
                val snapshot = coursesCollection
                    .whereIn("id", chunk)
                    .get()
                    .await()
                allCourses += snapshot.toObjects(Course::class.java)
            }

            Result.success(allCourses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCourse(course: Course): Result<Unit> {
        return try {
            firestore.collection("courses")
                .document(course.id)
                .set(course)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCourse(courseId: String): Result<Unit> {
        return try {
            firestore.collection("courses").document(courseId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
