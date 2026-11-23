package sk.momosilabs.truckTrack.push.sender

import io.github.oshai.kotlinlogging.KotlinLogging
import sk.momosilabs.truckTrack.push.model.PushMessage
import sk.momosilabs.truckTrack.push.service.PushNotificationService
import java.util.UUID

class DisabledPushNotificationService : PushNotificationService {

    companion object {
        private val LOG = KotlinLogging.logger {}
    }

    override fun sendToAccount(accountId: UUID, message: PushMessage) {
        LOG.warn { "Push notifications are disabled, ${message.type} push to account $accountId was not sent" }
    }

    override fun sendToDevice(firebaseInstallationId: String, message: PushMessage) {
        LOG.warn { "Push notifications are disabled, ${message.type} push to a device was not sent" }
    }
}
