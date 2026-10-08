package com.momosi.trucktrack.shared.issue

import kotlinx.serialization.Serializable

@Serializable
data class IssueHistoryVehicleDto(
    val id: Long,
    val licensePlate: String,
)
