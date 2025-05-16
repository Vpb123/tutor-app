package com.mytutor.app.data.remote.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class LearningMaterial(
    val id: String = "",
    val type: MaterialType = MaterialType.TEXT,
    val content: String = "",
    val caption: String? = null
)
