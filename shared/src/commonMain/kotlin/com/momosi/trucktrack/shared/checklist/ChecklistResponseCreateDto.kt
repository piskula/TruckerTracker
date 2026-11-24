package com.momosi.trucktrack.shared.checklist

import kotlinx.serialization.Serializable

@Serializable
data class ChecklistResponseCreateDto(
    val truckId: Long,
    val trailerId: Long,
    val answers: List<ChecklistAnswerDto>,
)
