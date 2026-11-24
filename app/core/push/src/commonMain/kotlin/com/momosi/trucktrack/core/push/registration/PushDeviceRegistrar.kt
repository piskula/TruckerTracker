package com.momosi.trucktrack.core.push.registration

internal interface PushDeviceRegistrar {

    suspend fun register(): Result<PushDeviceState>

    suspend fun sendFirebaseInstallationIdChange()

    suspend fun unregister(): Result<Unit>

    suspend fun currentState(): Result<PushDeviceState>
}
