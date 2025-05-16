package com.mytutor.app.data.remote.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class LessonProgress(
    val id: String = "",
    val studentId: String = "",
    val courseId: String = "",
    val lessonId: String = "",
    val completedAt: Long = System.currentTimeMillis()
)

