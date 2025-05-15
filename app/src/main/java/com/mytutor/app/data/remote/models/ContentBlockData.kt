package com.mytutor.app.data.remote.models

data class ContentBlockData(
    val type: String = "text",
    val text: String? = null,
    val imageUrl: String? = null,
    val caption: String? = null
)