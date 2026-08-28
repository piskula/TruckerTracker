package sk.momosilabs.truckTrack.issueManagement.service.cancelIssue

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.config.GlobalForbiddenException
import sk.momosilabs.truckTrack.config.GlobalUnprocessableException
import sk.momosilabs.truckTrack.issueManagement.entity.IssueHistoryEventType
import sk.momosilabs.truckTrack.issueManagement.entity.IssueStatus
import sk.momosilabs.truckTrack.issueManagement.model.IssueHistoryModel
import sk.momosilabs.truckTrack.issueManagement.model.IssueModel
import sk.momosilabs.truckTrack.issueManagement.service.IssuePersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.security.annotation.IsDriver
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class CancelIssue(
    private val issuePersistence: IssuePersistence,
    private val currentUserService: CurrentUserService,
) : CancelIssueUseCase {

    @IsDriver
    @Transactional
    override fun cancel(issueId: Long): IssueModel {
        val issue = issuePersistence.findById(issueId)
        val currentUser: AccountModel = currentUserService.currentUser()
        if (issue.reportedBy.id != currentUser.id) {
            throw GlobalForbiddenException("Only the reporter of the issue can cancel it")
        }
        if (issue.status != IssueStatus.OPEN) {
            throw GlobalUnprocessableException("Issue must be OPEN to cancel, current status: ${issue.status}")
        }

        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val saved = issuePersistence.updateStatusAndAssignee(issueId, IssueStatus.CANCELED, issue.assignedTo?.id, now)
        issuePersistence.saveHistory(
            IssueHistoryModel(
                id = UUID.randomUUID(),
                issueId = saved.id,
                type = IssueHistoryEventType.STATUS_CHANGE,
                performedBy = currentUser,
                createdAt = now,
                statusFrom = issue.status,
                statusTo = IssueStatus.CANCELED,
                commentText = null,
            )
        )
        return saved
    }
}
