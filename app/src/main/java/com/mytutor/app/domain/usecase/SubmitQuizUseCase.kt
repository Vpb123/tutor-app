package com.mytutor.app.domain.usecase

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
            var totalScore = 0

            for (question in questions) {
                val answer = answers[question.id]

                val awarded = when (question.questionType) {
                    QuestionType.MCQ -> {
                        answer?.toIntOrNull() == question.correctAnswerIndex
                    }

                    QuestionType.MSQ -> {
                        val correct = question.correctAnswerIndices?.sorted()
                        val userAnswer = answer
                            ?.split(",")
                            ?.mapNotNull { it.toIntOrNull() }
                            ?.sorted()
                        correct == userAnswer
                    }

                    QuestionType.FILL -> {
                        answer?.trim()?.lowercase() == question.correctAnswerText?.trim()?.lowercase()
                    }
                }

                if (awarded) {
                    totalScore += question.marks
                }
            }

            val resultId = "$quizId-$studentId"

            val quizResult = QuizResult(
                id = resultId,
                quizId = quizId,
                studentId = studentId,
                answers = answers,
                score = totalScore
            )

            val saveResult = quizResultRepository.saveQuizResult(quizResult)
            if (saveResult.isSuccess) {
                Result.success(quizResult)
            } else {
                Result.failure(saveResult.exceptionOrNull() ?: Exception("Failed to save quiz result"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
