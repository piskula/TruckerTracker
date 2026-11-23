package sk.momosilabs.truckTrack.push.service

import sk.momosilabs.truckTrack.push.model.PushMessage
import java.util.UUID

interface PushNotificationService {

    fun sendToAccount(accountId: UUID, message: PushMessage)

    fun sendToDevice(firebaseInstallationId: String, message: PushMessage)
}
