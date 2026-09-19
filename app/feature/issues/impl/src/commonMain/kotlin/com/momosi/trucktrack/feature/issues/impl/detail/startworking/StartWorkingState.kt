package com.momosi.trucktrack.feature.issues.impl.detail.startworking

import androidx.compose.runtime.Immutable
import com.momosi.trucktrack.core.issue.model.RepairType
import com.momosi.trucktrack.core.issue.model.VehicleSystem

@Immutable
data class StartWorkingState(val selectedRepairType: RepairType? = null, val selectedVehicleSystem: VehicleSystem? = null, val isSubmitting: Boolean = false, val showValidationErrors: Boolean = false) {
    val repairTypeError: Boolean
        get() = showValidationErrors && selectedRepairType == null

    val vehicleSystemError: Boolean
        get() = showValidationErrors && selectedVehicleSystem == null
}
