package com.mytutor.app.data.remote.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Quiz(
    val id: String = "",
    val courseId: String = "",
    val title: String = "",
    val description: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val totalMarks: Int = 0,
    val passPercentage: Int = 50
)
