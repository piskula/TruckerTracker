package sk.momosilabs.truckTrack.issueAttachment.service.deletePhoto

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.config.GlobalForbiddenException
import sk.momosilabs.truckTrack.config.GlobalUnprocessableException
import sk.momosilabs.truckTrack.file.service.FilePersistence
import sk.momosilabs.truckTrack.file.service.FileStorageService
import sk.momosilabs.truckTrack.issueAttachment.service.IssueAttachmentPersistence
import sk.momosilabs.truckTrack.issueManagement.entity.IssueStatus
import sk.momosilabs.truckTrack.issueManagement.service.IssuePersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.security.annotation.IsDriver

@Service
class DeletePhoto(
    private val issueAttachmentPersistence: IssueAttachmentPersistence,
    private val issuePersistence: IssuePersistence,
    private val filePersistence: FilePersistence,
    private val fileStorageService: FileStorageService,
    private val currentUserService: CurrentUserService,
) : DeletePhotoUseCase {

    @IsDriver
    @Transactional
    override fun delete(issueId: Long, attachmentId: Long) {
        val issue = issuePersistence.findById(issueId)
        if (issue.reportedBy.id != currentUserService.currentUserId()) {
            throw GlobalForbiddenException("Only the reporter of the issue can delete its photos")
        }
        if (issue.status != IssueStatus.OPEN) {
            throw GlobalUnprocessableException("Issue must be OPEN to delete a photo, current status: ${issue.status}")
        }

        val file = issueAttachmentPersistence.findFileByIssueIdAndAttachmentId(issueId, attachmentId)
        issueAttachmentPersistence.delete(issueId = issueId, attachmentId = attachmentId)
        filePersistence.delete(file.id)
        fileStorageService.delete(bucket = file.bucket, key = file.storageLocation)
    }
}
