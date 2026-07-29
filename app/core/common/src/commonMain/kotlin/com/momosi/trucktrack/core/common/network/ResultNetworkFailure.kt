package com.momosi.trucktrack.core.common.network

inline fun <T> Result<T>.onNoConnectionFailure(action: (Throwable) -> Unit): Result<T> = onFailure { throwable ->
    if (throwable.isTransientNetworkFailure()) action(throwable)
}

inline fun <T> Result<T>.onNetworkFailure(action: (Throwable) -> Unit): Result<T> = onFailure { throwable ->
    if (!throwable.isTransientNetworkFailure()) action(throwable)
}
