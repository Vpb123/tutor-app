package com.mytutor.app.data.remote.repository

import com.mytutor.app.data.remote.FirebaseService
import com.mytutor.app.data.remote.models.QuizResult
import kotlinx.coroutines.tasks.await

class QuizResultRepository {

    private val firestore = FirebaseService.firestore
    private val resultCollection = firestore.collection("quizResults")

    // ✅ Save quiz result (from domain layer)
    suspend fun saveQuizResult(result: QuizResult): Result<Unit> {
        return try {
            resultCollection.document(result.id).set(result).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Fetch result for a specific student + quiz
    suspend fun getQuizResult(quizId: String, studentId: String): Result<QuizResult> {
        return try {
            val resultId = "$quizId-$studentId"
            val snapshot = resultCollection.document(resultId).get().await()
            val result = snapshot.toObject(QuizResult::class.java)
                ?: throw Exception("No quiz result found")
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Get all results for tutor dashboard / analytics
    suspend fun getAllResultsForQuiz(quizId: String): Result<List<QuizResult>> {
        return try {
            val snapshot = resultCollection
                .whereEqualTo("quizId", quizId)
                .get()
                .await()
            val results = snapshot.toObjects(QuizResult::class.java)
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
