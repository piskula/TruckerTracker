package com.momosi.trucktrack.shared.checklist

import kotlinx.serialization.Serializable

@Serializable
data class ChecklistAnswerDto(
    val identifier: String,
    val answer: Boolean,
)
