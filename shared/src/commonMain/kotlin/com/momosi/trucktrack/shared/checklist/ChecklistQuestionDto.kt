package com.momosi.trucktrack.shared.checklist

import kotlinx.serialization.Serializable

@Serializable
data class ChecklistQuestionDto(
    val identifier: String,
    val text: String,
)
