package com.momosi.trucktrack.core.push.registration

import com.momosi.trucktrack.core.common.error.ErrorReporter
import com.momosi.trucktrack.core.common.network.ApiException
import com.momosi.trucktrack.core.push.firebase.FirebaseInstallationIdProvider
import com.momosi.trucktrack.user.AuthManager
import com.momosi.trucktrack.user.model.AuthenticationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class PushRegistrationManagerImpl(
    private val registrar: PushDeviceRegistrar,
    private val authManager: AuthManager,
    firebaseInstallationIdProvider: FirebaseInstallationIdProvider,
    private val errorReporter: ErrorReporter,
    private val scope: CoroutineScope,
) : PushRegistrationManager {

    override val state: StateFlow<PushDeviceState>
        field = MutableStateFlow<PushDeviceState>(PushDeviceState.Unknown)

    private val operationMutex = Mutex()

    private val isSignedIn: Boolean
        get() = authManager.authenticationState.value == AuthenticationState.Authorized

    init {
        authManager.loginCompletions
            .onEach { registerThisDevice().onFailure(errorReporter::report) }
            .launchIn(scope)

        authManager.authenticationState
            .filter { it == AuthenticationState.Guest }
            .onEach { state.value = PushDeviceState.Unknown }
            .launchIn(scope)

        merge(flowOf(Unit), firebaseInstallationIdProvider.firebaseInstallationIdChanges.map { })
            .onEach { sendFirebaseInstallationIdChange() }
            .launchIn(scope)
    }

    override suspend fun registerThisDevice(): Result<Unit> = exclusive {
        if (isSignedIn) {
            registrar.register().onSuccess { state.value = it }.map { }
        } else {
            Result.failure(ApiException.Unauthorized)
        }
    }

    override suspend fun unregisterThisDevice(): Result<Unit> = exclusive {
        if (isSignedIn) {
            registrar.unregister().onSuccess {
                if (state.value == PushDeviceState.ThisDevice) state.value = PushDeviceState.None
            }
        } else {
            Result.failure(ApiException.Unauthorized)
        }
    }

    override suspend fun refreshState(): Result<PushDeviceState> = exclusive {
        if (isSignedIn) {
            registrar.currentState().onSuccess { state.value = it }
        } else {
            Result.failure(ApiException.Unauthorized)
        }
    }

    private suspend fun sendFirebaseInstallationIdChange() = exclusive {
        if (isSignedIn) registrar.sendFirebaseInstallationIdChange()
    }

    private suspend fun <T> exclusive(operation: suspend () -> T): T = scope.async {
        operationMutex.withLock { operation() }
    }.await()
}
