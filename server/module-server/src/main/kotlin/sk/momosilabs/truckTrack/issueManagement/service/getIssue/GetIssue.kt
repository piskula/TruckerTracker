package sk.momosilabs.truckTrack.issueManagement.service.getIssue

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.config.GlobalNotFoundException
import sk.momosilabs.truckTrack.issueManagement.model.IssueModel
import sk.momosilabs.truckTrack.issueManagement.service.IssuePersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.security.annotation.IsUser

@Service
class GetIssue(
    private val issuePersistence: IssuePersistence,
    private val currentUserService: CurrentUserService,
) : GetIssueUseCase {

    @IsUser
    @Transactional(readOnly = true)
    override fun get(issueId: Long): IssueModel {
        val issue = issuePersistence.findByIdOrThrow(issueId)
        if (currentUserService.isMechanic()) {
            return issue
        }
        if (issue.reportedBy.id == currentUserService.currentUserId()) {
            return issue
        }
        throw GlobalNotFoundException("Issue #$issueId not found or not visible.")
    }
}
