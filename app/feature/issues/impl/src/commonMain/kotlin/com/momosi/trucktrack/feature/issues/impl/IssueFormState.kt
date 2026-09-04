package com.momosi.trucktrack.feature.issues.impl

import androidx.compose.runtime.Immutable
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
sealed interface VehiclesContent {
    data object Loading : VehiclesContent

    @Immutable
    data class Loaded(val vehicles: ImmutableList<Vehicle> = persistentListOf()) : VehiclesContent
    data object Error : VehiclesContent
}

@Immutable
sealed interface SubmitStatus {
    data object Idle : SubmitStatus
    data object ValidationError : SubmitStatus
    data object InProgress : SubmitStatus
    data object RequestFailed : SubmitStatus
}
