package sk.momosilabs.truckTrack.issueAttachment.service.deletePhoto

interface DeletePhotoUseCase {
    fun delete(issueId: Long, attachmentId: Long)
}
