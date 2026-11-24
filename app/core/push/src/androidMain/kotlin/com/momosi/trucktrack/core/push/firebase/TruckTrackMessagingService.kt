package com.momosi.trucktrack.core.push.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.momosi.trucktrack.core.push.message.PushNotificationPresenter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class TruckTrackMessagingService :
    FirebaseMessagingService(),
    KoinComponent {

    private val messagingClient: FirebasePushMessagingClient by inject()
    private val notificationPresenter: PushNotificationPresenter by inject()

    override fun onRegistered(firebaseInstallationId: String) {
        messagingClient.onRegistered(firebaseInstallationId)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        message.notification?.let { notification ->
            notificationPresenter.show(title = notification.title, body = notification.body)
        }
        messagingClient.onMessageReceived(message.data.toMap())
    }
}
