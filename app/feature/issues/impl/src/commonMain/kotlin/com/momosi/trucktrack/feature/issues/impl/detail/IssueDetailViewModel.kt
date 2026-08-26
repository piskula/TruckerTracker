package com.momosi.trucktrack.feature.issues.impl.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.momosi.trucktrack.core.common.error.ErrorReporter
import com.momosi.trucktrack.core.common.formatter.DateFormatter
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.issue.IssueAttachmentRepository
import com.momosi.trucktrack.core.issue.IssueRepository
import com.momosi.trucktrack.core.issue.model.Issue
import com.momosi.trucktrack.core.issue.model.IssueHistory
import com.momosi.trucktrack.core.issue.model.IssueStatus
import com.momosi.trucktrack.user.UserRepository
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IssueDetailViewModel(
    private val issueId: Long,
    private val issueRepository: IssueRepository,
    private val issueAttachmentRepository: IssueAttachmentRepository,
    private val userRepository: UserRepository,
    private val dateFormatter: DateFormatter,
    private val errorReporter: ErrorReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(IssueDetailState(issueId = issueId))
    val state: StateFlow<IssueDetailState> = _state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = IssueDetailState(issueId = issueId),
    )

    init {
        loadIssueDetail()
    }

    fun onAction(action: IssueDetailAction) {
        Logger.i("Action:IssueDetail", action.toString())
        when (action) {
            is IssueDetailAction.UpdateComment -> _state.update { it.copy(commentText = action.text) }
            is IssueDetailAction.UploadPhoto -> uploadPhoto(action.file)
            is IssueDetailAction.DeletePhoto -> deletePhoto(action.attachmentId)
            is IssueDetailAction.SendComment -> sendComment()
            is IssueDetailAction.Retry -> loadIssueDetail()
            is IssueDetailAction.StartWorking -> startWorking()
            is IssueDetailAction.ResolveIssue -> resolveIssue()
            is IssueDetailAction.ReassignToMe -> reassignToMe()
        }
    }

    private fun loadIssueDetail() {
        _state.update {
            it.copy(
                content = IssueDetailContent.Loading,
                photosContent = IssuePhotosContent.Loading,
            )
        }

        viewModelScope.launch {
            val issueDeferred = async { issueRepository.getIssue(issueId) }
            val historyDeferred = async { fetchHistory() }

            val issueResult = issueDeferred.await()
            val issue = issueResult.getOrNull()
            if (issue == null) {
                _state.update { it.copy(content = IssueDetailContent.Error) }
                issueResult.onFailure { errorReporter.report(it) }
                return@launch
            }
            _state.update {
                it.copy(
                    content = IssueDetailContent.Loaded(issue.toUi(), historyDeferred.await()),
                    mechanicAction = computeMechanicAction(issue),
                    canDeletePhotos = computeCanDeletePhotos(issue),
                )
            }
        }

        loadPhotos()
    }

    private fun refreshHistory() {
        viewModelScope.launch {
            val history = fetchHistory()
            _state.update { current ->
                val content = current.content
                if (content is IssueDetailContent.Loaded) {
                    current.copy(content = content.copy(history = history))
                } else {
                    current
                }
            }
        }
    }

    private suspend fun fetchHistory(): ImmutableList<IssueHistoryUi> = issueRepository.getIssueHistory(issueId)
        .getOrNull()?.content
        ?.map { it.toUi() }
        ?.toImmutableList() ?: persistentListOf()

    private fun loadPhotos() {
        _state.update { it.copy(photosContent = IssuePhotosContent.Loading) }
        viewModelScope.launch {
            val items = issueAttachmentRepository.getPhotos(issueId)
                .getOrNull()?.content
                ?.map { attachment ->
                    PhotoItem(
                        id = attachment.id,
                        filename = attachment.filename,
                        url = issueAttachmentRepository.getPhotoUrl(issueId, attachment.id),
                    )
                }
                ?.toImmutableList() ?: persistentListOf()
            _state.update { it.copy(photosContent = IssuePhotosContent.Loaded(items)) }
        }
    }

    private fun computeMechanicAction(issue: Issue): MechanicActionType? {
        val user = userRepository.user.value ?: return null
        if (!user.isMechanic) return null
        return when {
            issue.status == IssueStatus.Open && issue.assignedTo?.id != user.id -> MechanicActionType.StartWorking
            issue.status == IssueStatus.InProgress && issue.assignedTo?.id == user.id -> MechanicActionType.ResolveIssue
            issue.status == IssueStatus.InProgress && issue.assignedTo?.id != user.id -> MechanicActionType.Reassign
            else -> null
        }
    }

    private fun computeCanDeletePhotos(issue: Issue): Boolean {
        val user = userRepository.user.value ?: return false
        if (!user.isDriver) return false
        return issue.status == IssueStatus.Open && issue.reportedBy?.id == user.id
    }

    private fun onMechanicActionFailure(error: Throwable) {
        _state.update { it.copy(isMechanicActionLoading = false) }
        errorReporter.report(error)
    }

    private fun startWorking() {
        if (_state.value.isMechanicActionLoading) return
        _state.update { it.copy(isMechanicActionLoading = true) }
        viewModelScope.launch {
            issueRepository.startIssue(issueId)
                .onSuccess { issue ->
                    _state.update {
                        it.copy(
                            content = it.withUpdatedIssue(issue),
                            mechanicAction = computeMechanicAction(issue),
                            isMechanicActionLoading = false,
                            statusChanged = true,
                        )
                    }
                    refreshHistory()
                }
                .onFailure(::onMechanicActionFailure)
        }
    }

    private fun resolveIssue() {
        if (_state.value.isMechanicActionLoading) return
        _state.update { it.copy(isMechanicActionLoading = true) }
        viewModelScope.launch {
            issueRepository.resolveIssue(issueId)
                .onSuccess { issue ->
                    _state.update {
                        it.copy(
                            content = it.withUpdatedIssue(issue),
                            mechanicAction = computeMechanicAction(issue),
                            isMechanicActionLoading = false,
                            statusChanged = true,
                        )
                    }
                    refreshHistory()
                }
                .onFailure(::onMechanicActionFailure)
        }
    }

    private fun reassignToMe() {
        if (_state.value.isMechanicActionLoading) return
        _state.update { it.copy(isMechanicActionLoading = true) }
        viewModelScope.launch {
            issueRepository.assignIssue(issueId)
                .onSuccess { issue ->
                    _state.update {
                        it.copy(
                            content = it.withUpdatedIssue(issue),
                            mechanicAction = computeMechanicAction(issue),
                            isMechanicActionLoading = false,
                        )
                    }
                    refreshHistory()
                }
                .onFailure(::onMechanicActionFailure)
        }
    }

    private fun sendComment() {
        val text = _state.value.commentText.trim()
        if (text.isEmpty() || _state.value.isSendingComment) return

        _state.update { it.copy(isSendingComment = true) }

        viewModelScope.launch {
            issueRepository.addComment(issueId, text)
                .onSuccess {
                    _state.update { it.copy(commentText = "", isSendingComment = false) }
                    refreshHistory()
                }
                .onFailure {
                    _state.update { state -> state.copy(isSendingComment = false) }
                    errorReporter.report(it)
                }
        }
    }

    private fun IssueDetailState.withUpdatedIssue(issue: Issue): IssueDetailContent.Loaded {
        val existingHistory = (content as? IssueDetailContent.Loaded)?.history ?: persistentListOf()
        return IssueDetailContent.Loaded(issue.toUi(), existingHistory)
    }

    private fun uploadPhoto(file: PlatformFile) {
        if (_state.value.isUploadingPhoto) return
        _state.update { it.copy(isUploadingPhoto = true) }
        viewModelScope.launch {
            issueAttachmentRepository.uploadPhoto(
                issueId = issueId,
                fileName = file.name,
                fileBytes = file.readBytes(),
                contentType = mimeTypeFromFileName(file.name),
            )
                .onFailure { errorReporter.report(it) }
            _state.update { it.copy(isUploadingPhoto = false) }
            loadPhotos()
        }
    }

    private fun deletePhoto(attachmentId: Long) {
        if (attachmentId in _state.value.deletingPhotoIds) return
        _state.update { it.copy(deletingPhotoIds = (it.deletingPhotoIds + attachmentId).toImmutableSet()) }
        viewModelScope.launch {
            issueAttachmentRepository.deletePhoto(issueId = issueId, attachmentId = attachmentId)
                .onSuccess {
                    _state.update { state ->
                        val photosContent = state.photosContent
                        if (photosContent is IssuePhotosContent.Loaded) {
                            state.copy(
                                photosContent = photosContent.copy(
                                    items = photosContent.items.filterNot { it.id == attachmentId }.toImmutableList(),
                                ),
                            )
                        } else {
                            state
                        }
                    }
                }
                .onFailure { errorReporter.report(it) }
            _state.update { it.copy(deletingPhotoIds = (it.deletingPhotoIds - attachmentId).toImmutableSet()) }
        }
    }

    private fun mimeTypeFromFileName(fileName: String): String = when (fileName.substringAfterLast('.', "").lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "heic", "heif" -> "image/heic"
        else -> "image/jpeg"
    }
    private fun Issue.toUi() = IssueUi(
        id = id,
        title = title,
        description = description,
        status = status,
        priority = priority,
        vehicleLabel = vehicle?.let { "${it.licensePlate} · ${it.make} ${it.model}" } ?: "",
        vehicleType = vehicle?.type,
        reportedByName = reportedBy?.fullName ?: "—",
        assignedToName = assignedTo?.fullName ?: "—",
        createdAtFormatted = dateFormatter.formatDateTime(createdAt),
    )

    private fun IssueHistory.toUi() = IssueHistoryUi(
        id = id,
        type = type,
        statusTo = statusTo,
        performedByName = performedBy?.fullName,
        createdAtFormatted = dateFormatter.formatDateTime(createdAt),
        commentText = commentText,
    )
}
