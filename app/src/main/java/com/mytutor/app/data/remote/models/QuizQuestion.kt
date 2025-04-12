package com.mytutor.app.data.remote.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class QuizQuestion(
    val id: String = "",
    val quizId: String = "",
    val questionText: String = "",
    val imageUrl: String? = null,             // Optional image (Firebase Storage)
    val options: List<String> = emptyList(),  // Shown for MCQ/MSQ
    val questionType: QuestionType = QuestionType.MCQ,

    // Depending on questionType, only one of the below is used:
    val correctAnswerIndex: Int? = null,          // For MCQ
    val correctAnswerIndices: List<Int>? = null,  // For MSQ
    val correctAnswerText: String? = null         // For FILL
)
