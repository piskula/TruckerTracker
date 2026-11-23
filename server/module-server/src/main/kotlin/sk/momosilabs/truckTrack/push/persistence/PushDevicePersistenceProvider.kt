package sk.momosilabs.truckTrack.push.persistence

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.account.persistence.repository.AccountRepository
import sk.momosilabs.truckTrack.config.GlobalNotFoundException
import sk.momosilabs.truckTrack.push.entity.PushDeviceEntity
import sk.momosilabs.truckTrack.push.model.PushDeviceModel
import sk.momosilabs.truckTrack.push.persistence.mapper.asNewEntity
import sk.momosilabs.truckTrack.push.persistence.mapper.toModel
import sk.momosilabs.truckTrack.push.persistence.repository.PushDeviceRepository
import sk.momosilabs.truckTrack.push.service.PushDevicePersistence
import sk.momosilabs.truckTrack.util.toUtcLocalDateTime
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class PushDevicePersistenceProvider(
    private val pushDeviceRepository: PushDeviceRepository,
    private val accountRepository: AccountRepository,
) : PushDevicePersistence {

    @Transactional
    override fun lockAccount(accountId: UUID) {
        accountRepository.findLockedById(accountId)
    }

    @Transactional(readOnly = true)
    override fun findAllByAccountId(accountId: UUID): List<PushDeviceModel> =
        pushDeviceRepository.findAllByAccountIdOrderByRegisteredAtUtcAsc(accountId).map { it.toModel() }

    @Transactional(readOnly = true)
    override fun findByAccountIdAndFirebaseInstallationId(accountId: UUID, firebaseInstallationId: String): PushDeviceModel? =
        pushDeviceRepository.findByAccountIdAndFirebaseInstallationId(accountId, firebaseInstallationId)?.toModel()

    @Transactional
    override fun create(model: PushDeviceModel): PushDeviceModel {
        val entityToSave = model.asNewEntity(accountRepository::getReferenceById)
        return pushDeviceRepository.save(entityToSave).toModel()
    }

    @Transactional
    override fun update(
        accountId: UUID,
        registeredFirebaseInstallationId: String,
        newFirebaseInstallationId: String,
        updatedAt: OffsetDateTime,
    ): PushDeviceModel {
        val entity = pushDeviceRepository.findByAccountIdAndFirebaseInstallationId(accountId, registeredFirebaseInstallationId)
            ?: throw GlobalNotFoundException("push device for account=$accountId firebaseInstallationId=$registeredFirebaseInstallationId not found")
        entity.updateWith(
            newFirebaseInstallationId = newFirebaseInstallationId,
            updatedAtUtc = updatedAt.toUtcLocalDateTime(),
        )
        return entity.toModel()
    }

    private fun PushDeviceEntity.updateWith(newFirebaseInstallationId: String, updatedAtUtc: LocalDateTime) {
        firebaseInstallationId = newFirebaseInstallationId
        this.updatedAtUtc = updatedAtUtc
    }

    @Transactional
    override fun deleteAllForAccountOrFirebaseInstallationId(accountId: UUID, firebaseInstallationId: String) {
        val rows = pushDeviceRepository.findAllByAccountIdOrFirebaseInstallationId(accountId, firebaseInstallationId)
        if (rows.isNotEmpty()) {
            pushDeviceRepository.deleteAll(rows)
            pushDeviceRepository.flush()
        }
    }

    @Transactional
    override fun deleteByFirebaseInstallationIdOfOtherAccounts(firebaseInstallationId: String, accountId: UUID) {
        pushDeviceRepository.deleteByFirebaseInstallationIdAndAccountIdNot(
            firebaseInstallationId = firebaseInstallationId,
            accountId = accountId,
        )
    }

    @Transactional
    override fun deleteByAccountIdAndFirebaseInstallationId(accountId: UUID, firebaseInstallationId: String) {
        val rows = pushDeviceRepository.findAllByAccountIdAndFirebaseInstallationId(accountId, firebaseInstallationId)
        if (rows.isNotEmpty()) {
            pushDeviceRepository.deleteAll(rows)
            pushDeviceRepository.flush()
        }
    }

    @Transactional
    override fun deleteByFirebaseInstallationId(firebaseInstallationId: String) {
        val toDelete = pushDeviceRepository.findByFirebaseInstallationId(firebaseInstallationId) ?: return
        accountRepository.findLockedById(toDelete.account.id)
        pushDeviceRepository.deleteByIdAndFirebaseInstallationId(toDelete.id, firebaseInstallationId)
    }
}
