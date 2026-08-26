package sk.momosilabs.truckTrack.issueAttachment.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import sk.momosilabs.truckTrack.file.model.FileModel
import sk.momosilabs.truckTrack.issueAttachment.model.IssueAttachmentModel

interface IssueAttachmentPersistence {
    fun findPage(issueId: Long, pageable: Pageable): Page<IssueAttachmentModel>
    fun findFileById(attachmentId: Long): FileModel
    fun findFileByIssueIdAndAttachmentId(issueId: Long, attachmentId: Long): FileModel
    fun linkFileToIssue(issueId: Long, fileId: Long): IssueAttachmentModel
    fun delete(issueId: Long, attachmentId: Long)
}
