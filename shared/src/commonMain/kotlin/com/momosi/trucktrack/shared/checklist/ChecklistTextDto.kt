package com.momosi.trucktrack.shared.checklist

import kotlinx.serialization.Serializable

@Serializable
data class ChecklistTextDto(
    val language: String,
    val text: String,
)
