package com.mytutor.app.data.remote.repository

import com.mytutor.app.data.remote.models.Enrolment
import com.mytutor.app.data.remote.models.EnrolmentStatus
import com.mytutor.app.data.remote.FirebaseService
import kotlinx.coroutines.tasks.await

class EnrolmentRepository {

    private val firestore = FirebaseService.firestore
    private val enrolmentCollection = firestore.collection("enrolments")

    suspend fun requestEnrolment(courseId: String, studentId: String): Result<Unit> {
        return try {
            val enrolmentId = "${courseId}_$studentId"
            val enrolment = Enrolment(
                id = enrolmentId,
                courseId = courseId,
                studentId = studentId,
                status = EnrolmentStatus.PENDING
            )
            enrolmentCollection.document(enrolmentId).set(enrolment).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEnrolmentStatus(
        courseId: String,
        studentId: String,
        status: EnrolmentStatus
    ): Result<Unit> {
        return try {
            val enrolmentId = "${courseId}_$studentId"
            enrolmentCollection.document(enrolmentId)
                .update("status", status.name, "updatedAt", System.currentTimeMillis())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEnrolmentsByCourse(courseId: String): Result<List<Enrolment>> {
        return try {
            val snapshot = enrolmentCollection
                .whereEqualTo("courseId", courseId)
                .get()
                .await()
            val enrolments = snapshot.toObjects(Enrolment::class.java)
            Result.success(enrolments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingEnrolmentsForTutorCourses(courseIds: List<String>): Result<List<Enrolment>> {
        return try {
            if (courseIds.isEmpty()) return Result.success(emptyList())

            val tasks = courseIds.map { courseId ->
                enrolmentCollection
                    .whereEqualTo("courseId", courseId)
                    .whereEqualTo("status", EnrolmentStatus.PENDING.name)
                    .get()
            }

            val results = tasks.map { it.await() }.flatMap { it.toObjects(Enrolment::class.java) }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEnrolmentsByStudent(studentId: String): Result<List<Enrolment>> {
        return try {
            val snapshot = enrolmentCollection
                .whereEqualTo("studentId", studentId)
                .get()
                .await()
            val enrolments = snapshot.toObjects(Enrolment::class.java)
            Result.success(enrolments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAcceptedCoursesForStudent(studentId: String): Result<List<String>> {
        return try {
            val snapshot = enrolmentCollection
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("status", EnrolmentStatus.ACCEPTED.name)
                .get()
                .await()
            val courseIds = snapshot.toObjects(Enrolment::class.java).map { it.courseId }
            Result.success(courseIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

