package com.momosi.trucktrack.app

import com.momosi.trucktrack.core.push.firebase.IosPushEvents

object IosPushBridge {

    var deviceName: String?
        get() = IosPushEvents.deviceName
        set(value) {
            IosPushEvents.deviceName = value
        }

    var notificationSettingsUrl: String?
        get() = IosPushEvents.notificationSettingsUrl
        set(value) {
            IosPushEvents.notificationSettingsUrl = value
        }

    fun onFirebaseInstallationIdReceived(firebaseInstallationId: String) {
        IosPushEvents.onFirebaseInstallationIdReceived(firebaseInstallationId)
    }

    fun onMessageReceived(data: Map<String, String>) {
        IosPushEvents.onMessageReceived(data)
    }
}
