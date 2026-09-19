package com.momosi.trucktrack.shared.issue

import kotlinx.serialization.Serializable

@Serializable
data class StartIssueDto(
    val repairType: RepairTypeDto,
    val vehicleSystem: VehicleSystemDto,
)
