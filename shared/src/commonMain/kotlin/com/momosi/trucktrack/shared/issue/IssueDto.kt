package com.momosi.trucktrack.shared.issue

import com.momosi.trucktrack.shared.vehicle.VehicleDto
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class IssueDto(
    val id: Long,
    val title: String,
    val description: String,
    val status: IssueStatusDto,
    val priority: IssuePriorityDto,
    val vehicle: VehicleDto,
    val reportedBy: AccountDto,
    val assignedTo: AccountDto?,
    val repairType: RepairTypeDto?,
    val vehicleSystem: VehicleSystemDto?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
