package com.momosi.trucktrack.core.common.network

sealed class ApiException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    data object NoConnection : ApiException()
    data object Unauthorized : ApiException()
    data object HttpError : ApiException()
}
