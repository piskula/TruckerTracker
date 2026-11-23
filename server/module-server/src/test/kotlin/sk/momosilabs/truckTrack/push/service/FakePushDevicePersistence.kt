package sk.momosilabs.truckTrack.push.service

import sk.momosilabs.truckTrack.push.model.PushDeviceModel
import java.util.UUID

class FakePushDevicePersistence(vararg initialDevices: PushDeviceModel) : PushDevicePersistence {

    val devices = initialDevices.toMutableList()
    val lockedAccounts = mutableListOf<UUID>()
    private var nextId = (initialDevices.maxOfOrNull { it.id } ?: 0L) + 1

    override fun lockAccount(accountId: UUID) {
        lockedAccounts += accountId
    }

    override fun findByAccountId(accountId: UUID) = devices.singleOrNull { it.accountId == accountId }

    override fun create(model: PushDeviceModel) = model.copy(id = nextId++).also { devices += it }

    override fun update(model: PushDeviceModel) = model.also { updated ->
        devices.replaceAll { if (it.id == updated.id) updated else it }
    }

    override fun deleteByFirebaseInstallationIdOfOtherAccounts(firebaseInstallationId: String, accountId: UUID) {
        devices.removeIf { it.firebaseInstallationId == firebaseInstallationId && it.accountId != accountId }
    }

    override fun deleteByAccountIdAndFirebaseInstallationId(accountId: UUID, firebaseInstallationId: String) {
        devices.removeIf { it.accountId == accountId && it.firebaseInstallationId == firebaseInstallationId }
    }

    override fun deleteByFirebaseInstallationId(firebaseInstallationId: String) {
        devices.removeIf { it.firebaseInstallationId == firebaseInstallationId }
    }
}
