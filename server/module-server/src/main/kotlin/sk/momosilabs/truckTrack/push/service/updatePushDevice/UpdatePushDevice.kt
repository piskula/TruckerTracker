package sk.momosilabs.truckTrack.push.service.updatePushDevice

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.config.GlobalConflictException
import sk.momosilabs.truckTrack.push.service.PushDevicePersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.security.annotation.IsUser
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
class UpdatePushDevice(
    private val pushDevicePersistence: PushDevicePersistence,
    private val currentUserService: CurrentUserService,
) : UpdatePushDeviceUseCase {

    @IsUser
    @Transactional
    override fun update(registeredFirebaseInstallationId: String, firebaseInstallationId: String) {
        val accountId = currentUserService.currentUserId()
        pushDevicePersistence.lockAccount(accountId)
        val device = pushDevicePersistence.findByAccountId(accountId)
            ?.takeIf { it.firebaseInstallationId == registeredFirebaseInstallationId }
            ?: throw GlobalConflictException(
                "Firebase Installation ID $registeredFirebaseInstallationId does not hold the push device slot of the current user"
            )

        pushDevicePersistence.deleteByFirebaseInstallationIdOfOtherAccounts(
            firebaseInstallationId = firebaseInstallationId,
            accountId = accountId,
        )
        pushDevicePersistence.update(
            device.copy(
                firebaseInstallationId = firebaseInstallationId,
                updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            )
        )
    }
}
