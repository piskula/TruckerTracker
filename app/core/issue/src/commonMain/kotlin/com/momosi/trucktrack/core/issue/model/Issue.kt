package com.momosi.trucktrack.core.issue.model

import com.momosi.trucktrack.core.vehicle.model.Vehicle
import kotlin.time.Instant

data class Issue(
    val id: Long,
    val title: String,
    val description: String,
    val status: IssueStatus,
    val priority: IssuePriority,
    val vehicle: Vehicle?,
    val reportedBy: Account?,
    val assignedTo: Account?,
    val repairType: RepairType?,
    val vehicleSystem: VehicleSystem?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
