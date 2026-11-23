package sk.momosilabs.truckTrack.push.service

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import sk.momosilabs.truckTrack.push.model.PushMessage

@Component
class PushDeviceReplacedListener(
    private val pushNotificationService: PushNotificationService,
) {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onPushDeviceReplaced(event: PushDeviceReplacedEvent) {
        pushNotificationService.sendToDevice(
            firebaseInstallationId = event.previousFirebaseInstallationId,
            message = PushMessage.deviceUnregistered(newDeviceName = event.newDeviceName),
        )
    }
}
