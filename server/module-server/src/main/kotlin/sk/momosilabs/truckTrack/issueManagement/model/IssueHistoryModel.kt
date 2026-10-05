package sk.momosilabs.truckTrack.issueManagement.model

import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.issueManagement.entity.IssuePriority
import sk.momosilabs.truckTrack.issueManagement.entity.IssueStatus
import java.time.OffsetDateTime
import java.util.UUID

sealed interface IssueHistoryModel {
    val id: UUID
    val issueId: Long
    val performedBy: AccountModel
    val createdAt: OffsetDateTime

    data class StatusChange(
        override val id: UUID,
        override val issueId: Long,
        override val performedBy: AccountModel,
        override val createdAt: OffsetDateTime,
        val statusFrom: IssueStatus?,
        val statusTo: IssueStatus,
    ) : IssueHistoryModel

    data class AssigneeChange(
        override val id: UUID,
        override val issueId: Long,
        override val performedBy: AccountModel,
        override val createdAt: OffsetDateTime,
        val assigneeFrom: AccountModel?,
        val assigneeTo: AccountModel?,
    ) : IssueHistoryModel

    data class Comment(
        override val id: UUID,
        override val issueId: Long,
        override val performedBy: AccountModel,
        override val createdAt: OffsetDateTime,
        val commentText: String,
    ) : IssueHistoryModel

    data class TitleChange(
        override val id: UUID,
        override val issueId: Long,
        override val performedBy: AccountModel,
        override val createdAt: OffsetDateTime,
        val titleFrom: String,
        val titleTo: String,
    ) : IssueHistoryModel

    data class DescriptionChange(
        override val id: UUID,
        override val issueId: Long,
        override val performedBy: AccountModel,
        override val createdAt: OffsetDateTime,
        val descriptionFrom: String,
        val descriptionTo: String,
    ) : IssueHistoryModel

    data class PriorityChange(
        override val id: UUID,
        override val issueId: Long,
        override val performedBy: AccountModel,
        override val createdAt: OffsetDateTime,
        val priorityFrom: IssuePriority,
        val priorityTo: IssuePriority,
    ) : IssueHistoryModel

    data class VehicleChange(
        override val id: UUID,
        override val issueId: Long,
        override val performedBy: AccountModel,
        override val createdAt: OffsetDateTime,
        val vehicleFrom: VehicleSnapshot,
        val vehicleTo: VehicleSnapshot,
    ) : IssueHistoryModel

    data class VehicleSnapshot(
        val id: Long,
        val licensePlate: String,
    )
}
