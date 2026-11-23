package sk.momosilabs.truckTrack.push.service.getCurrentUserPushDevice

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.push.model.PushDeviceModel
import sk.momosilabs.truckTrack.push.service.PushDevicePersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.security.annotation.IsUser

@Service
class GetCurrentUserPushDevice(
    private val pushDevicePersistence: PushDevicePersistence,
    private val currentUserService: CurrentUserService,
) : GetCurrentUserPushDeviceUseCase {

    @IsUser
    @Transactional(readOnly = true)
    override fun get(): PushDeviceModel? =
        pushDevicePersistence.findAllByAccountId(currentUserService.currentUserId()).lastOrNull()
}
