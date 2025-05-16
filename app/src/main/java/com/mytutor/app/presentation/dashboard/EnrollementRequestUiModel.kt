package com.mytutor.app.presentation.dashboard

import com.mytutor.app.data.remote.models.EnrolmentStatus

data class EnrolmentRequestUiModel(
    val enrolmentId: String,
    val studentName: String,
    val courseTitle: String,
    val requestedAt: Long,
    val courseId: String,
    val studentId: String,
    val tutorName: String? = null,
    val status: EnrolmentStatus? = null,
    val description: String? = null,
    val subject: String? = null
)