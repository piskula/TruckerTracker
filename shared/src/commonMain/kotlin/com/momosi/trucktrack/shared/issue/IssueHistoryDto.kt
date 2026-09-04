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
    @SerialName("UPDATE")
    data class Update(
        override val id: Uuid,
        override val performedBy: AccountDto,
        override val createdAt: Instant,
        val changedFields: List<IssueUpdatedFieldDto>,
    ) : IssueHistoryDto
}
