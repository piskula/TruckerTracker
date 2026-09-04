package sk.momosilabs.truckTrack.issueManagement.controller

import sk.momosilabs.truckTrack.api.issue.dto.AccountDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueFilterDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueHistoryDto
import sk.momosilabs.truckTrack.api.issue.dto.IssuePriorityDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueStatusDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueUpdatedFieldDto
import sk.momosilabs.truckTrack.api.vehicle.dto.VehicleDto
import sk.momosilabs.truckTrack.api.vehicle.dto.VehicleTypeDto
import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.issueManagement.entity.IssuePriority
import sk.momosilabs.truckTrack.issueManagement.entity.IssueStatus
import sk.momosilabs.truckTrack.issueManagement.model.IssueHistoryModel
import sk.momosilabs.truckTrack.issueManagement.model.IssueModel
import sk.momosilabs.truckTrack.issueManagement.service.IssueListFilter
import sk.momosilabs.truckTrack.vehicle.model.VehicleModel

fun IssueStatusDto.toModel() = IssueStatus.valueOf(name)
fun IssuePriorityDto.toModel() = IssuePriority.valueOf(name)

fun IssueModel.toDto() = IssueDto(
    id = id,
    title = title,
    description = description,
    status = IssueStatusDto.valueOf(status.name),
    priority = IssuePriorityDto.valueOf(priority.name),
    vehicle = vehicle.toDto(),
    reportedBy = reportedBy.toDto(),
    assignedTo = assignedTo?.toDto(),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun IssueHistoryModel.toDto(): IssueHistoryDto = when (this) {
    is IssueHistoryModel.StatusChange -> IssueHistoryDto.StatusChange(
        id = id,
        performedBy = performedBy.toDto(),
        createdAt = createdAt,
        statusFrom = statusFrom?.let { IssueStatusDto.valueOf(it.name) },
        statusTo = IssueStatusDto.valueOf(statusTo.name),
    )

    is IssueHistoryModel.AssigneeChange -> IssueHistoryDto.AssigneeChange(
        id = id,
        performedBy = performedBy.toDto(),
        createdAt = createdAt,
    )

    is IssueHistoryModel.Comment -> IssueHistoryDto.Comment(
        id = id,
        performedBy = performedBy.toDto(),
        createdAt = createdAt,
        commentText = commentText,
    )

    is IssueHistoryModel.Update -> IssueHistoryDto.Update(
        id = id,
        performedBy = performedBy.toDto(),
        createdAt = createdAt,
        changedFields = changedFields.map { IssueUpdatedFieldDto.valueOf(it.name) },
    )
}

fun VehicleModel.toDto() = VehicleDto(
    id = id,
    licensePlate = licensePlate,
    make = make,
    model = model,
    type = VehicleTypeDto.valueOf(type.name),
)

fun AccountModel.toDto() = AccountDto(
    id = id,
    username = username,
    firstName = firstName,
    lastName = lastName,
)

fun IssueFilterDto.toModel() = IssueListFilter(
    statuses = statuses.map { it.toModel() },
    vehicleIds = vehicleIds,
    accountIds = accountIds,
)