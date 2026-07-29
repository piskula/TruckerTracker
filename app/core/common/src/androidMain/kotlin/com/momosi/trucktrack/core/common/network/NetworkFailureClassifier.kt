package com.momosi.trucktrack.core.common.network

import android.system.ErrnoException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private const val GAI_EXCEPTION_CLASS_NAME = "android.system.GaiException"

actual fun Throwable.isTransientNetworkFailure(): Boolean {
    var current: Throwable? = this
    var depth = 0
    while (current != null && depth < 10) {
        if (current is UnknownHostException ||
            current is ConnectException ||
            current is SocketTimeoutException ||
            current is ErrnoException ||
            current.javaClass.name == GAI_EXCEPTION_CLASS_NAME
        ) {
            return true
        }
        current = current.cause.takeIf { it !== current }
        depth++
    }
    return false
}
