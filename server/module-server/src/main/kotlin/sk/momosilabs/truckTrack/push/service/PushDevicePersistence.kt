package sk.momosilabs.truckTrack.push.service

import sk.momosilabs.truckTrack.push.model.PushDeviceModel
import java.util.UUID

interface PushDevicePersistence {

    fun lockAccount(accountId: UUID)

    fun findByAccountId(accountId: UUID): PushDeviceModel?

    fun create(model: PushDeviceModel): PushDeviceModel

    fun update(model: PushDeviceModel): PushDeviceModel

    fun deleteByFirebaseInstallationIdOfOtherAccounts(firebaseInstallationId: String, accountId: UUID)

    fun deleteByAccountIdAndFirebaseInstallationId(accountId: UUID, firebaseInstallationId: String)

    fun deleteByFirebaseInstallationId(firebaseInstallationId: String)
}
