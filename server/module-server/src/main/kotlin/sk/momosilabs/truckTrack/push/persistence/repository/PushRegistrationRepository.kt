package sk.momosilabs.truckTrack.push.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import sk.momosilabs.truckTrack.push.entity.PushRegistrationEntity
import java.util.UUID

interface PushRegistrationRepository : JpaRepository<PushRegistrationEntity, Long> {

    fun findAllByAccountId(accountId: UUID): List<PushRegistrationEntity>

    fun deleteByAccountId(accountId: UUID)

    fun deleteByInstallationIdAndAccountIdNot(installationId: String, accountId: UUID)
}
