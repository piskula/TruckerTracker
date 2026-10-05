package com.momosi.trucktrack.shared.issue

import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalUuidApi::class)
@Serializable
sealed interface IssueHistoryDto {
    val id: Uuid
    val performedBy: AccountDto
    val createdAt: Instant

    @Serializable
    @SerialName("STATUS_CHANGE")
    data class StatusChange(
        override val id: Uuid,
        override val performedBy: AccountDto,
        override val createdAt: Instant,
        val statusFrom: IssueStatusDto?,
        val statusTo: IssueStatusDto,
    ) : IssueHistoryDto

    @Serializable
    @SerialName("ASSIGNEE_CHANGE")
    data class AssigneeChange(
        override val id: Uuid,
        override val performedBy: AccountDto,
        override val createdAt: Instant,
        val assigneeFrom: AccountDto? = null,
        val assigneeTo: AccountDto? = null,
    ) : IssueHistoryDto

    @Serializable
    @SerialName("COMMENT")
    data class Comment(
        override val id: Uuid,
        override val performedBy: AccountDto,
        override val createdAt: Instant,
        val commentText: String,
    ) : IssueHistoryDto

    @Serializable
    @SerialName("TITLE_CHANGE")
    data class TitleChange(
        override val id: Uuid,
        override val performedBy: AccountDto,
        override val createdAt: Instant,
        val titleFrom: String,
        val titleTo: String,
    ) : IssueHistoryDto

    @Serializable
    @SerialName("DESCRIPTION_CHANGE")
    data class DescriptionChange(
        override val id: Uuid,
        override val performedBy: AccountDto,
        override val createdAt: Instant,
        val descriptionFrom: String,
        val descriptionTo: String,
    ) : IssueHistoryDto

    @Serializable
    @SerialName("PRIORITY_CHANGE")
    data class PriorityChange(
        override val id: Uuid,
        override val performedBy: AccountDto,
        override val createdAt: Instant,
        val priorityFrom: IssuePriorityDto,
        val priorityTo: IssuePriorityDto,
    ) : IssueHistoryDto

    @Serializable
    @SerialName("VEHICLE_CHANGE")
    data class VehicleChange(
        override val id: Uuid,
        override val performedBy: AccountDto,
        override val createdAt: Instant,
        val vehicleFrom: IssueHistoryVehicleDto,
        val vehicleTo: IssueHistoryVehicleDto,
    ) : IssueHistoryDto
}
