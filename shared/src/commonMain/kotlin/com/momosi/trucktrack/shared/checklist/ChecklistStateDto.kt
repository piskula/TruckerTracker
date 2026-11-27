package com.momosi.trucktrack.shared.checklist

import kotlinx.serialization.Serializable

@Serializable
enum class ChecklistStateDto {
    NOT_REQUIRED,
    PENDING,
    COMPLETED,
}
