package sk.momosilabs.truckTrack.issueManagement.service.cancelIssue

import sk.momosilabs.truckTrack.issueManagement.model.IssueModel

interface CancelIssueUseCase {

    fun cancel(issueId: Long): IssueModel
}
