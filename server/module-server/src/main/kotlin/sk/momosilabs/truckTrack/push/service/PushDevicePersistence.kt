package sk.momosilabs.truckTrack.push.service

import sk.momosilabs.truckTrack.push.model.PushDeviceModel
import java.time.OffsetDateTime
import java.util.UUID

interface PushDevicePersistence {

    fun lockAccount(accountId: UUID)

    fun findAllByAccountId(accountId: UUID): List<PushDeviceModel>

    fun findByAccountIdAndFirebaseInstallationId(accountId: UUID, firebaseInstallationId: String): PushDeviceModel?

    fun create(model: PushDeviceModel): PushDeviceModel

    fun update(
        accountId: UUID,
        registeredFirebaseInstallationId: String,
        newFirebaseInstallationId: String,
        updatedAt: OffsetDateTime,
    ): PushDeviceModel

    fun deleteAllForAccountOrFirebaseInstallationId(accountId: UUID, firebaseInstallationId: String)

    fun deleteByFirebaseInstallationIdOfOtherAccounts(firebaseInstallationId: String, accountId: UUID)

    fun deleteByAccountIdAndFirebaseInstallationId(accountId: UUID, firebaseInstallationId: String)

    fun deleteByFirebaseInstallationId(firebaseInstallationId: String)
}
