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
        val sessionId = currentUserService.currentSessionId()
        val accountId = currentUserService.currentUser().id
        pushDevicePersistence.lockAccount(accountId)

        val currentDevices = pushDevicePersistence.findAllByAccountId(accountId).mapTo(HashSet()) { it.firebaseInstallationId }
        pushDevicePersistence.deleteAllForAccountOrFirebaseInstallationId(
            accountId = accountId,
            firebaseInstallationId = command.firebaseInstallationId,
        )

        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val registered = pushDevicePersistence.create(command.toNewDevice(accountId, sessionId, now))
        publishDeviceReplacedEvents(currentDevices, command)
        return registered
    }

    private fun publishDeviceReplacedEvents(previousDevices: Set<String>, command: RegisterPushDeviceCommand) {
        previousDevices
            .filter { it != command.firebaseInstallationId }
            .forEach { device ->
                eventPublisher.publishEvent(
                    PushDeviceReplacedEvent(previousFirebaseInstallationId = device, newDeviceName = command.deviceName)
                )
            }
    }

    private fun RegisterPushDeviceCommand.toNewDevice(accountId: UUID, sessionId: String, now: OffsetDateTime) = PushDeviceModel(
        id = 0L,
        accountId = accountId,
        firebaseInstallationId = firebaseInstallationId,
        deviceName = deviceName,
        platform = platform,
        registeredAt = now,
        updatedAt = now,
        sessionId = sessionId,
    )
}
