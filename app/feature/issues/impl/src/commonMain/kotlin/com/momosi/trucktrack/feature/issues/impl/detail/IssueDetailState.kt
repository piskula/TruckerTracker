package com.momosi.trucktrack.feature.issues.impl.detail

import androidx.compose.runtime.Immutable
import com.momosi.trucktrack.core.issue.model.IssueHistoryType
import com.momosi.trucktrack.core.issue.model.IssuePriority
import com.momosi.trucktrack.core.issue.model.IssueStatus
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

@Immutable
data class IssueDetailState(
    val issueId: Long,
    val content: IssueDetailContent = IssueDetailContent.Loading,
    val photosContent: IssuePhotosContent = IssuePhotosContent.Loading,
    val commentText: String = "",
    val isSendingComment: Boolean = false,
    val mechanicAction: MechanicActionType? = null,
    val isMechanicActionLoading: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val canDeletePhotos: Boolean = false,
    val deletingPhotoIds: ImmutableSet<Long> = persistentSetOf(),
    val statusChanged: Boolean = false,
    val canCancelIssue: Boolean = false,
    val isCancellingIssue: Boolean = false,
)

@Immutable
sealed interface IssueDetailContent {
    data object Loading : IssueDetailContent
    data object Error : IssueDetailContent

    @Immutable
    data class Loaded(val issue: IssueUi, val history: ImmutableList<IssueHistoryUi>) : IssueDetailContent
}

@Immutable
sealed interface IssuePhotosContent {
    data object Loading : IssuePhotosContent

    @Immutable
    data class Loaded(val items: ImmutableList<PhotoItem> = persistentListOf()) : IssuePhotosContent
}

@Immutable
data class IssueUi(
    val id: Long,
    val title: String,
    val description: String,
    val status: IssueStatus,
    val priority: IssuePriority,
    val vehicleLabel: String,
    val vehicleType: VehicleType?,
    val reportedByName: String,
    val assignedToName: String,
    val createdAtFormatted: String,
)

@Immutable
data class IssueHistoryUi(val id: String, val type: IssueHistoryType, val statusTo: IssueStatus?, val performedByName: String?, val createdAtFormatted: String, val commentText: String?)

@Immutable
data class PhotoItem(val id: Long, val filename: String, val url: String)

enum class MechanicActionType {
    StartWorking,
    ResolveIssue,
    Reassign,
}
