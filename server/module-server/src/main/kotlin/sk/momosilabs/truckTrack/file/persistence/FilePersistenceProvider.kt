package sk.momosilabs.truckTrack.file.persistence

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.account.persistence.repository.AccountRepository
import sk.momosilabs.truckTrack.file.entity.FileEntity
import sk.momosilabs.truckTrack.file.model.FileModel
import sk.momosilabs.truckTrack.file.persistence.mapper.toModel
import sk.momosilabs.truckTrack.file.persistence.repository.FileRepository
import sk.momosilabs.truckTrack.file.service.FilePersistence
import sk.momosilabs.truckTrack.util.toUtcLocalDateTime

@Repository
class FilePersistenceProvider(
    private val fileRepository: FileRepository,
    private val accountRepository: AccountRepository,
) : FilePersistence {

    @Transactional
    override fun create(model: FileModel): FileModel {
        val entityToSave = FileEntity(
            uuid = model.uuid,
            bucket = model.bucket,
            storageLocation = model.storageLocation,
            filename = model.filename,
            contentType = model.contentType.toString(),
            sizeBytes = model.sizeBytes,
            uploadedBy = accountRepository.getReferenceById(model.uploadedBy.id),
            uploadedAtUtc = model.uploadedAt.toUtcLocalDateTime(),
        )
        return fileRepository.save(entityToSave).toModel()
    }

    @Transactional
    override fun delete(fileId: Long) {
        fileRepository.deleteById(fileId)
    }
}
