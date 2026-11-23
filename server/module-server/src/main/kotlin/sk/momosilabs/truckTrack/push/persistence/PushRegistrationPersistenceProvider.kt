package sk.momosilabs.truckTrack.push.persistence

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.account.persistence.repository.AccountRepository
import sk.momosilabs.truckTrack.push.entity.PushRegistrationEntity
import sk.momosilabs.truckTrack.push.model.PushRegistrationModel
import sk.momosilabs.truckTrack.push.persistence.mapper.asNewEntity
import sk.momosilabs.truckTrack.push.persistence.repository.PushRegistrationRepository
import sk.momosilabs.truckTrack.push.service.PushRegistrationPersistence
import sk.momosilabs.truckTrack.util.toUtcLocalDateTime
import java.util.UUID

@Repository
class PushRegistrationPersistenceProvider(
    private val pushRegistrationRepository: PushRegistrationRepository,
    private val accountRepository: AccountRepository,
) : PushRegistrationPersistence {

    @Transactional
    override fun deleteRegistrationsOfAccountId(accountId: UUID) {
        pushRegistrationRepository.deleteByAccountId(accountId)
    }

    @Transactional
    override fun setNewRegistration(accountId: UUID, model: PushRegistrationModel) {
        pushRegistrationRepository.deleteByInstallationIdAndAccountIdNot(model.installationId, accountId)
        pushRegistrationRepository.flush()

        val existing = pushRegistrationRepository.findAllByAccountId(accountId)
        if (existing.isEmpty()) {
            val entityToSave = model.asNewEntity(accountRepository.getReferenceById(accountId))
            pushRegistrationRepository.save(entityToSave)
            return
        }

        existing.first().updateWith(model)
        pushRegistrationRepository.deleteAll(existing.drop(1))
    }

    private fun PushRegistrationEntity.updateWith(toUpdate: PushRegistrationModel) {
        installationId = toUpdate.installationId
        platform = toUpdate.platform
        deviceLabel = toUpdate.deviceLabel
        registeredAtUtc = toUpdate.registeredAt.toUtcLocalDateTime()
    }
}
