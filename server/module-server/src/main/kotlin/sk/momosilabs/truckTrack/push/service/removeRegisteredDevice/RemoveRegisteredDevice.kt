package sk.momosilabs.truckTrack.push.service.removeRegisteredDevice

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.push.service.PushRegistrationPersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.security.annotation.IsUser

@Service
class RemoveRegisteredDevice(
    private val persistence: PushRegistrationPersistence,
    private val currentUserService: CurrentUserService,
) : RemoveRegisteredDeviceUseCase {

    @IsUser
    @Transactional
    override fun remove() {
        persistence.deleteRegistrationsOfAccountId(currentUserService.currentUserId())
    }
}
