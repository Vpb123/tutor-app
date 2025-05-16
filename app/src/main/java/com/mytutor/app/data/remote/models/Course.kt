package com.mytutor.app.data.remote.models

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Course(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val subject: CourseSubject = CourseSubject.OTHER,
    val tutorId: String = "",
    val lessonCount: Int = 0,
    val durationInHours: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    @PropertyName("published")
    val isPublished: Boolean = false
)
