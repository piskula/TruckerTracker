package com.momosi.trucktrack.core.push.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.momosi.trucktrack.core.common.coroutines.runCatchingCancellable
import com.momosi.trucktrack.core.push.registration.logPushFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.seconds

private const val TAG = "PushMessaging"
private const val MESSAGE_BUFFER = 8
private val REGISTRATION_TIMEOUT = 10.seconds

internal class FirebasePushMessagingClient(private val storage: ReceivedFirebaseInstallationIdStorage) : PushMessagingClient {

    private val latestFirebaseInstallationId = MutableStateFlow(storage.lastReceivedFirebaseInstallationId)

    override val firebaseInstallationIdChanges: Flow<String> = latestFirebaseInstallationId.filterNotNull()

    override val messages: Flow<Map<String, String>>
        field = MutableSharedFlow<Map<String, String>>(extraBufferCapacity = MESSAGE_BUFFER)

    override suspend fun currentFirebaseInstallationId(): String? = latestFirebaseInstallationId.value ?: requestRegistration()

    fun onRegistered(firebaseInstallationId: String) {
        storage.lastReceivedFirebaseInstallationId = firebaseInstallationId
        latestFirebaseInstallationId.value = firebaseInstallationId
    }

    fun onMessageReceived(data: Map<String, String>) {
        messages.tryEmit(data)
    }

    private suspend fun requestRegistration(): String? = withTimeoutOrNull(REGISTRATION_TIMEOUT) {
        val registration = runCatchingCancellable { FirebaseMessaging.getInstance().register().await() }
        if (registration.isFailure) {
            registration.exceptionOrNull()?.let { logPushFailure(TAG, it, "Registering with Firebase Cloud Messaging failed") }
            return@withTimeoutOrNull null
        }
        latestFirebaseInstallationId.filterNotNull().first()
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: IllegalStateException("Firebase Cloud Messaging task did not complete"))
        }
    }
}
