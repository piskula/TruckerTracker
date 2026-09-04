package sk.momosilabs.truckTrack.api.issue.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.OffsetDateTime
import java.util.UUID

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = IssueHistoryDto.StatusChange::class, name = "STATUS_CHANGE"),
    JsonSubTypes.Type(value = IssueHistoryDto.AssigneeChange::class, name = "ASSIGNEE_CHANGE"),
    JsonSubTypes.Type(value = IssueHistoryDto.Comment::class, name = "COMMENT"),
    JsonSubTypes.Type(value = IssueHistoryDto.Update::class, name = "UPDATE"),
)
sealed interface IssueHistoryDto {
    val id: UUID
    val performedBy: AccountDto
    val createdAt: OffsetDateTime

    data class StatusChange(
        override val id: UUID,
        override val performedBy: AccountDto,
        override val createdAt: OffsetDateTime,
        val statusFrom: IssueStatusDto?,
        val statusTo: IssueStatusDto,
    ) : IssueHistoryDto

    data class AssigneeChange(
        override val id: UUID,
        override val performedBy: AccountDto,
        override val createdAt: OffsetDateTime,
    ) : IssueHistoryDto

    data class Comment(
        override val id: UUID,
        override val performedBy: AccountDto,
        override val createdAt: OffsetDateTime,
        val commentText: String,
    ) : IssueHistoryDto

    data class Update(
        override val id: UUID,
        override val performedBy: AccountDto,
        override val createdAt: OffsetDateTime,
        val changedFields: List<IssueUpdatedFieldDto>,
    ) : IssueHistoryDto
}
