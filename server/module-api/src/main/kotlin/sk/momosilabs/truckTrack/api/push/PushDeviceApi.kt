package sk.momosilabs.truckTrack.api.push

import sk.momosilabs.truckTrack.api.push.dto.PushDeviceDto
import sk.momosilabs.truckTrack.api.push.dto.PushDeviceRegistrationDto
import sk.momosilabs.truckTrack.api.push.dto.PushDeviceUpdateDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Push Devices")
interface PushDeviceApi {

    companion object {
        private const val ENDPOINT = "/api/v1/push-device"
    }

    @Operation(summary = "Register push device, taking over from the previously registered device")
    @PutMapping(ENDPOINT)
    fun registerPushDevice(@RequestBody request: PushDeviceRegistrationDto): PushDeviceDto

    @Operation(
        summary = "Update Firebase Installation ID of the registered push device",
        description = "The path holds the Firebase Installation ID the app registered last, the body the current one.",
    )
    @ApiResponse(responseCode = "409", description = "The Firebase Installation ID in the path does not hold the push device slot")
    @PatchMapping("$ENDPOINT/{firebaseInstallationId}")
    fun updatePushDevice(
        @PathVariable("firebaseInstallationId") registeredFirebaseInstallationId: String,
        @RequestBody request: PushDeviceUpdateDto,
    )

    @Operation(summary = "Get registered push device")
    @ApiResponse(responseCode = "204", description = "No push device is registered")
    @GetMapping(ENDPOINT)
    fun getPushDevice(): ResponseEntity<PushDeviceDto>

    @Operation(summary = "Delete push device if this Firebase Installation ID holds the push device slot")
    @DeleteMapping("$ENDPOINT/{firebaseInstallationId}")
    fun deletePushDevice(@PathVariable firebaseInstallationId: String)
}
