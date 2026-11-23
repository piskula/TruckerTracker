package sk.momosilabs.truckTrack.api.push

import sk.momosilabs.truckTrack.api.push.dto.PushRegistrationDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Push")
interface PushRegistrationApi {

    companion object {
        private const val ENDPOINT = "/api/v1/push"
    }

    @Operation(summary = "Register Firebase push identifiers for the current account")
    @PostMapping(ENDPOINT)
    fun updateRegisteredDeviceForPushNotifications(@RequestBody request: PushRegistrationDto)

    @Operation(summary = "Remove the stored Firebase push identifiers of the current account")
    @DeleteMapping(ENDPOINT)
    fun removeRegisteredDeviceForPushNotifications()
}
