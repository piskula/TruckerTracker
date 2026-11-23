package sk.momosilabs.truckTrack.push.controller

import org.springframework.web.bind.annotation.RestController
import sk.momosilabs.truckTrack.api.push.PushRegistrationApi
import sk.momosilabs.truckTrack.api.push.dto.PushRegistrationDto
import sk.momosilabs.truckTrack.push.service.removeRegisteredDevice.RemoveRegisteredDeviceUseCase
import sk.momosilabs.truckTrack.push.service.updateRegisteredDevice.UpdateRegisteredDeviceUseCase

@RestController
class PushRegistrationController(
    private val updateRegisteredDevice: UpdateRegisteredDeviceUseCase,
    private val removeRegisteredDevice: RemoveRegisteredDeviceUseCase,
) : PushRegistrationApi {

    override fun updateRegisteredDeviceForPushNotifications(request: PushRegistrationDto) =
        updateRegisteredDevice.update(request.toCommand())

    override fun removeRegisteredDeviceForPushNotifications() =
        removeRegisteredDevice.remove()
}
