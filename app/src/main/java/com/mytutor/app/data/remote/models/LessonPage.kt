package com.mytutor.app.data.remote.models

import java.util.UUID
data class LessonPage(
    val id: String = UUID.randomUUID().toString(),
    val contentBlocks: List<ContentBlockData> = emptyList(),
    val embeddedMaterials: List<LearningMaterial> = emptyList()
)