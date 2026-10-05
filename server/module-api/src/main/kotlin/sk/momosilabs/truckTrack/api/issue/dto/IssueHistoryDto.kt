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
    JsonSubTypes.Type(value = IssueHistoryDto.TitleChange::class, name = "TITLE_CHANGE"),
    JsonSubTypes.Type(value = IssueHistoryDto.DescriptionChange::class, name = "DESCRIPTION_CHANGE"),
    JsonSubTypes.Type(value = IssueHistoryDto.PriorityChange::class, name = "PRIORITY_CHANGE"),
    JsonSubTypes.Type(value = IssueHistoryDto.VehicleChange::class, name = "VEHICLE_CHANGE"),
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
        val assigneeFrom: AccountDto?,
        val assigneeTo: AccountDto?,
    ) : IssueHistoryDto

    data class Comment(
        override val id: UUID,
        override val performedBy: AccountDto,
        override val createdAt: OffsetDateTime,
        val commentText: String,
    ) : IssueHistoryDto

    data class TitleChange(
        override val id: UUID,
        override val performedBy: AccountDto,
        override val createdAt: OffsetDateTime,
        val titleFrom: String,
        val titleTo: String,
    ) : IssueHistoryDto

    data class DescriptionChange(
        override val id: UUID,
        override val performedBy: AccountDto,
        override val createdAt: OffsetDateTime,
        val descriptionFrom: String,
        val descriptionTo: String,
    ) : IssueHistoryDto

    data class PriorityChange(
        override val id: UUID,
        override val performedBy: AccountDto,
        override val createdAt: OffsetDateTime,
        val priorityFrom: IssuePriorityDto,
        val priorityTo: IssuePriorityDto,
    ) : IssueHistoryDto

    data class VehicleChange(
        override val id: UUID,
        override val performedBy: AccountDto,
        override val createdAt: OffsetDateTime,
        val vehicleFrom: IssueHistoryVehicleDto,
        val vehicleTo: IssueHistoryVehicleDto,
    ) : IssueHistoryDto
}
