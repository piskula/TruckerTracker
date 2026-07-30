package com.momosi.trucktrack.core.common.network

inline fun <T> Result<T>.onNoConnectionFailure(action: (ApiException.NoConnection) -> Unit): Result<T> = onFailure { throwable ->
    if (throwable is ApiException.NoConnection) action(throwable)
}

inline fun <T> Result<T>.onNetworkFailure(action: (ApiException) -> Unit): Result<T> = onFailure { throwable ->
    if (throwable is ApiException && throwable !is ApiException.NoConnection) action(throwable)
}
