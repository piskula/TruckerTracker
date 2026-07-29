package com.momosi.trucktrack.core.common.network

// TODO: match NSURLError domain codes once the Darwin Ktor engine is wired up.
actual fun Throwable.isTransientNetworkFailure(): Boolean = false
