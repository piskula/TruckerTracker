package sk.momosilabs.truckTrack.push.service.updateRegisteredDevice

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.config.GlobalUnprocessableException
import sk.momosilabs.truckTrack.push.model.PushRegistrationModel
import sk.momosilabs.truckTrack.push.service.PushRegistrationPersistence
import sk.momosilabs.truckTrack.security.CurrentUserService
import sk.momosilabs.truckTrack.security.annotation.IsUser
import java.time.OffsetDateTime

@Service
class UpdateRegisteredDevice(
    private val persistence: PushRegistrationPersistence,
    private val currentUserService: CurrentUserService,
) : UpdateRegisteredDeviceUseCase {

    @IsUser
    @Transactional
    override fun update(command: UpdateRegisteredDeviceCommand) {
        val account = currentUserService.currentUser()
        if (command.installationId.isBlank()) {
            throw GlobalUnprocessableException("Installation id must not be blank")
        }
        val model = PushRegistrationModel(
            accountId = account.id,
            installationId = command.installationId.trim(),
            platform = command.platform,
            deviceLabel = command.deviceLabel.trim(),
            registeredAt = OffsetDateTime.now(),
        )
        persistence.setNewRegistration(account.id, model)
    }
}
