package sk.momosilabs.truckTrack.push.service

import sk.momosilabs.truckTrack.config.GlobalNotFoundException
import sk.momosilabs.truckTrack.push.model.PushDeviceModel
import java.time.OffsetDateTime
import java.util.UUID

class FakePushDevicePersistence(vararg initialDevices: PushDeviceModel) : PushDevicePersistence {

    val devices = initialDevices.toMutableList()
    val lockedAccounts = mutableListOf<UUID>()
    private var nextId = (initialDevices.maxOfOrNull { it.id } ?: 0L) + 1

    override fun lockAccount(accountId: UUID) {
        lockedAccounts += accountId
    }

    override fun findAllByAccountId(accountId: UUID) =
        devices.filter { it.accountId == accountId }.sortedBy { it.registeredAt }

    override fun findByAccountIdAndFirebaseInstallationId(accountId: UUID, firebaseInstallationId: String): PushDeviceModel? {
        val matches = devices.filter { it.accountId == accountId && it.firebaseInstallationId == firebaseInstallationId }
        check(matches.size < 2) {
            "duplicate push device rows for account=$accountId firebaseInstallationId=$firebaseInstallationId"
        }
        return matches.firstOrNull()
    }

    override fun create(model: PushDeviceModel) = model.copy(id = nextId++).also { devices += it }

    override fun update(
        accountId: UUID,
        registeredFirebaseInstallationId: String,
        newFirebaseInstallationId: String,
        updatedAt: OffsetDateTime,
    ): PushDeviceModel {
        val index = devices.indexOfFirst {
            it.accountId == accountId && it.firebaseInstallationId == registeredFirebaseInstallationId
        }
        if (index < 0) {
            throw GlobalNotFoundException("push device for account=$accountId firebaseInstallationId=$registeredFirebaseInstallationId not found")
        }
        val updated = devices[index].copy(firebaseInstallationId = newFirebaseInstallationId, updatedAt = updatedAt)
        devices[index] = updated
        return updated
    }

    override fun deleteByAccountIdAndSessionId(accountId: UUID, sessionId: String) {
        lockAccount(accountId)
        devices.removeIf { it.accountId == accountId && it.sessionId == sessionId }
    }

    override fun deleteAllForAccountOrFirebaseInstallationId(accountId: UUID, firebaseInstallationId: String) {
        devices.removeIf { it.accountId == accountId || it.firebaseInstallationId == firebaseInstallationId }
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
