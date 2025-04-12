package com.mytutor.app.domain.usecase

import com.google.gson.Gson
import com.mytutor.app.data.remote.models.QuizQuestion
import com.mytutor.app.data.remote.models.QuizResult
import com.mytutor.app.data.remote.models.QuestionType
import com.mytutor.app.data.remote.repository.QuizResultRepository

class SubmitQuizUseCase(
    private val quizResultRepository: QuizResultRepository
) {
    suspend operator fun invoke(
        quizId: String,
        studentId: String,
        questions: List<QuizQuestion>,
        answers: Map<String, String>
    ): Result<QuizResult> {
        return try {
            var score = 0

            for (question in questions) {
                val answer = answers[question.id]
                if (answer != null) {
                    when (question.questionType) {
                        QuestionType.MCQ -> {
                            if (answer.toIntOrNull() == question.correctAnswerIndex) score++
                        }
                        QuestionType.MSQ -> {
                            val correct = question.correctAnswerIndices?.sorted()
                            val userAnswer = Gson().fromJson(answer, Array<Int>::class.java)?.sorted()
                            if (correct == userAnswer) score++
                        }
                        QuestionType.FILL -> {
                            if (answer.trim().equals(question.correctAnswerText, ignoreCase = true)) score++
                        }
                    }
                }
            }

            val resultId = "$quizId-$studentId"
            val quizResult = QuizResult(
                id = resultId,
                quizId = quizId,
                studentId = studentId,
                answers = answers,
                score = score
            )

            val saveResult = quizResultRepository.saveQuizResult(quizResult)
            if (saveResult.isSuccess) Result.success(quizResult)
            else Result.failure(saveResult.exceptionOrNull() ?: Exception("Failed to save quiz result"))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
