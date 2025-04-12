package com.mytutor.app.data.remote.repository

import com.mytutor.app.data.remote.models.Quiz
import com.mytutor.app.data.remote.models.QuizQuestion
import com.mytutor.app.data.remote.FirebaseService
import kotlinx.coroutines.tasks.await

class QuizRepository {

    private val firestore = FirebaseService.firestore
    private val quizCollection = firestore.collection("quizzes")
    private val questionCollection = firestore.collection("quizQuestions")

    suspend fun createQuiz(quiz: Quiz): Result<String> {
        return try {
            val docRef = quizCollection.document()
            val quizWithId = quiz.copy(id = docRef.id)
            docRef.set(quizWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addQuestionToQuiz(question: QuizQuestion): Result<Unit> {
        return try {
            val docRef = if (question.id.isBlank()) {
                questionCollection.document()
            } else {
                questionCollection.document(question.id)
            }

            val questionWithId = question.copy(id = docRef.id)
            docRef.set(questionWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getQuizByCourseId(courseId: String): Result<Quiz?> {
        return try {
            val snapshot = quizCollection
                .whereEqualTo("courseId", courseId)
                .limit(1)
                .get()
                .await()
            val quiz = snapshot.documents.firstOrNull()?.toObject(Quiz::class.java)
            Result.success(quiz)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getQuestionsByQuizId(quizId: String): Result<List<QuizQuestion>> {
        return try {
            val snapshot = questionCollection
                .whereEqualTo("quizId", quizId)
                .get()
                .await()
            val questions = snapshot.toObjects(QuizQuestion::class.java)
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updateQuiz(quiz: Quiz): Result<Unit> {
        return try {
            quizCollection.document(quiz.id).set(quiz).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun deleteQuiz(quizId: String): Result<Unit> {
        return try {
            quizCollection.document(quizId).delete().await()
            // Optional: delete associated questions too
            val questionsSnapshot = questionCollection.whereEqualTo("quizId", quizId).get().await()
            for (doc in questionsSnapshot.documents) {
                doc.reference.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
