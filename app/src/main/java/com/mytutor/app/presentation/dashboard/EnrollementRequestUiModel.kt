package com.mytutor.app.presentation.dashboard

data class EnrolmentRequestUiModel(
    val enrolmentId: String,
    val studentName: String,
    val courseTitle: String,
    val requestedAt: Long
)