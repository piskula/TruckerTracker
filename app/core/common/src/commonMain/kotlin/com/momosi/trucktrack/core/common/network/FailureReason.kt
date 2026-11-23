package com.momosi.trucktrack.core.common.network

enum class FailureReason { NoConnection, Other }

fun Throwable.toFailureReason(): FailureReason = when (this) {
    is ApiException.NoConnection -> FailureReason.NoConnection
    else -> FailureReason.Other
}
