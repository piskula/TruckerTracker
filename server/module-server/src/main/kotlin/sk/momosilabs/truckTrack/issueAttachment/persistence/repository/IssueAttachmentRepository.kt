package sk.momosilabs.truckTrack.issueAttachment.persistence.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import sk.momosilabs.truckTrack.issueAttachment.entity.IssueAttachmentEntity

interface IssueAttachmentRepository : JpaRepository<IssueAttachmentEntity, Long> {
    fun findAllByIssueId(issueId: Long, pageable: Pageable): Page<IssueAttachmentEntity>
    fun findByIdAndIssueId(id: Long, issueId: Long): IssueAttachmentEntity?
}
