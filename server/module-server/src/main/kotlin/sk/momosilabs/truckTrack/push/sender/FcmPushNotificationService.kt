package sk.momosilabs.truckTrack.push.sender

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.ApnsConfig
import com.google.firebase.messaging.Aps
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MessagingErrorCode
import com.google.firebase.messaging.Notification
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.MessageSource
import sk.momosilabs.truckTrack.push.model.PushMessage
import sk.momosilabs.truckTrack.push.model.PushText
import sk.momosilabs.truckTrack.push.service.PushDevicePersistence
import sk.momosilabs.truckTrack.push.service.PushNotificationService
import java.util.Locale
import java.util.UUID

class FcmPushNotificationService(
    private val firebaseMessaging: FirebaseMessaging,
    private val pushDevicePersistence: PushDevicePersistence,
    private val messageSource: MessageSource,
) : PushNotificationService {

    companion object {
        private val LOG = KotlinLogging.logger {}
        private const val ANDROID_CHANNEL_ID = "trucktrack_notifications"
        private const val APNS_DEFAULT_SOUND = "default"
        private val BASE_BUNDLE_LOCALE = Locale.ROOT
        private val INVALID_REGISTRATION_ERROR_CODES = setOf(
            MessagingErrorCode.UNREGISTERED,
            MessagingErrorCode.INVALID_ARGUMENT,
            MessagingErrorCode.SENDER_ID_MISMATCH,
        )
    }

    override fun sendToAccount(accountId: UUID, message: PushMessage) = sendSafely(message) {
        val device = pushDevicePersistence.findByAccountId(accountId)
        if (device == null) {
            LOG.debug { "No push device registered for account $accountId, ${message.type} push skipped" }
        } else {
            send(firebaseInstallationId = device.firebaseInstallationId, message = message)
        }
    }

    override fun sendToDevice(firebaseInstallationId: String, message: PushMessage) = sendSafely(message) {
        send(firebaseInstallationId = firebaseInstallationId, message = message)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun sendSafely(message: PushMessage, sending: () -> Unit) {
        try {
            sending()
        } catch (exception: RuntimeException) {
            LOG.warn(exception) { "Sending ${message.type} push failed" }
        }
    }

    private fun send(firebaseInstallationId: String, message: PushMessage) {
        try {
            firebaseMessaging.send(message.toFcmMessage(firebaseInstallationId))
        } catch (exception: FirebaseMessagingException) {
            handleSendFailure(exception, firebaseInstallationId, message)
        }
    }

    private fun handleSendFailure(exception: FirebaseMessagingException, firebaseInstallationId: String, message: PushMessage) {
        val errorCode = exception.messagingErrorCode
        if (errorCode in INVALID_REGISTRATION_ERROR_CODES) {
            LOG.info { "FCM rejected the Firebase Installation ID ($errorCode), removing the push device that still holds it" }
            pushDevicePersistence.deleteByFirebaseInstallationId(firebaseInstallationId)
        } else {
            LOG.warn(exception) { "Sending ${message.type} push failed ($errorCode)" }
        }
    }

    private fun PushText.resolve(): String =
        messageSource.getMessage(key, arguments.toTypedArray(), BASE_BUNDLE_LOCALE)

    private fun PushMessage.toFcmMessage(firebaseInstallationId: String): Message =
        Message.builder()
            .setFid(firebaseInstallationId)
            .setNotification(
                Notification.builder()
                    .setTitle(title.resolve())
                    .setBody(body.resolve())
                    .build()
            )
            .putAllData(data + (PushMessage.TYPE_DATA_KEY to type.name))
            .setAndroidConfig(
                AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder().setChannelId(ANDROID_CHANNEL_ID).build())
                    .build()
            )
            .setApnsConfig(
                ApnsConfig.builder()
                    .setAps(Aps.builder().setSound(APNS_DEFAULT_SOUND).build())
                    .build()
            )
            .build()
}
