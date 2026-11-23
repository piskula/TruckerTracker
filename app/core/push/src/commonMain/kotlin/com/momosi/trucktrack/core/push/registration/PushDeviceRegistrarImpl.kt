package com.momosi.trucktrack.core.push.registration

import com.momosi.trucktrack.core.common.coroutines.runCatchingCancellable
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.common.network.ApiException
import com.momosi.trucktrack.core.push.firebase.FirebaseInstallationIdProvider
import com.momosi.trucktrack.core.push.firebase.FirebaseInstallationIdUnavailableException
import com.momosi.trucktrack.core.push.registration.api.PushDeviceApi
import com.momosi.trucktrack.core.push.registration.api.currentPushDevice
import com.momosi.trucktrack.core.push.registration.api.registrationRetry
import com.momosi.trucktrack.core.push.registration.api.updatePushDevice
import com.momosi.trucktrack.shared.push.PushDeviceDto
import com.momosi.trucktrack.shared.push.PushDeviceRegistrationDto
import com.momosi.trucktrack.shared.push.PushDeviceUpdateDto
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

private const val TAG = "PushRegistration"
private val UNREGISTER_TIMEOUT = 5.seconds

internal class PushDeviceRegistrarImpl(private val api: PushDeviceApi, private val deviceInfo: PushDeviceInfo, private val storage: PushRegistrationStorage, private val firebaseInstallationIdProvider: FirebaseInstallationIdProvider) :
    PushDeviceRegistrar {

    override suspend fun register(): Result<PushDeviceState> {
        val firebaseInstallationId = firebaseInstallationIdProvider.currentFirebaseInstallationId()
            ?: return Result.failure<PushDeviceState>(FirebaseInstallationIdUnavailableException())
                .onFailure { logPushFailure(TAG, it, "Device registration failed") }
        val registration = PushDeviceRegistrationDto(
            firebaseInstallationId = firebaseInstallationId,
            deviceName = deviceInfo.deviceName(),
            platform = deviceInfo.platform,
        )
        return runCatchingCancellable { api.register(registration) { registrationRetry() } }
            .onSuccess {
                storage.lastSentFirebaseInstallationId = firebaseInstallationId
                Logger.i(TAG, "Device registered for push notifications")
            }
            .onFailure { logPushFailure(TAG, it, "Device registration failed") }
            .map { PushDeviceState.ThisDevice }
    }

    override suspend fun sendFirebaseInstallationIdChange() {
        val lastSentFirebaseInstallationId = storage.lastSentFirebaseInstallationId ?: return
        val firebaseInstallationId = firebaseInstallationIdProvider.currentFirebaseInstallationId() ?: return
        if (firebaseInstallationId == lastSentFirebaseInstallationId) return

        runCatchingCancellable { api.updatePushDevice(lastSentFirebaseInstallationId, PushDeviceUpdateDto(firebaseInstallationId)) }
            .onSuccess { result ->
                storage.lastSentFirebaseInstallationId = firebaseInstallationId
                Logger.i(TAG, "Firebase Installation ID change sent: $result")
            }
            .onFailure { logPushFailure(TAG, it, "Sending the Firebase Installation ID change failed") }
    }

    override suspend fun unregister(): Result<Unit> {
        val result = withTimeoutOrNull(UNREGISTER_TIMEOUT) {
            val firebaseInstallationId = storage.lastSentFirebaseInstallationId
                ?: firebaseInstallationIdProvider.currentFirebaseInstallationId()
                ?: return@withTimeoutOrNull Result.failure(FirebaseInstallationIdUnavailableException())
            runCatchingCancellable { api.unregister(firebaseInstallationId) }
        } ?: Result.failure(ApiException.NoConnection)

        return result
            .onSuccess { Logger.i(TAG, "Device unregistered from push notifications") }
            .onFailure { logPushFailure(TAG, it, "Device unregistration failed") }
    }

    override suspend fun currentState(): Result<PushDeviceState> = runCatchingCancellable { stateOf(api.currentPushDevice()) }
        .onFailure { logPushFailure(TAG, it, "Loading the push device failed") }

    private suspend fun stateOf(registeredDevice: PushDeviceDto?): PushDeviceState = when {
        registeredDevice == null -> PushDeviceState.None
        isThisDevice(registeredDevice.firebaseInstallationId) -> PushDeviceState.ThisDevice
        else -> PushDeviceState.OtherDevice(registeredDevice.deviceName)
    }

    private suspend fun isThisDevice(firebaseInstallationId: String): Boolean = firebaseInstallationId == storage.lastSentFirebaseInstallationId ||
        firebaseInstallationId == firebaseInstallationIdProvider.currentFirebaseInstallationId()
}
