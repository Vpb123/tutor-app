package com.mytutor.app.data.remote.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class QuizQuestion(
    val id: String = "",
    val quizId: String = "",
    val questionText: String = "",
    val imageUrl: String? = null,
    val options: List<String> = emptyList(),
    val questionType: QuestionType = QuestionType.MCQ,
    val marks: Int = 1,
    val correctAnswerIndex: Int? = null,          // For MCQ
    val correctAnswerIndices: List<Int>? = null,  // For MSQ
    val correctAnswerText: String? = null         // For FILL
)
