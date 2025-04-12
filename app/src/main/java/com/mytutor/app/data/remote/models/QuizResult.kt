package com.mytutor.app.data.remote.models

data class QuizResult(
    val id: String = "",
    val quizId: String = "",
    val studentId: String = "",

    val answers: Map<String, String> = emptyMap(),

    val score: Int = 0,
    val submittedAt: Long = System.currentTimeMillis()
)
