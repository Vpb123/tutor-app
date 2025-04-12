package com.mytutor.app.data.remote.models

data class CourseAnalytics(
    val courseId: String,
    val courseTitle: String,
    val enrolledCount: Int,
    val averageProgress: Float,
    val completedCount: Int,
    val passedQuizCount: Int
)