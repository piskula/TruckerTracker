package sk.momosilabs.truckTrack.api.issue.dto

import sk.momosilabs.truckTrack.api.vehicle.dto.VehicleDto
import java.time.OffsetDateTime

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
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)