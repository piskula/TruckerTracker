package com.momosi.trucktrack.core.push.registration

import com.momosi.trucktrack.user.SignOutHook

internal class UnregisterPushDeviceOnSignOut(private val pushRegistrationManager: PushRegistrationManager) : SignOutHook {

    override suspend fun beforeSessionCleared() {
        pushRegistrationManager.unregisterThisDevice()
    }
}
