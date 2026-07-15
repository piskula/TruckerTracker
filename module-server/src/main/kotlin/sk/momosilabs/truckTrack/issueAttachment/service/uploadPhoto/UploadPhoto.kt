package sk.momosilabs.truckTrack.issueAttachment.service.uploadPhoto

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.config.GlobalUnprocessableException
import sk.momosilabs.truckTrack.file.model.FileModel
import sk.momosilabs.truckTrack.file.model.TruckTrackFile
import sk.momosilabs.truckTrack.file.service.FilePersistence
import sk.momosilabs.truckTrack.file.service.FileStorageService
import sk.momosilabs.truckTrack.issueAttachment.model.IssueAttachmentModel
import sk.momosilabs.truckTrack.issueAttachment.service.IssueAttachmentPersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.security.annotation.IsUser
import java.time.OffsetDateTime
import java.util.UUID

@Service
class UploadPhoto(
    private val issueAttachmentPersistence: IssueAttachmentPersistence,
    private val filePersistence: FilePersistence,
    private val fileStorageService: FileStorageService,
    private val currentUserService: CurrentUserService,
) : UploadPhotoUseCase {

    companion object {
        private const val BUCKET = "issues"
    }

    @IsUser
    @Transactional
    override fun upload(issueId: Long, file: TruckTrackFile): IssueAttachmentModel {
        if (file.content.isEmpty()) {
            throw GlobalUnprocessableException("File is empty")
        }

        val currentUser = currentUserService.currentUser()
        val uuid = UUID.randomUUID()
        val storageLocation = getStorageLocation(issueId, uuid, file.filename)

        fileStorageService.upload(file = file, bucket = BUCKET, key = storageLocation)

        val fileMeta = FileModel(
            id = 0L,
            uuid = uuid,
            bucket = BUCKET,
            storageLocation = storageLocation,
            filename = file.filename,
            contentType = file.contentType,
            sizeBytes = file.content.size.toLong(),
            uploadedBy = currentUser,
            uploadedAt = OffsetDateTime.now(),
        )

        val savedFile = filePersistence.create(fileMeta)

        return issueAttachmentPersistence.linkFileToIssue(issueId = issueId, fileId = savedFile.id)
    }

    private fun getStorageLocation(issueId: Long, uuid: UUID, filename: String): String {
        val extension = filename.substringAfterLast('.', "")
        val storedName = if (extension.isNotEmpty()) "$uuid.$extension" else "$uuid"
        return "issue/$issueId/$storedName"
    }

}
