package com.momosi.trucktrack.core.push.registration

import kotlinx.coroutines.flow.StateFlow

interface PushRegistrationManager {

    val state: StateFlow<PushDeviceState>

    suspend fun registerThisDevice(): Result<Unit>

    suspend fun unregisterThisDevice(): Result<Unit>

    suspend fun refreshState(): Result<PushDeviceState>
}
