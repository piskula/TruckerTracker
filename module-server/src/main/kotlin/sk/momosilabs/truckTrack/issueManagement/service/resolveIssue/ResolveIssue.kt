package sk.momosilabs.truckTrack.issueManagement.service.resolveIssue

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.account.model.AccountModel
import sk.momosilabs.truckTrack.config.GlobalUnprocessableException
import sk.momosilabs.truckTrack.issueManagement.entity.IssueHistoryEventType
import sk.momosilabs.truckTrack.issueManagement.entity.IssueStatus
import sk.momosilabs.truckTrack.issueManagement.model.IssueHistoryModel
import sk.momosilabs.truckTrack.issueManagement.model.IssueModel
import sk.momosilabs.truckTrack.issueManagement.service.IssuePersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.security.annotation.IsMechanic
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class ResolveIssue(
    private val issuePersistence: IssuePersistence,
    private val currentUserService: CurrentUserService,
) : ResolveIssueUseCase {

    @IsMechanic
    @Transactional
    override fun resolve(issueId: Long): IssueModel {
        val issue = issuePersistence.findById(issueId)
        if (issue.status != IssueStatus.IN_PROGRESS) {
            throw GlobalUnprocessableException("Issue must be IN_PROGRESS to resolve, current status: ${issue.status}")
        }

        val resolvedBy: AccountModel = currentUserService.currentUser()

        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val saved = issuePersistence.updateStatus(issueId, IssueStatus.DONE, now)
        issuePersistence.saveHistory(
            IssueHistoryModel(
                id = UUID.randomUUID(),
                issueId = saved.id,
                type = IssueHistoryEventType.STATUS_CHANGE,
                performedBy = resolvedBy,
                createdAt = now,
                statusFrom = IssueStatus.IN_PROGRESS,
                statusTo = IssueStatus.DONE,
                commentText = null,
            )
        )
        return saved
    }
}
