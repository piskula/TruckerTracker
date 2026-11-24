package com.momosi.trucktrack.core.push.firebase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

private val REGISTRATION_TIMEOUT = 10.seconds

internal class IosPushMessagingClient : PushMessagingClient {

    override val firebaseInstallationIdChanges: Flow<String> = IosPushEvents.latestFirebaseInstallationId.filterNotNull()

    override val messages: Flow<Map<String, String>> = IosPushEvents.messages

    override suspend fun currentFirebaseInstallationId(): String? = IosPushEvents.latestFirebaseInstallationId.value
        ?: withTimeoutOrNull(REGISTRATION_TIMEOUT) { firebaseInstallationIdChanges.first() }
}
