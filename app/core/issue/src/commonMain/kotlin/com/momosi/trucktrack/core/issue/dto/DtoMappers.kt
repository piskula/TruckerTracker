package com.momosi.trucktrack.core.issue.dto

import com.momosi.trucktrack.core.issue.model.Account
import com.momosi.trucktrack.core.issue.model.Issue
import com.momosi.trucktrack.core.issue.model.IssueAttachment
import com.momosi.trucktrack.core.issue.model.IssueCreate
import com.momosi.trucktrack.core.issue.model.IssueHistory
import com.momosi.trucktrack.core.issue.model.IssuePriority
import com.momosi.trucktrack.core.issue.model.IssueStatus
import com.momosi.trucktrack.core.issue.model.IssueUpdate
import com.momosi.trucktrack.core.issue.model.IssueUpdatedField
import com.momosi.trucktrack.core.vehicle.dto.toVehicle
import com.momosi.trucktrack.shared.issue.AccountDto
import com.momosi.trucktrack.shared.issue.IssueAttachmentDto
import com.momosi.trucktrack.shared.issue.IssueCreateDto
import com.momosi.trucktrack.shared.issue.IssueDto
import com.momosi.trucktrack.shared.issue.IssueFilterDto
import com.momosi.trucktrack.shared.issue.IssueHistoryDto
import com.momosi.trucktrack.shared.issue.IssuePriorityDto
import com.momosi.trucktrack.shared.issue.IssueStatusDto
import com.momosi.trucktrack.shared.issue.IssueUpdateDto
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal fun AccountDto.toAccount(): Account = Account(
    id = id.toString(),
    username = username,
    firstName = firstName,
    lastName = lastName,
)

internal fun IssueDto.toIssue(): Issue = Issue(
    id = id,
    title = title,
    description = description,
    status = IssueStatus.fromApiValue(status.name),
    priority = IssuePriority.fromApiValue(priority.name),
    vehicle = vehicle.toVehicle(),
    reportedBy = reportedBy.toAccount(),
    assignedTo = assignedTo?.toAccount(),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun IssueCreate.toDto(): IssueCreateDto = IssueCreateDto(
    vehicleId = vehicleId,
    title = title,
    description = description,
    priority = IssuePriorityDto.valueOf(priority.toApiValue()),
)

internal fun IssueUpdate.toDto(): IssueUpdateDto = IssueUpdateDto(
    vehicleId = vehicleId,
    title = title,
    description = description,
    priority = IssuePriorityDto.valueOf(priority.toApiValue()),
)

@OptIn(ExperimentalUuidApi::class)
internal fun List<IssueStatus>.toFilterDto(vehicleIds: List<Long>, accountIds: List<String>): IssueFilterDto = IssueFilterDto(
    statuses = map { IssueStatusDto.valueOf(it.toApiValue()) },
    vehicleIds = vehicleIds,
    accountIds = accountIds.map { Uuid.parse(it) },
)

internal fun IssueAttachmentDto.toIssueAttachment(): IssueAttachment = IssueAttachment(
    id = id,
    filename = filename,
    contentType = contentType,
    sizeBytes = sizeBytes,
    uploadedBy = uploadedBy.toAccount(),
    uploadedAt = uploadedAt,
)

internal fun IssueHistoryDto.toIssueHistory(): IssueHistory = when (this) {
    is IssueHistoryDto.StatusChange -> IssueHistory.StatusChange(
        id = id.toString(),
        performedBy = performedBy.toAccount(),
        createdAt = createdAt,
        statusFrom = statusFrom?.let { IssueStatus.fromApiValue(it.name) },
        statusTo = IssueStatus.fromApiValue(statusTo.name),
    )

    is IssueHistoryDto.AssigneeChange -> IssueHistory.AssigneeChange(
        id = id.toString(),
        performedBy = performedBy.toAccount(),
        createdAt = createdAt,
    )

    is IssueHistoryDto.Comment -> IssueHistory.Comment(
        id = id.toString(),
        performedBy = performedBy.toAccount(),
        createdAt = createdAt,
        commentText = commentText,
    )

    is IssueHistoryDto.Update -> IssueHistory.Update(
        id = id.toString(),
        performedBy = performedBy.toAccount(),
        createdAt = createdAt,
        changedFields = changedFields.map { IssueUpdatedField.fromApiValue(it.name) },
    )
}
