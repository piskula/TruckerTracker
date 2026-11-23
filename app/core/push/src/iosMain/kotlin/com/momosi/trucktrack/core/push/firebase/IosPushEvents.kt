package com.momosi.trucktrack.core.push.firebase

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

private const val MESSAGE_BUFFER = 8

object IosPushEvents {

    internal val latestFirebaseInstallationId = MutableStateFlow<String?>(null)
    internal val messages = MutableSharedFlow<Map<String, String>>(extraBufferCapacity = MESSAGE_BUFFER)

    var deviceName: String? = null
    var notificationSettingsUrl: String? = null

    fun onFirebaseInstallationIdReceived(firebaseInstallationId: String) {
        latestFirebaseInstallationId.value = firebaseInstallationId
    }

    fun onMessageReceived(data: Map<String, String>) {
        messages.tryEmit(data)
    }
}
