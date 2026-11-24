package sk.momosilabs.truckTrack.push.controller

import sk.momosilabs.truckTrack.api.push.dto.PushDeviceDto
import sk.momosilabs.truckTrack.api.push.dto.PushDeviceRegistrationDto
import sk.momosilabs.truckTrack.api.push.dto.PushDeviceUpdateDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import sk.momosilabs.truckTrack.api.push.PushDeviceApi
import sk.momosilabs.truckTrack.push.service.deletePushDevice.DeletePushDeviceUseCase
import sk.momosilabs.truckTrack.push.service.getCurrentUserPushDevice.GetCurrentUserPushDeviceUseCase
import sk.momosilabs.truckTrack.push.service.registerPushDevice.RegisterPushDeviceUseCase
import sk.momosilabs.truckTrack.push.service.updatePushDevice.UpdatePushDeviceUseCase

@RestController
class PushDeviceController(
    private val registerPushDevice: RegisterPushDeviceUseCase,
    private val updatePushDevice: UpdatePushDeviceUseCase,
    private val getCurrentUserPushDevice: GetCurrentUserPushDeviceUseCase,
    private val deletePushDevice: DeletePushDeviceUseCase,
) : PushDeviceApi {

    override fun registerPushDevice(request: PushDeviceRegistrationDto): PushDeviceDto =
        registerPushDevice.register(request.toCommand()).toDto()

    override fun updatePushDevice(registeredFirebaseInstallationId: String, request: PushDeviceUpdateDto) =
        updatePushDevice.update(
            registeredFirebaseInstallationId = registeredFirebaseInstallationId,
            firebaseInstallationId = request.firebaseInstallationId,
        )

    override fun getPushDevice(): ResponseEntity<PushDeviceDto> =
        getCurrentUserPushDevice.get()
            ?.let { ResponseEntity.ok(it.toDto()) }
            ?: ResponseEntity.noContent().build()

    override fun deletePushDevice(firebaseInstallationId: String) =
        deletePushDevice.delete(firebaseInstallationId)

}
