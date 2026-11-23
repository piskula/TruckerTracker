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
    override fun findByAccountId(accountId: UUID): PushDeviceModel? =
        pushDeviceRepository.findByAccountId(accountId)?.toModel()

    @Transactional
    override fun create(model: PushDeviceModel): PushDeviceModel {
        val entityToSave = model.asNewEntity(accountRepository::getReferenceById)
        return pushDeviceRepository.save(entityToSave).toModel()
    }

    @Transactional
    override fun update(model: PushDeviceModel): PushDeviceModel {
        val entity = pushDeviceRepository.findById(model.id)
            .orElseThrow { GlobalNotFoundException("push device id=${model.id} not found") }
        entity.updateWith(model)
        return entity.toModel()
    }

    private fun PushDeviceEntity.updateWith(toUpdate: PushDeviceModel) {
        firebaseInstallationId = toUpdate.firebaseInstallationId
        deviceName = toUpdate.deviceName
        platform = toUpdate.platform
        registeredAtUtc = toUpdate.registeredAt.toUtcLocalDateTime()
        updatedAtUtc = toUpdate.updatedAt.toUtcLocalDateTime()
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
        pushDeviceRepository.deleteByAccountIdAndFirebaseInstallationId(
            accountId = accountId,
            firebaseInstallationId = firebaseInstallationId,
        )
    }

    @Transactional
    override fun deleteByFirebaseInstallationId(firebaseInstallationId: String) {
        pushDeviceRepository.deleteByFirebaseInstallationId(firebaseInstallationId)
    }
}
