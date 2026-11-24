package com.momosi.trucktrack.core.push.firebase

import kotlinx.coroutines.flow.Flow

internal interface PushMessagingClient : FirebaseInstallationIdProvider {

    val messages: Flow<Map<String, String>>
}
