package sk.momosilabs.truckTrack.issueManagement.persistence.mapper

import sk.momosilabs.truckTrack.account.persistence.mapper.toModel
import sk.momosilabs.truckTrack.issueManagement.entity.IssueEntity
import sk.momosilabs.truckTrack.issueManagement.model.IssueModel
import sk.momosilabs.truckTrack.vehicle.persistence.mapper.toModel
import sk.momosilabs.truckTrack.util.toUtcOffsetDateTime

fun IssueEntity.toModel() = IssueModel(
    id = id,
    title = title,
    description = description,
    status = status,
    priority = priority,
    vehicle = vehicle.toModel(),
    reportedBy = reportedBy.toModel(),
    assignedTo = assignedTo?.toModel(),
    repairType = repairType,
    vehicleSystem = vehicleSystem,
    createdAt = createdAtUtc.toUtcOffsetDateTime(),
    updatedAt = updatedAtUtc.toUtcOffsetDateTime(),
)
