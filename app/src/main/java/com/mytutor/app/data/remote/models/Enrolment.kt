package com.mytutor.app.data.remote.models


import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Enrolment(
    val id: String = "",
    val courseId: String = "",
    val studentId: String = "",
    val status: EnrolmentStatus = EnrolmentStatus.PENDING,
    val requestedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)