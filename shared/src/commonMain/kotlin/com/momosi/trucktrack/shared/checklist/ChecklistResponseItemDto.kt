package com.momosi.trucktrack.shared.checklist

import kotlinx.serialization.Serializable

@Serializable
data class ChecklistResponseItemDto(
    val identifier: String,
    val texts: List<ChecklistTextDto>,
    val answer: Boolean,
)
