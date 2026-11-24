package com.momosi.trucktrack.feature.issues.impl

import androidx.compose.runtime.Immutable

@Immutable
sealed interface SubmitStatus {
    data object Idle : SubmitStatus
    data object ValidationError : SubmitStatus
    data object InProgress : SubmitStatus
    data object RequestFailed : SubmitStatus
}
