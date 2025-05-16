package com.mytutor.app.data.remote.models

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val bio: String = "",
    val profileImageUrl: String = "",
    val role: UserRole = UserRole.STUDENT,

    val phoneNumber: String? = null,
    val address: String? = null,

    val specialization: String? = null,
    val experienceYears: Int? = null,

    @Exclude
    val isLoggedIn: Boolean = false
)
