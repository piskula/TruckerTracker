package com.momosi.trucktrack.shared.checklist

import kotlinx.serialization.Serializable

@Serializable
data class ChecklistVehicleSnapshotDto(
    val id: Long,
    val licensePlate: String,
)
