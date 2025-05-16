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

    val academicYear: String? = null,        // e.g., "1st Year", "Final Year"
    val course: String? = null,              // e.g., "BSc Computer Science"
    val university: String? = null,
    val studentId: String? = null,

    val phoneNumber: String? = null,
    val address: String? = null,

    val skills: List<String>? = null,        // e.g., ["Java", "Teamwork"]
    val interests: List<String>? = null,     // e.g., ["AI", "Fitness"]
    val enrollmentDate: String? = null,      // ISO-8601 format recommended

    val specialization: String? = null,
    val experienceYears: Int? = null,

    @Exclude
    val isLoggedIn: Boolean = false
)
