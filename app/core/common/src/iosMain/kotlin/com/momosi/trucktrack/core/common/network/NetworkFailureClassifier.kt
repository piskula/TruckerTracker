package com.momosi.trucktrack.core.common.network

import io.ktor.client.engine.darwin.DarwinHttpRequestException
import io.ktor.client.network.sockets.SocketTimeoutException
import platform.Foundation.NSURLErrorCallIsActive
import platform.Foundation.NSURLErrorCannotConnectToHost
import platform.Foundation.NSURLErrorCannotFindHost
import platform.Foundation.NSURLErrorDNSLookupFailed
import platform.Foundation.NSURLErrorDataNotAllowed
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorInternationalRoamingOff
import platform.Foundation.NSURLErrorNetworkConnectionLost
import platform.Foundation.NSURLErrorNotConnectedToInternet

// NSURLErrorDomain codes that mean "device is offline / can't reach the server", as opposed to a genuine HTTP/app-level failure.
private val TRANSIENT_URL_ERROR_CODES = setOf(
    NSURLErrorNotConnectedToInternet,
    NSURLErrorNetworkConnectionLost,
    NSURLErrorCannotFindHost,
    NSURLErrorCannotConnectToHost,
    NSURLErrorDNSLookupFailed,
    NSURLErrorInternationalRoamingOff,
    NSURLErrorDataNotAllowed,
    NSURLErrorCallIsActive,
)

actual fun Throwable.isTransientNetworkFailure(): Boolean {
    var current: Throwable? = this
    var depth = 0
    while (current != null && depth < 10) {
        if (current is SocketTimeoutException ||
            (current is DarwinHttpRequestException && current.origin.domain == NSURLErrorDomain && current.origin.code in TRANSIENT_URL_ERROR_CODES)
        ) {
            return true
        }
        current = current.cause.takeIf { it !== current }
        depth++
    }
    return false
}
