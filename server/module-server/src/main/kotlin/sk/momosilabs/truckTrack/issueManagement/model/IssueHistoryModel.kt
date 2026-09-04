package sk.momosilabs.truckTrack.issueManagement.model

import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.issueManagement.entity.IssueStatus
import sk.momosilabs.truckTrack.issueManagement.entity.IssueUpdatedField
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
    ) : IssueHistoryModel

    data class Comment(
        override val id: UUID,
        override val issueId: Long,
        override val performedBy: AccountModel,
        override val createdAt: OffsetDateTime,
        val commentText: String,
    ) : IssueHistoryModel

    data class Update(
        override val id: UUID,
        override val issueId: Long,
        override val performedBy: AccountModel,
        override val createdAt: OffsetDateTime,
        val changedFields: List<IssueUpdatedField>,
    ) : IssueHistoryModel
}
