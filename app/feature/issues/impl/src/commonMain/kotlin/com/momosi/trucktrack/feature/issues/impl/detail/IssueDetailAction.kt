package com.momosi.trucktrack.feature.issues.impl.detail

import com.momosi.trucktrack.core.issue.model.Issue
import io.github.vinceglb.filekit.core.PlatformFile

sealed interface IssueDetailAction {
    data class UpdateComment(val text: String) : IssueDetailAction
    data class UploadPhoto(val file: PlatformFile) : IssueDetailAction
    data class DeletePhoto(val attachmentId: Long) : IssueDetailAction
    data object SendComment : IssueDetailAction
    data object Retry : IssueDetailAction
    data class IssueStarted(val issue: Issue) : IssueDetailAction
    data object ResolveIssue : IssueDetailAction
    data object ReassignToMe : IssueDetailAction
    data object CancelIssue : IssueDetailAction
}
