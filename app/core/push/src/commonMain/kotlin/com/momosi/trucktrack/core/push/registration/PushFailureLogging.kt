package com.momosi.trucktrack.core.push.registration

import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.common.network.ApiException
import com.momosi.trucktrack.core.common.network.isTransientNetworkFailure
import com.momosi.trucktrack.core.push.firebase.FirebaseInstallationIdUnavailableException

internal fun logPushFailure(
    tag: String,
    throwable: Throwable,
    message: String,
) {
    if (throwable.isExpectedPushFailure()) {
        Logger.w(tag, throwable, message)
    } else {
        Logger.e(tag, throwable, message)
    }
}

private fun Throwable.isExpectedPushFailure(): Boolean = this is ApiException.NoConnection ||
    this is FirebaseInstallationIdUnavailableException ||
    isTransientNetworkFailure()
