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

    val specialization: String? = "",
    val experienceYears: Int? = 0,

    @Exclude
    val isLoggedIn: Boolean = false
)