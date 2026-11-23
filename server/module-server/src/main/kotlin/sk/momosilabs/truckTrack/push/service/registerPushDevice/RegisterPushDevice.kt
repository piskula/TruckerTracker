package sk.momosilabs.truckTrack.push.service.registerPushDevice

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.push.model.PushDeviceModel
import sk.momosilabs.truckTrack.push.service.PushDevicePersistence
import sk.momosilabs.truckTrack.push.service.PushDeviceReplacedEvent
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.security.annotation.IsUser
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class RegisterPushDevice(
    private val pushDevicePersistence: PushDevicePersistence,
    private val currentUserService: CurrentUserService,
    private val eventPublisher: ApplicationEventPublisher,
) : RegisterPushDeviceUseCase {

    @IsUser
    @Transactional
    override fun register(command: RegisterPushDeviceCommand): PushDeviceModel {
        val accountId = currentUserService.currentUser().id
        pushDevicePersistence.lockAccount(accountId)
        pushDevicePersistence.deleteByFirebaseInstallationIdOfOtherAccounts(
            firebaseInstallationId = command.firebaseInstallationId,
            accountId = accountId,
        )

        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val currentDevice = pushDevicePersistence.findByAccountId(accountId)
        return when {
            currentDevice == null -> pushDevicePersistence.create(command.toNewDevice(accountId, now))
            currentDevice.firebaseInstallationId == command.firebaseInstallationId ->
                pushDevicePersistence.update(currentDevice.refreshedWith(command, now))
            else -> replace(currentDevice, command, now)
        }
    }

    private fun replace(previousDevice: PushDeviceModel, command: RegisterPushDeviceCommand, now: OffsetDateTime): PushDeviceModel {
        val newDevice = pushDevicePersistence.update(
            previousDevice.refreshedWith(command, now).copy(
                firebaseInstallationId = command.firebaseInstallationId,
                registeredAt = now,
            )
        )
        eventPublisher.publishEvent(
            PushDeviceReplacedEvent(
                previousFirebaseInstallationId = previousDevice.firebaseInstallationId,
                newDeviceName = command.deviceName,
            )
        )
        return newDevice
    }

    private fun RegisterPushDeviceCommand.toNewDevice(accountId: UUID, now: OffsetDateTime) = PushDeviceModel(
        id = 0L,
        accountId = accountId,
        firebaseInstallationId = firebaseInstallationId,
        deviceName = deviceName,
        platform = platform,
        registeredAt = now,
        updatedAt = now,
    )

    private fun PushDeviceModel.refreshedWith(command: RegisterPushDeviceCommand, now: OffsetDateTime) = copy(
        deviceName = command.deviceName,
        platform = command.platform,
        updatedAt = now,
    )
}
