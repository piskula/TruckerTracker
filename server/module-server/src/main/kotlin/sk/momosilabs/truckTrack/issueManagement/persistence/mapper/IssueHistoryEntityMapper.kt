package sk.momosilabs.truckTrack.issueManagement.persistence.mapper

import sk.momosilabs.truckTrack.account.persistence.mapper.toModel
import sk.momosilabs.truckTrack.issueManagement.entity.IssueHistoryEntity
import sk.momosilabs.truckTrack.issueManagement.entity.IssueHistoryEventType
import sk.momosilabs.truckTrack.issueManagement.entity.IssueUpdatedField
import sk.momosilabs.truckTrack.issueManagement.model.IssueHistoryModel
import sk.momosilabs.truckTrack.util.toUtcOffsetDateTime

fun IssueHistoryEntity.toModel(): IssueHistoryModel = when (type) {
    IssueHistoryEventType.STATUS_CHANGE -> IssueHistoryModel.StatusChange(
        id = id,
        issueId = issue.id,
        performedBy = performedBy.toModel(),
        createdAt = createdAtUtc.toUtcOffsetDateTime(),
        statusFrom = statusFrom,
        statusTo = requireNotNull(statusTo) { "statusTo is required for STATUS_CHANGE history id=$id" },
    )

    IssueHistoryEventType.ASSIGNEE_CHANGE -> IssueHistoryModel.AssigneeChange(
        id = id,
        issueId = issue.id,
        performedBy = performedBy.toModel(),
        createdAt = createdAtUtc.toUtcOffsetDateTime(),
    )

    IssueHistoryEventType.COMMENT -> IssueHistoryModel.Comment(
        id = id,
        issueId = issue.id,
        performedBy = performedBy.toModel(),
        createdAt = createdAtUtc.toUtcOffsetDateTime(),
        commentText = requireNotNull(commentText) { "commentText is required for COMMENT history id=$id" },
    )

    IssueHistoryEventType.UPDATE -> IssueHistoryModel.Update(
        id = id,
        issueId = issue.id,
        performedBy = performedBy.toModel(),
        createdAt = createdAtUtc.toUtcOffsetDateTime(),
        changedFields = changedFields?.takeIf { it.isNotEmpty() }?.split(",")?.map { IssueUpdatedField.valueOf(it) }.orEmpty(),
    )
}
