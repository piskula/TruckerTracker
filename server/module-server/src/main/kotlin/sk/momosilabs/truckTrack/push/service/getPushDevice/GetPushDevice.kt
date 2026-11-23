package sk.momosilabs.truckTrack.push.service.getPushDevice

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.push.model.PushDeviceModel
import sk.momosilabs.truckTrack.push.service.PushDevicePersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.security.annotation.IsUser

@Service
class GetPushDevice(
    private val pushDevicePersistence: PushDevicePersistence,
    private val currentUserService: CurrentUserService,
) : GetPushDeviceUseCase {

    @IsUser
    @Transactional(readOnly = true)
    override fun get(): PushDeviceModel? =
        pushDevicePersistence.findByAccountId(currentUserService.currentUserId())
}
