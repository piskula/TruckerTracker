package sk.momosilabs.truckTrack.push.sender

import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import sk.momosilabs.truckTrack.push.service.PushDevicePersistence
import sk.momosilabs.truckTrack.push.service.PushNotificationService
import java.io.IOException
import java.util.Base64

@Configuration
@EnableConfigurationProperties(FirebaseProperties::class)
class FirebaseConfig {

    companion object {
        private val LOG = KotlinLogging.logger {}
        private const val FIREBASE_APP_NAME = "trucktrack"
        private const val INVALID_CREDENTIALS_MESSAGE =
            "firebase.credentials-base64 is not a Base64-encoded Firebase service account JSON"
    }

    @Bean
    fun pushNotificationService(
        props: FirebaseProperties,
        pushDevicePersistence: PushDevicePersistence,
        messageSource: MessageSource,
    ): PushNotificationService {
        if (props.credentialsBase64.isBlank()) {
            LOG.warn { "FIREBASE_CREDENTIALS_BASE64 is not set, push notifications are disabled" }
            return DisabledPushNotificationService()
        }
        return FcmPushNotificationService(
            firebaseMessaging = FirebaseMessaging.getInstance(firebaseApp(props.credentialsBase64)),
            pushDevicePersistence = pushDevicePersistence,
            messageSource = messageSource,
        )
    }

    private fun firebaseApp(credentialsBase64: String): FirebaseApp =
        FirebaseApp.getApps().firstOrNull { it.name == FIREBASE_APP_NAME }
            ?: FirebaseApp.initializeApp(
                FirebaseOptions.builder().setCredentials(decodeCredentials(credentialsBase64)).build(),
                FIREBASE_APP_NAME,
            )

    private fun decodeCredentials(credentialsBase64: String): GoogleCredentials =
        try {
            Base64.getMimeDecoder().decode(credentialsBase64).inputStream().use { ServiceAccountCredentials.fromStream(it) }
        } catch (_: IllegalArgumentException) {
            error(INVALID_CREDENTIALS_MESSAGE)
        } catch (_: IOException) {
            error(INVALID_CREDENTIALS_MESSAGE)
        }
}
