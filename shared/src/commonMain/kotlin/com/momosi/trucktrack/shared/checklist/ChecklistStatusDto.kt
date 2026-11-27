package com.momosi.trucktrack.shared.checklist

import kotlinx.serialization.Serializable

@Serializable
data class ChecklistStatusDto(
    val state: ChecklistStateDto,
    val response: ChecklistResponseDto? = null,
)
