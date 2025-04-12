package com.mytutor.app.data.remote.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class LessonProgress(
    val id: String = "",                   // Firestore doc ID (optional)
    val studentId: String = "",            // UID of the student
    val courseId: String = "",             // Course the lesson belongs to
    val lessonId: String = "",             // Completed lesson ID
    val completedAt: Long = System.currentTimeMillis()
)

