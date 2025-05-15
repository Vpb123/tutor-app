package com.mytutor.app.data.remote.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Lesson(
    val id: String = "",
    val courseId: String = "",
    val title: String = "",
    val pages: List<LessonPage> = emptyList(),
    val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
