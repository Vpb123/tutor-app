package com.mytutor.app.data.remote.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class LearningMaterial(
    val id: String = "",                      // Unique ID or auto-generated
    val type: MaterialType = MaterialType.TEXT,
    val content: String = "",                // Can be plain text or URL (for media)
    val caption: String? = null              // Optional description for media
)
