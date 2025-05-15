package com.mytutor.app.data.remote.models

sealed class ContentBlock {
    data class TextBlock(val text: String) : ContentBlock()
    data class ImageBlock(val imageUrl: String, val caption: String? = null) : ContentBlock()
}