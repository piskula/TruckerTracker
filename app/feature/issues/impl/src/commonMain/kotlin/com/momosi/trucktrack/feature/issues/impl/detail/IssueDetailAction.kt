package com.momosi.trucktrack.feature.issues.impl.detail

import io.github.vinceglb.filekit.core.PlatformFile

sealed interface IssueDetailAction {
    data class UpdateComment(val text: String) : IssueDetailAction
    data class UploadPhoto(val file: PlatformFile) : IssueDetailAction
    data class DeletePhoto(val attachmentId: Long) : IssueDetailAction
    data object SendComment : IssueDetailAction
    data object Retry : IssueDetailAction
    data object StartWorking : IssueDetailAction
    data object ResolveIssue : IssueDetailAction
    data object ReassignToMe : IssueDetailAction
}
