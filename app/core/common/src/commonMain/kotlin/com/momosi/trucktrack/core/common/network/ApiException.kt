package com.momosi.trucktrack.core.common.network

sealed class ApiException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {

    data object NoConnection : ApiException()

    data object Unauthorized : ApiException()

    data class HttpError(val statusCode: Int, val serverMessage: String? = null) : ApiException("HTTP $statusCode: $serverMessage")
}

fun ApiException.isNotFound(): Boolean = this is ApiException.HttpError && statusCode == HTTP_NOT_FOUND_STATUS_CODE

private const val HTTP_NOT_FOUND_STATUS_CODE = 404
