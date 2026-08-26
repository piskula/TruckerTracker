package sk.momosilabs.truckTrack.issueAttachment.controller

import sk.momosilabs.truckTrack.api.common.PageDto
import sk.momosilabs.truckTrack.api.common.PageableDto
import sk.momosilabs.truckTrack.api.issue.dto.IssueAttachmentDto
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import sk.momosilabs.truckTrack.api.issue.IssueAttachmentApi
import sk.momosilabs.truckTrack.file.model.mapResponseEntity
import sk.momosilabs.truckTrack.issueAttachment.service.deletePhoto.DeletePhotoUseCase
import sk.momosilabs.truckTrack.issueAttachment.service.getPhoto.GetPhotoUseCase
import sk.momosilabs.truckTrack.issueAttachment.service.getPhotoList.GetPhotoListUseCase
import sk.momosilabs.truckTrack.issueAttachment.service.uploadPhoto.UploadPhotoUseCase
import sk.momosilabs.truckTrack.util.toDto
import sk.momosilabs.truckTrack.util.toModel

@RestController
class IssueAttachmentController(
    private val getPhotoList: GetPhotoListUseCase,
    private val getPhoto: GetPhotoUseCase,
    private val uploadPhoto: UploadPhotoUseCase,
    private val deletePhoto: DeletePhotoUseCase,
) : IssueAttachmentApi {

    override fun getPhotoList(issueId: Long, pageable: PageableDto): PageDto<IssueAttachmentDto> =
        getPhotoList.list(issueId, pageable.toModel()).toDto { it.toDto() }

    override fun uploadPhoto(issueId: Long, file: MultipartFile): IssueAttachmentDto =
        uploadPhoto.upload(issueId = issueId, file = file.toModel()).toDto()

    override fun downloadPhoto(issueId: Long, attachmentId: Long): ResponseEntity<ByteArrayResource> =
        getPhoto.get(attachmentId).mapResponseEntity()

    override fun deletePhoto(issueId: Long, attachmentId: Long) =
        deletePhoto.delete(issueId = issueId, attachmentId = attachmentId)

}
