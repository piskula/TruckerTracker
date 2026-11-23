package sk.momosilabs.truckTrack.push.service.deletePushDevice

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.push.service.PushDevicePersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.security.annotation.IsUser

@Service
class DeletePushDevice(
    private val pushDevicePersistence: PushDevicePersistence,
    private val currentUserService: CurrentUserService,
) : DeletePushDeviceUseCase {

    @IsUser
    @Transactional
    override fun delete(firebaseInstallationId: String) {
        pushDevicePersistence.deleteByAccountIdAndFirebaseInstallationId(
            accountId = currentUserService.currentUserId(),
            firebaseInstallationId = firebaseInstallationId,
        )
    }
}
